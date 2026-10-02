package com.example.meshguard.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.meshguard.data.local.dao.PacketDao
import com.example.meshguard.data.local.entity.PacketEntity
import com.example.meshguard.data.model.HistoryNode
import com.example.meshguard.data.model.MeshPeer
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.data.model.UserRole
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "NearbyMesh"

/**
 * Separator between the human-readable device name and the random UUID
 * that we embed in the advertised endpoint name for the tiebreaker.
 * Example advertised name: "Pixel 7#a1b2c3d4"
 */
private const val NAME_SEPARATOR = "#"

class NearbyMeshRepository(
    context: Context,
    private val accountRepository: AccountRepository = AccountRepository.getInstance(context),
    private val survivorRepository: SurvivorRepository? = null,
    private val packetDao: PacketDao? = null
) : MeshRepository {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fallbackConnectionJobs = mutableMapOf<String, Job>()

    @Volatile
    private var isMeshRunning = false

    private val _nearbyPeers = MutableStateFlow<List<MeshPeer>>(emptyList())
    override val nearbyPeers: StateFlow<List<MeshPeer>> = _nearbyPeers.asStateFlow()

    private val _packetsCarriedCount = MutableStateFlow(0)
    override val packetsCarriedCount: StateFlow<Int> = _packetsCarriedCount.asStateFlow()

    private val _isBroadcastingBeacon = MutableStateFlow(accountRepository.loadIsBroadcasting())
    override val isBroadcastingBeacon: StateFlow<Boolean> = _isBroadcastingBeacon.asStateFlow()

    private val _isBatterySaverActive = MutableStateFlow(true)
    override val isBatterySaverActive: StateFlow<Boolean> = _isBatterySaverActive.asStateFlow()

    private val _breadcrumbChain = MutableStateFlow<List<HistoryNode>>(emptyList())
    override val breadcrumbChain: StateFlow<List<HistoryNode>> = _breadcrumbChain.asStateFlow()

    // Must be identical on every phone.
    private val serviceId = "com.example.meshguard"

    // Random UUID generated once per app session.
    // Embedded into our advertised name so the remote can compare it with theirs.
    // This is the stable, globally-unique value used for the tiebreaker.
    private val myUuid: String = UUID.randomUUID().toString().take(8)  // short 8-char hex

    private val connectionsClient = Nearby.getConnectionsClient(context.applicationContext)

    // Track names for endpoints in the middle of handshaking.
    private val pendingNames = mutableMapOf<String, String>()

    // Endpoints we have already requested a connection to (avoid duplicate requests).
    private val pendingRequests = mutableSetOf<String>()

    private var isStartingMesh = false

    init {
        // Step 6: Reactively observe packet count from Room so it survives app restarts
        // and accurately reflects the unique survivor packets stored in the database.
        if (packetDao != null) {
            ioScope.launch {
                packetDao.getPacketCount().collect { count ->
                    _packetsCarriedCount.value = count
                    Log.d(TAG, "init: packetDao.getPacketCount emitted $count")
                }
            }
        }

        // Step 7: When our survivor status or medical info updates, broadcast the fresh
        // packet to all currently connected peers so the update immediately propagates across the mesh.
        // Rescuers do not generate an SOS distress beacon.
        if (survivorRepository != null) {
            scope.launch {
                var isFirst = true
                survivorRepository.mySurvivorPacket.collect { packet ->
                    if (isFirst) {
                        isFirst = false
                        return@collect
                    }
                    val isRescuer = accountRepository.userRole.value == UserRole.RESCUER
                    if (!isRescuer && _nearbyPeers.value.isNotEmpty()) {
                        Log.i(TAG, "mySurvivorPacket updated: broadcasting to ${_nearbyPeers.value.size} peer(s)")
                        for (peer in _nearbyPeers.value) {
                            sendSinglePacket(packet, peer.peerId)
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Payload callback — receive packets from peers
    // -------------------------------------------------------------------------
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes()
            if (bytes == null) {
                Log.w(TAG, "onPayloadReceived from $endpointId: payload bytes is null")
                return
            }

            try {
                val jsonStr = String(bytes, Charsets.UTF_8)
                val packet = SurvivorPacket.fromJson(jsonStr)

                // Step 7: Drop loopback packet (originated from this device)
                val mySurvivorId = accountRepository.getOrCreateSurvivorId()
                if (packet.survivorId == mySurvivorId) {
                    Log.d(TAG, "DROPPED loopback packet from this device ($mySurvivorId)")
                    return
                }

                // Step 7: TTL check — drop if hopCount >= ttl
                if (packet.hopCount >= packet.ttl) {
                    Log.d(TAG, "DROPPED packet from ${packet.survivorId} — hopCount(${packet.hopCount}) >= ttl(${packet.ttl})")
                    return
                }

                // Step 7: Clock safety — reject if timestamp > 10 min in the future
                val now = System.currentTimeMillis()
                if (packet.timestamp > now + PacketDao.CLOCK_DRIFT_MAX_MS) {
                    Log.w(TAG, "REJECTED packet from ${packet.survivorId} — timestamp ${packet.timestamp} is >10 min ahead of now ($now)")
                    return
                }

                // Step 7: Increment hopCount for the relayed copy we store and forward
                val relayedPacket = packet.copy(hopCount = packet.hopCount + 1)

                Log.i(TAG, "==================================================")
                Log.i(TAG, "✓ PACKET RECEIVED from $endpointId")
                Log.i(TAG, "  Survivor  : ${relayedPacket.survivorName} (${relayedPacket.survivorId})")
                Log.i(TAG, "  Status    : ${relayedPacket.statusTag} (Priority: ${relayedPacket.priority})")
                Log.i(TAG, "  Location  : lat=${relayedPacket.latitude}, lon=${relayedPacket.longitude} (acc=${relayedPacket.locationAccuracy}m)")
                Log.i(TAG, "  Hop       : ${packet.hopCount} → ${relayedPacket.hopCount} / ttl=${relayedPacket.ttl}")
                Log.i(TAG, "  Timestamp : ${relayedPacket.timestamp}")
                Log.i(TAG, "==================================================")

                // Store in Room via PacketDao (newest-wins sync).
                // If the packet is NEW or NEWER than what we stored, relay it to all OTHER connected peers.
                ioScope.launch {
                    val wasStored = if (packetDao != null) {
                        packetDao.upsertIfNewer(PacketEntity.fromSurvivorPacket(relayedPacket), mySurvivorId)
                    } else {
                        _packetsCarriedCount.update { it + 1 }
                        true
                    }

                    if (wasStored) {
                        Log.i(TAG, "✓ Stored newer packet for ${relayedPacket.survivorId}. Checking relay...")
                        // Multi-hop gossip relay: forward to all OTHER connected peers (excluding sender)
                        if (relayedPacket.hopCount < relayedPacket.ttl) {
                            val otherPeers = _nearbyPeers.value.filter { it.peerId != endpointId }
                            if (otherPeers.isNotEmpty()) {
                                Log.i(TAG, "Gossip relay: forwarding to ${otherPeers.size} other peer(s)...")
                                for (peer in otherPeers) {
                                    sendSinglePacket(relayedPacket, peer.peerId)
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Discarded older/duplicate packet for ${relayedPacket.survivorId}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse received payload from $endpointId", e)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.d(TAG, "onPayloadTransferUpdate: payload transfer SUCCESS to/from $endpointId")
            } else if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                Log.w(TAG, "onPayloadTransferUpdate: payload transfer FAILED to/from $endpointId")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Connection lifecycle
    // -------------------------------------------------------------------------
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated: id=$endpointId  name=${info.endpointName}  incoming=${info.isIncomingConnection}")
            fallbackConnectionJobs.remove(endpointId)?.cancel()

            // Extract the display name (before the # separator).
            val displayName = info.endpointName.substringBefore(NAME_SEPARATOR)
            pendingNames[endpointId] = displayName

            // Always accept — both sides must call acceptConnection.
            Log.d(TAG, "Accepting connection from $endpointId ($displayName)")
            connectionsClient.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener {
                    Log.d(TAG, "acceptConnection queued OK for $endpointId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "acceptConnection FAILED for $endpointId", e)
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val code = result.status.statusCode
            Log.d(TAG, "onConnectionResult: id=$endpointId  success=${result.status.isSuccess}  code=$code  msg=${result.status.statusMessage}")
            pendingRequests.remove(endpointId)
            fallbackConnectionJobs.remove(endpointId)?.cancel()

            when {
                result.status.isSuccess -> {
                    val name = pendingNames.remove(endpointId) ?: "Peer"
                    Log.d(TAG, "✓ CONNECTED to $endpointId ($name)")
                    addPeer(endpointId, name)

                    // Step 7: Gossip sync — send ALL stored packets to the new peer
                    scope.launch {
                        delay(600)
                        gossipSyncTo(endpointId)
                    }
                }

                code == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.w(TAG, "Rejected by $endpointId — they may connect to us instead")
                    pendingNames.remove(endpointId)
                }

                code == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> {
                    val name = pendingNames.remove(endpointId) ?: "Peer"
                    Log.w(TAG, "Already connected to $endpointId — ensuring in peer list")
                    addPeer(endpointId, name)
                    scope.launch {
                        delay(600)
                        gossipSyncTo(endpointId)
                    }
                }

                else -> {
                    Log.e(TAG, "✗ Connection FAILED to $endpointId  code=$code")
                    pendingNames.remove(endpointId)
                    removePeer(endpointId)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "onDisconnected: $endpointId")
            pendingNames.remove(endpointId)
            pendingRequests.remove(endpointId)
            fallbackConnectionJobs.remove(endpointId)?.cancel()
            removePeer(endpointId)

            // In Nearby Connections, once an endpoint disconnects, Google Play Services
            // will not call onEndpointFound again for that endpoint unless discovery
            // is restarted. Restart discovery so we can reconnect when in range.
            if (_isBroadcastingBeacon.value) {
                rescan()
            }
        }
    }

    // -------------------------------------------------------------------------
    // Endpoint discovery callback
    // -------------------------------------------------------------------------
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound: id=$endpointId  name=${info.endpointName}  service=${info.serviceId}")

            // Skip if we already sent a request or are already connected.
            if (endpointId in pendingRequests) {
                Log.d(TAG, "Already have a pending request to $endpointId — skipping")
                return
            }
            if (_nearbyPeers.value.any { it.peerId == endpointId }) {
                Log.d(TAG, "Already connected to $endpointId — skipping")
                return
            }

            val displayName = info.endpointName.substringBefore(NAME_SEPARATOR)
            pendingNames[endpointId] = displayName

            // --- Tiebreaker to avoid symmetric collision ---
            // Both phones advertise their name as "ModelName#randomUuid".
            // We parse the remote UUID and compare with ours.
            // Device with the greater UUID initiates immediately.
            // Device with smaller UUID waits briefly for the other to initiate,
            // but initiates after a timeout if the other hasn't (prevents hang).
            val remoteUuid = info.endpointName.substringAfter(NAME_SEPARATOR, "")
            if (remoteUuid.isNotEmpty() && myUuid < remoteUuid) {
                Log.d(TAG, "Tiebreaker: myUuid=$myUuid < remoteUuid=$remoteUuid → WAIT briefly for peer")
                fallbackConnectionJobs[endpointId] = scope.launch {
                    delay(2500)
                    if (endpointId !in pendingRequests && _nearbyPeers.value.none { it.peerId == endpointId }) {
                        Log.d(TAG, "Fallback timer expired for $endpointId — initiating connection")
                        requestConnectionTo(endpointId)
                    }
                }
                return
            }
            Log.d(TAG, "Tiebreaker: myUuid=$myUuid >= remoteUuid=$remoteUuid → INITIATE")
            requestConnectionTo(endpointId)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "onEndpointLost: $endpointId")
            pendingRequests.remove(endpointId)
            fallbackConnectionJobs.remove(endpointId)?.cancel()
        }
    }

    private fun requestConnectionTo(endpointId: String) {
        if (endpointId in pendingRequests) return
        if (_nearbyPeers.value.any { it.peerId == endpointId }) return

        pendingRequests.add(endpointId)
        val myAdvertisedName = "${Build.MODEL}${NAME_SEPARATOR}${myUuid}"
        connectionsClient.requestConnection(
            myAdvertisedName,
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener {
            Log.d(TAG, "requestConnection sent to $endpointId")
        }.addOnFailureListener { e ->
            val statusCode = (e as? ApiException)?.statusCode
            if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                val name = pendingNames.remove(endpointId) ?: "Peer"
                Log.d(TAG, "Already connected to $endpointId — adding to peers")
                addPeer(endpointId, name)
            } else {
                Log.e(TAG, "requestConnection FAILED to $endpointId (code=$statusCode)", e)
            }
            pendingRequests.remove(endpointId)
        }
    }

    // -------------------------------------------------------------------------
    // Step 7: Gossip sync — send all stored packets to a newly connected peer
    // -------------------------------------------------------------------------

    /**
     * When a new peer connects, send them ALL packets we have stored in Room,
     * plus our own packet. This is the core of the gossip relay.
     *
     * The receiver applies newest-timestamp-wins, so duplicates are harmless —
     * it will only keep the newest version of each survivor.
     *
     * Send order: lowest priority number first (most urgent), then newest timestamp.
     * This matches the ORDER BY in PacketDao.getAllPacketsSync().
     */
    private fun gossipSyncTo(endpointId: String) {
        ioScope.launch {
            val isRescuer = accountRepository.userRole.value == UserRole.RESCUER

            // 1. Send our own packet first ONLY if this device is a SURVIVOR.
            // A Rescuer is a responder, not a trapped survivor, and must never broadcast an SOS beacon.
            if (!isRescuer) {
                val myPacket = survivorRepository?.mySurvivorPacket?.value ?: createOwnPacket()
                sendSinglePacket(myPacket, endpointId)
            }

            // 2. Send all stored packets from Room (already sorted by priority ASC, timestamp DESC)
            if (packetDao != null) {
                val stored = packetDao.getAllPacketsSync()
                Log.i(TAG, "gossipSyncTo($endpointId): sending ${stored.size} stored packets" + if (!isRescuer) " + own packet" else "")
                for (entity in stored) {
                    // Don't re-send our own survivorId (we already sent it above)
                    val mySurvivorId = accountRepository.getOrCreateSurvivorId()
                    if (entity.survivorId == mySurvivorId) continue

                    // Don't forward packets that have hit their TTL
                    if (entity.hopCount >= entity.ttl) {
                        Log.d(TAG, "gossipSync: skipping ${entity.survivorId} — hopCount(${entity.hopCount}) >= ttl(${entity.ttl})")
                        continue
                    }

                    val pkt = entity.toSurvivorPacket()
                    sendSinglePacket(pkt, endpointId)

                    // Small delay between packets to avoid overwhelming the BLE pipe
                    delay(50)
                }
            } else {
                Log.d(TAG, "gossipSyncTo($endpointId): no packetDao" + if (!isRescuer) " — only sent own packet" else "")
            }
        }
    }

    /** Send a single SurvivorPacket JSON to one endpoint. */
    private fun sendSinglePacket(packet: SurvivorPacket, endpointId: String) {
        val jsonBytes = packet.toJson().toByteArray(Charsets.UTF_8)
        val payload = Payload.fromBytes(jsonBytes)
        connectionsClient.sendPayload(endpointId, payload)
            .addOnSuccessListener {
                Log.d(TAG, "gossipSync: sent ${packet.survivorId} (hop=${packet.hopCount}) to $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "gossipSync: FAILED to send ${packet.survivorId} to $endpointId", e)
            }
    }

    // -------------------------------------------------------------------------
    // MeshRepository interface
    // -------------------------------------------------------------------------

    override fun toggleBroadcast(enable: Boolean?) {
        val willEnable = enable ?: !_isBroadcastingBeacon.value
        _isBroadcastingBeacon.value = willEnable
        accountRepository.saveIsBroadcasting(willEnable)
        if (willEnable) {
            startMesh()
        } else {
            stopMesh()
        }
    }

    override fun setBatterySaver(enabled: Boolean) {
        _isBatterySaverActive.value = enabled
    }

    override fun simulateIncomingPeer(peer: MeshPeer) {
        addPeer(peer.peerId, peer.alias)
    }

    override fun recordContactHop(node: HistoryNode) {
        _breadcrumbChain.update { it + node }
    }

    override fun rescan() {
        Log.d(TAG, "rescan() — restarting discovery")
        if (!_isBroadcastingBeacon.value) return
        connectionsClient.stopDiscovery()
        scope.launch {
            delay(200)
            if (!_isBroadcastingBeacon.value) return@launch
            val discoveryOptions = DiscoveryOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER)
                .build()
            connectionsClient.startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                discoveryOptions
            ).addOnSuccessListener {
                Log.d(TAG, "✓ Discovery restarted")
            }.addOnFailureListener { e ->
                val statusCode = (e as? ApiException)?.statusCode
                if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING) {
                    Log.d(TAG, "✓ Already discovering")
                } else {
                    Log.e(TAG, "✗ Discovery restart FAILED (code=$statusCode)", e)
                }
            }
        }
    }

    override fun sendPacket(packet: SurvivorPacket?, targetEndpointId: String?) {
        val isRescuer = accountRepository.userRole.value == UserRole.RESCUER
        val pkt = packet ?: if (!isRescuer) (survivorRepository?.mySurvivorPacket?.value ?: createOwnPacket()) else null
        if (pkt == null) {
            Log.d(TAG, "sendPacket: Rescuer device has no default own packet to send")
            return
        }
        val jsonBytes = pkt.toJson().toByteArray(Charsets.UTF_8)
        val payload = Payload.fromBytes(jsonBytes)

        val targets = if (targetEndpointId != null) {
            listOf(targetEndpointId)
        } else {
            _nearbyPeers.value.map { it.peerId }
        }

        if (targets.isEmpty()) {
            Log.w(TAG, "sendPacket: No connected peers to send to")
            return
        }

        for (endpointId in targets) {
            Log.d(TAG, "sendPacket: sending packet '${pkt.packetId}' to $endpointId (${jsonBytes.size} bytes)")
            connectionsClient.sendPayload(endpointId, payload)
                .addOnSuccessListener {
                    Log.i(TAG, "✓ Packet '${pkt.packetId}' (${pkt.survivorName}) sent to $endpointId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "✗ Failed to send packet to $endpointId", e)
                }
        }
    }

    /** Create this device's own SurvivorPacket (hopCount=0, fresh timestamp).
     *  Step 8: includes current status and GPS location from survivorRepository. */
    private fun createOwnPacket(): SurvivorPacket {
        val myMedical = accountRepository.medicalRecord.value
        val mySurvivorId = accountRepository.getOrCreateSurvivorId()
        val currentLoc = survivorRepository?.mySurvivorPacket?.value
        val status = currentLoc?.statusTag ?: UrgencyStatus.UNKNOWN
        return SurvivorPacket(
            packetId = "PKT-${UUID.randomUUID().toString().take(6).uppercase()}",
            survivorId = mySurvivorId,
            survivorName = "${myMedical.name} (${Build.MODEL})",
            statusTag = status,
            medicalData = myMedical,
            timestamp = System.currentTimeMillis(),
            priority = status.priorityLevel,
            hopCount = 0,
            ttl = 10,
            sectorCode = "SEC-4B",
            latitude = currentLoc?.latitude ?: 0.0,
            longitude = currentLoc?.longitude ?: 0.0,
            locationAccuracy = currentLoc?.locationAccuracy ?: 0f,
            locationCapturedAt = currentLoc?.locationCapturedAt ?: 0L
        )
    }

    // -------------------------------------------------------------------------
    // Internal mesh start / stop
    // -------------------------------------------------------------------------

    private fun startMesh() {
        if (isStartingMesh || isMeshRunning) {
            Log.d(TAG, "startMesh() already running or in progress — skipping")
            return
        }
        isStartingMesh = true
        Log.d(TAG, "startMesh() — starting mesh session")

        pendingNames.clear()
        pendingRequests.clear()
        fallbackConnectionJobs.values.forEach { it.cancel() }
        fallbackConnectionJobs.clear()

        // Clean up any stale sessions in Google Play Services before starting fresh
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()

        scope.launch {
            delay(200)
            isStartingMesh = false
            if (!_isBroadcastingBeacon.value) {
                Log.d(TAG, "Broadcasting was disabled while stopping — aborting start")
                return@launch
            }

            val myAdvertisedName = "${Build.MODEL}${NAME_SEPARATOR}${myUuid}"
            Log.d(TAG, "startMesh() — name='$myAdvertisedName'  serviceId=$serviceId")

            // Advertise so other phones can find us.
            val advertisingOptions = AdvertisingOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER)
                .build()

            connectionsClient.startAdvertising(
                myAdvertisedName,
                serviceId,
                connectionLifecycleCallback,
                advertisingOptions
            ).addOnSuccessListener {
                Log.d(TAG, "✓ Advertising started successfully")
            }.addOnFailureListener { e ->
                val statusCode = (e as? ApiException)?.statusCode
                if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_ADVERTISING) {
                    Log.d(TAG, "✓ Already advertising")
                } else {
                    Log.e(TAG, "✗ Advertising FAILED (code=$statusCode)", e)
                }
            }

            // Discover other phones.
            val discoveryOptions = DiscoveryOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER)
                .build()

            connectionsClient.startDiscovery(
                serviceId,
                endpointDiscoveryCallback,
                discoveryOptions
            ).addOnSuccessListener {
                Log.d(TAG, "✓ Discovery started successfully")
                isMeshRunning = true
            }.addOnFailureListener { e ->
                val statusCode = (e as? ApiException)?.statusCode
                if (statusCode == ConnectionsStatusCodes.STATUS_ALREADY_DISCOVERING) {
                    Log.d(TAG, "✓ Already discovering")
                    isMeshRunning = true
                } else {
                    Log.e(TAG, "✗ Discovery FAILED (code=$statusCode), auto-retrying in 800ms", e)
                    isMeshRunning = false
                    scope.launch {
                        delay(800)
                        if (_isBroadcastingBeacon.value && !isMeshRunning) {
                            rescan()
                        }
                    }
                }
            }
        }
    }

    private fun stopMesh() {
        Log.d(TAG, "stopMesh()")
        isMeshRunning = false
        fallbackConnectionJobs.values.forEach { it.cancel() }
        fallbackConnectionJobs.clear()
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        pendingNames.clear()
        pendingRequests.clear()
        _nearbyPeers.value = emptyList()
    }

    // -------------------------------------------------------------------------
    // Peer list helpers
    // -------------------------------------------------------------------------

    private fun addPeer(id: String, name: String) {
        _nearbyPeers.update { current ->
            if (current.any { it.peerId == id }) {
                Log.d(TAG, "addPeer: $id already in list — skipping")
                return@update current
            }
            Log.d(TAG, "addPeer: $id ($name) → total=${current.size + 1}")
            current + MeshPeer(
                peerId = id,
                alias = name,
                rssi = -65,
                packetsCarried = 0,
                isDirectConnection = true,
                lastSyncAgoSec = 0
            )
        }
    }

    private fun removePeer(id: String) {
        _nearbyPeers.update { current ->
            val updated = current.filter { it.peerId != id }
            Log.d(TAG, "removePeer: $id → total=${updated.size}")
            updated
        }
    }
}
