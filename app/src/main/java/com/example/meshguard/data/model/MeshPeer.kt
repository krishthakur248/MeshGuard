package com.example.meshguard.data.model

/**
 * Discovered radio peer in mesh network.
 */
data class MeshPeer(
    val peerId: String = "",
    val alias: String = "",
    val rssi: Int = -70,
    val packetsCarried: Int = 0,
    val isDirectConnection: Boolean = true,
    val lastSyncAgoSec: Int = 0
)
