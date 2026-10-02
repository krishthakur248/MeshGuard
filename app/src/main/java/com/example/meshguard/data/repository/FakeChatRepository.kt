package com.example.meshguard.data.repository

import com.example.meshguard.data.model.ChatMessage
import com.example.meshguard.data.model.DeliveryState
import com.example.meshguard.data.model.MessageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class FakeChatRepository : ChatRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "msg-001",
                senderId = "RES-CHIEF",
                senderName = "Rescue Command (Sector 4)",
                type = MessageType.TEXT,
                content = "BROADCAST: Sector 4 North Gymnasium confirmed as Safe Haven. Water and medical triage on site.",
                audioDurationSec = 0,
                hopCount = 3,
                deliveryState = DeliveryState.DELIVERED_TO_RESCUER,
                timestamp = System.currentTimeMillis() - 3600000L,
                isFromMe = false
            ),
            ChatMessage(
                id = "msg-002",
                senderId = "USR-209",
                senderName = "Marcus Chen",
                type = MessageType.TEXT,
                content = "Stuck near 4-C parking ramp. Have insulin pack, but need cold storage or quick evac.",
                audioDurationSec = 0,
                hopCount = 1,
                deliveryState = DeliveryState.RELAYED,
                timestamp = System.currentTimeMillis() - 3000000L,
                isFromMe = false
            ),
            ChatMessage(
                id = "msg-003",
                senderId = "USR-7701",
                senderName = "Me",
                type = MessageType.TEXT,
                content = "Trapped in B1 stairwell, Sector 4-B. Door jammed by debris. Structural ceiling intact.",
                audioDurationSec = 0,
                hopCount = 0,
                deliveryState = DeliveryState.RELAYED,
                timestamp = System.currentTimeMillis() - 2400000L,
                isFromMe = true
            ),
            ChatMessage(
                id = "msg-004",
                senderId = "USR-7701",
                senderName = "Me",
                type = MessageType.VOICE,
                content = "[Voice Note: Sounding pipe taps for localization]",
                audioDurationSec = 14,
                hopCount = 1,
                deliveryState = DeliveryState.RELAYED,
                timestamp = System.currentTimeMillis() - 2100000L,
                isFromMe = true
            )
        )
    )
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    override fun sendTextMessage(content: String) {
        val newMessage = ChatMessage(
            id = "msg-" + UUID.randomUUID().toString().take(8),
            senderId = "USR-7701",
            senderName = "Me",
            type = MessageType.TEXT,
            content = content,
            audioDurationSec = 0,
            hopCount = 0,
            deliveryState = DeliveryState.STORED,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        _messages.update { it + newMessage }
    }

    override fun sendVoiceDropMessage(audioDurationSec: Int, voiceBase64Payload: String) {
        val voiceMessage = ChatMessage(
            id = "msg-" + UUID.randomUUID().toString().take(8),
            senderId = "USR-7701",
            senderName = "Me",
            type = MessageType.VOICE,
            content = "[15s Voice Drop Blackbox Note]",
            audioDurationSec = audioDurationSec.coerceIn(1, 15),
            hopCount = 0,
            deliveryState = DeliveryState.STORED,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        _messages.update { it + voiceMessage }
    }

    override fun markDeliveredToRescuer(messageId: String) {
        _messages.update { list ->
            list.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(deliveryState = DeliveryState.DELIVERED_TO_RESCUER)
                } else msg
            }
        }
    }
}
