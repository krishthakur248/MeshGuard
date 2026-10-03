package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.HistoryNode
import com.example.meshguard.data.model.MeshPeer
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.data.repository.MeshRepository
import com.example.meshguard.data.repository.SurvivorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MeshSyncLogEntry(
    val id: String,
    val timestampFormatted: String,
    val peerAlias: String,
    val eventType: String,
    val packetCount: Int,
    val rssi: Int,
    val isOutbound: Boolean
)

data class MeshNetworkUiState(
    val peers: List<MeshPeer> = emptyList(),
    val packetsCarriedCount: Int = 0,
    val isBroadcasting: Boolean = true,
    val isBatterySaverActive: Boolean = true,
    val breadcrumbs: List<HistoryNode> = emptyList(),
    val syncLogs: List<MeshSyncLogEntry> = emptyList(),
    val isScanning: Boolean = false,
    val trappedCount: Int = 0,
    val injuredCount: Int = 0,
    val needsMedsCount: Int = 0,
    val safeCount: Int = 0,
    val maxBufferCapacity: Int = 500
)

class MeshNetworkViewModel(
    private val meshRepository: MeshRepository = AppDependencies.meshRepository,
    private val survivorRepository: SurvivorRepository = AppDependencies.survivorRepository
) : ViewModel() {

    init {
        if (meshRepository.isBroadcastingBeacon.value) {
            meshRepository.toggleBroadcast(true)
        }
    }

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _syncLogs = MutableStateFlow(
        listOf(
            MeshSyncLogEntry(
                id = "log-1",
                timestampFormatted = timeFormatter.format(Date(System.currentTimeMillis() - 4000L)),
                peerAlias = "Mule Relay #04",
                eventType = "BLE GATT SYNC COMPLETE",
                packetCount = 6,
                rssi = -64,
                isOutbound = false
            ),
            MeshSyncLogEntry(
                id = "log-2",
                timestampFormatted = timeFormatter.format(Date(System.currentTimeMillis() - 11000L)),
                peerAlias = "Search Team Echo",
                eventType = "DISTRESS BEACON FORWARDED",
                packetCount = 1,
                rssi = -72,
                isOutbound = true
            )
        )
    )

    private val _isScanning = MutableStateFlow(false)

    val uiState: StateFlow<MeshNetworkUiState> = combine(
        combine(
            meshRepository.nearbyPeers,
            meshRepository.isBroadcastingBeacon,
            meshRepository.isBatterySaverActive,
            meshRepository.packetsCarriedCount
        ) { peers, broadcasting, eco, carriedCount ->
            listOf(peers, broadcasting, eco, carriedCount)
        },
        survivorRepository.triagedSurvivors,
        combine(_syncLogs, _isScanning) { logs, scanning -> logs to scanning }
    ) { meshData, survivors, (logs, scanning) ->
        @Suppress("UNCHECKED_CAST")
        val peers = meshData[0] as List<MeshPeer>
        val broadcasting = meshData[1] as Boolean
        val eco = meshData[2] as Boolean
        val carriedCount = meshData[3] as Int

        val trapped = survivors.count { it.statusTag == UrgencyStatus.TRAPPED }
        val injured = survivors.count { it.statusTag == UrgencyStatus.INJURED }
        val meds = survivors.count { it.statusTag == UrgencyStatus.NEEDS_INSULIN || it.statusTag == UrgencyStatus.NEED_WATER }
        val safe = survivors.count { it.statusTag == UrgencyStatus.SAFE }

        MeshNetworkUiState(
            peers = peers,
            packetsCarriedCount = carriedCount,
            isBroadcasting = broadcasting,
            isBatterySaverActive = eco,
            breadcrumbs = emptyList(),
            syncLogs = logs,
            isScanning = scanning,
            trappedCount = trapped,
            injuredCount = injured,
            needsMedsCount = meds,
            safeCount = safe,
            maxBufferCapacity = 500
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MeshNetworkUiState()
    )

    fun onToggleBroadcast() {
        meshRepository.toggleBroadcast()
    }

    fun onForceRescan() {
        meshRepository.rescan()
        viewModelScope.launch {
            _isScanning.value = true
            delay(1500)
            _isScanning.value = false

            val newLog = MeshSyncLogEntry(
                id = "log-${System.currentTimeMillis()}",
                timestampFormatted = timeFormatter.format(Date()),
                peerAlias = "Radio Discovery Engine",
                eventType = "SCAN CYCLE COMPLETED (${uiState.value.peers.size} PEERS FOUND)",
                packetCount = 0,
                rssi = -60,
                isOutbound = false
            )
            _syncLogs.update { listOf(newLog) + it.take(19) }
        }
    }

    fun onPeerClicked(peer: MeshPeer) {
        meshRepository.sendPacket(targetEndpointId = peer.peerId)
        val log = MeshSyncLogEntry(
            id = "log-${System.currentTimeMillis()}",
            timestampFormatted = timeFormatter.format(Date()),
            peerAlias = peer.alias,
            eventType = "PACKET DISPATCHED TO PEER",
            packetCount = 1,
            rssi = peer.rssi,
            isOutbound = true
        )
        _syncLogs.update { listOf(log) + it.take(19) }
    }
}
