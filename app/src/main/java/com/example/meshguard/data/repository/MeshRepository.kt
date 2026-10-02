package com.example.meshguard.data.repository

import com.example.meshguard.data.model.HistoryNode
import com.example.meshguard.data.model.MeshPeer
import com.example.meshguard.data.model.SurvivorPacket
import kotlinx.coroutines.flow.StateFlow

interface MeshRepository {
    val nearbyPeers: StateFlow<List<MeshPeer>>
    val packetsCarriedCount: StateFlow<Int>
    val isBroadcastingBeacon: StateFlow<Boolean>
    val isBatterySaverActive: StateFlow<Boolean>
    val breadcrumbChain: StateFlow<List<HistoryNode>>

    fun toggleBroadcast(enable: Boolean? = null)
    fun setBatterySaver(enabled: Boolean)
    fun simulateIncomingPeer(peer: MeshPeer)
    fun recordContactHop(node: HistoryNode)
    fun rescan()
    fun sendPacket(packet: SurvivorPacket? = null, targetEndpointId: String? = null)
}
