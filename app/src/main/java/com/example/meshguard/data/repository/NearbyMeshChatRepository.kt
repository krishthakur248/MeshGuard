package com.example.meshguard.data.repository

import android.util.Log
import com.example.meshguard.data.local.dao.ChatDao
import com.example.meshguard.data.model.ChatMessage
import com.example.meshguard.data.model.DeliveryState
import com.example.meshguard.data.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

private const val TAG = "NearbyChat"

/** Prefix on every raw Nearby Connections payload that carries a chat message. */
const val CHAT_PAYLOAD_PREFIX = "CHAT:"

/**
 * Step 12 — Real chat repository backed by:
 *   - Room (ChatDao) for persistence across restarts
 *   - Nearby Connections gossip relay for multi-hop delivery
 *
 * Design:
 *   - Messages are identified by a UUID [ChatMessage.id].
 *   - On send: persist locally → emit to UI → send JSON to all connected peers.
 *   - On receive (called by NearbyMeshRepository.payloadCallback):
 *       dedupe by id (Room IGNORE + in-memory set) → store → emit → relay to other peers.
 *   - TTL / hop-count mirror the SurvivorPacket relay rules.
 *   - [sendRawToAllPeers] is injected at init so this repo can send without depending on the
 *     full NearbyMeshRepository (keeps a single responsibility).
 */
class NearbyMeshChatRepository(
    private val chatDao: ChatDao,
    private val accountRepository: AccountRepository,
    /**
     * Injected function: send raw bytes to all currently connected Nearby peers.
     * Supplied by NearbyMeshRepository after construction.
     */
    private val sendRawToAllPeers: (ByteArray) -> Unit = {},
    /**
     * Injected function: send raw bytes to all peers EXCEPT one endpoint id.
     * Used during gossip relay to not echo back to the sender.
     */
    private val sendRawToAllPeersExcept: (ByteArray, String) -> Unit = { _, _ -> }
) : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** In-memory set of message ids we have already processed this session (fast dedupe). */
    private val seenIds = mutableSetOf<String>()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    /** Default TTL for chat messages (hops allowed). */
    private val CHAT_TTL = 8

    init {
        // Load persisted messages from Room and stream them into the UI state
        scope.launch {
            chatDao.getAllMessages().collect { entities ->
                val msgs = entities.map { it.toChatMessage() }
                _messages.value = msgs
                // Populate the dedup set from DB on restart
                synchronized(seenIds) {
                    seenIds.addAll(msgs.map { it.id })
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // ChatRepository interface
    // -------------------------------------------------------------------------

    override fun sendTextMessage(content: String) {
        if (content.isBlank()) return
        val myId = accountRepository.getOrCreateSurvivorId()
        val myName = accountRepository.medicalRecord.value.name.ifBlank { "Unknown" }
        val msg = ChatMessage(
            id = "CMSG-${UUID.randomUUID().toString().take(10).uppercase()}",
            senderId = myId,
            senderName = myName,
            type = MessageType.TEXT,
            content = content.trim(),
            hopCount = 0,
            deliveryState = DeliveryState.STORED,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        persistAndRelay(msg, senderEndpointId = null)
    }

    override fun sendVoiceDropMessage(audioDurationSec: Int, voiceBase64Payload: String) {
        val myId = accountRepository.getOrCreateSurvivorId()
        val myName = accountRepository.medicalRecord.value.name.ifBlank { "Unknown" }
        val msg = ChatMessage(
            id = "CMSG-${UUID.randomUUID().toString().take(10).uppercase()}",
            senderId = myId,
            senderName = myName,
            type = MessageType.VOICE,
            content = voiceBase64Payload,
            audioDurationSec = audioDurationSec.coerceIn(1, 15),
            hopCount = 0,
            deliveryState = DeliveryState.STORED,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        persistAndRelay(msg, senderEndpointId = null)
    }

    override fun markDeliveredToRescuer(messageId: String) {
        scope.launch {
            _messages.value = _messages.value.map { msg ->
                if (msg.id == messageId) msg.copy(deliveryState = DeliveryState.DELIVERED_TO_RESCUER) else msg
            }
        }
    }

    // -------------------------------------------------------------------------
    // Incoming message from Nearby Connections (called by NearbyMeshRepository)
    // -------------------------------------------------------------------------

    /**
     * Called by [NearbyMeshRepository] when a CHAT payload arrives from [senderEndpointId].
     * Handles dedup, persistence, and gossip relay.
     */
    fun onChatPayloadReceived(jsonStr: String, senderEndpointId: String) {
        try {
            val root = JSONObject(jsonStr)
            val msgId = root.optString("id", "")
            if (msgId.isBlank()) return

            // Fast dedup — check in-memory set first
            synchronized(seenIds) {
                if (msgId in seenIds) {
                    Log.d(TAG, "DROPPED duplicate chat msg $msgId")
                    return
                }
                seenIds.add(msgId)
            }

            val hopCount = root.optInt("hopCount", 0)
            val ttl = root.optInt("ttl", CHAT_TTL)

            // TTL check
            if (hopCount >= ttl) {
                Log.d(TAG, "DROPPED chat msg $msgId — hopCount($hopCount) >= ttl($ttl)")
                return
            }

            val myId = accountRepository.getOrCreateSurvivorId()
            val senderId = root.optString("senderId", "")

            // Loopback check — drop our own messages arriving via relay
            if (senderId == myId) {
                Log.d(TAG, "DROPPED loopback chat from $myId")
                return
            }

            val msg = ChatMessage(
                id = msgId,
                senderId = senderId,
                senderName = root.optString("senderName", "Unknown"),
                type = try { MessageType.valueOf(root.optString("type", "TEXT")) } catch (e: Exception) { MessageType.TEXT },
                content = root.optString("content", ""),
                audioDurationSec = root.optInt("audioDurationSec", 0),
                hopCount = hopCount + 1,    // increment for the relay we store and forward
                deliveryState = DeliveryState.RELAYED,
                timestamp = root.optLong("timestamp", System.currentTimeMillis()),
                isFromMe = false
            )

            Log.i(TAG, "✓ CHAT RECEIVED from $senderEndpointId: [${msg.senderName}] ${msg.content.take(40)}")

            // Persist and gossip relay (excluding the sender endpoint)
            scope.launch {
                chatDao.insert(com.example.meshguard.data.local.entity.ChatMessageEntity.fromChatMessage(msg, ttl = ttl))
                // Relay to all peers except sender if still within TTL
                if (msg.hopCount < ttl) {
                    val relayBytes = serializeMessage(msg, ttl).toByteArray(Charsets.UTF_8)
                    sendRawToAllPeersExcept(relayBytes, senderEndpointId)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse chat payload", e)
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun persistAndRelay(msg: ChatMessage, senderEndpointId: String?) {
        scope.launch {
            chatDao.insert(com.example.meshguard.data.local.entity.ChatMessageEntity.fromChatMessage(msg, ttl = CHAT_TTL))
            val payload = (CHAT_PAYLOAD_PREFIX + serializeMessage(msg, CHAT_TTL)).toByteArray(Charsets.UTF_8)
            if (senderEndpointId != null) {
                sendRawToAllPeersExcept(payload, senderEndpointId)
            } else {
                sendRawToAllPeers(payload)
            }
        }
    }

    private fun serializeMessage(msg: ChatMessage, ttl: Int): String {
        val root = JSONObject()
        root.put("id", msg.id)
        root.put("senderId", msg.senderId)
        root.put("senderName", msg.senderName)
        root.put("type", msg.type.name)
        root.put("content", msg.content)
        root.put("audioDurationSec", msg.audioDurationSec)
        root.put("hopCount", msg.hopCount)
        root.put("ttl", ttl)
        root.put("timestamp", msg.timestamp)
        return root.toString()
    }
}
