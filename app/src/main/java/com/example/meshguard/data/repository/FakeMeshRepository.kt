package com.example.meshguard.data.repository

import com.example.meshguard.data.model.HistoryNode
import com.example.meshguard.data.model.MeshPeer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeMeshRepository : MeshRepository {

    private val _nearbyPeers = MutableStateFlow<List<MeshPeer>>(
        listOf(
            MeshPeer(
                peerId = "peer-alpha-01",
                alias = "Mule Relay #04",
                rssi = -64,
                packetsCarried = 24,
                isDirectConnection = true,
                lastSyncAgoSec = 4
            ),
            MeshPeer(
                peerId = "peer-bravo-02",
                alias = "Search Team Echo",
                rssi = -72,
                packetsCarried = 58,
                isDirectConnection = true,
                lastSyncAgoSec = 11
            ),
            MeshPeer(
                peerId = "peer-charlie-03",
                alias = "Citizen Node #C9",
                rssi = -86,
                packetsCarried = 6,
                isDirectConnection = false,
                lastSyncAgoSec = 35
            )
        )
    )
    override val nearbyPeers: StateFlow<List<MeshPeer>> = _nearbyPeers.asStateFlow()

    private val _packetsCarriedCount = MutableStateFlow(28)
    override val packetsCarriedCount: StateFlow<Int> = _packetsCarriedCount.asStateFlow()

    private val _isBroadcastingBeacon = MutableStateFlow(true)
    override val isBroadcastingBeacon: StateFlow<Boolean> = _isBroadcastingBeacon.asStateFlow()

    private val _isBatterySaverActive = MutableStateFlow(true)
    override val isBatterySaverActive: StateFlow<Boolean> = _isBatterySaverActive.asStateFlow()

    private val _breadcrumbChain = MutableStateFlow<List<HistoryNode>>(
        listOf(
            HistoryNode(
                deviceAlias = "Stationary Beacon SEC-4B",
                lastSeenTimestamp = System.currentTimeMillis() - 1800000L,
                signalStrengthRssi = -55,
                hopIndex = 0
            ),
            HistoryNode(
                deviceAlias = "Volunteer Mule #12",
                lastSeenTimestamp = System.currentTimeMillis() - 900000L,
                signalStrengthRssi = -68,
                hopIndex = 1
            ),
            HistoryNode(
                deviceAlias = "This Device (Local Gateway)",
                lastSeenTimestamp = System.currentTimeMillis(),
                signalStrengthRssi = -61,
                hopIndex = 2
            )
        )
    )
    override val breadcrumbChain: StateFlow<List<HistoryNode>> = _breadcrumbChain.asStateFlow()

    override fun toggleBroadcast(enable: Boolean?) {
        _isBroadcastingBeacon.update { current -> enable ?: !current }
    }

    override fun setBatterySaver(enabled: Boolean) {
        _isBatterySaverActive.value = enabled
    }

    override fun simulateIncomingPeer(peer: MeshPeer) {
        _nearbyPeers.update { currentList ->
            listOf(peer) + currentList.filterNot { it.peerId == peer.peerId }
        }
        _packetsCarriedCount.update { it + peer.packetsCarried }
    }

    override fun recordContactHop(node: HistoryNode) {
        _breadcrumbChain.update { it + node }
    }

    override fun rescan() {
        // No-op for preview/testing
    }

    override fun sendPacket(packet: com.example.meshguard.data.model.SurvivorPacket?, targetEndpointId: String?) {
        _packetsCarriedCount.update { it + 1 }
    }
}
