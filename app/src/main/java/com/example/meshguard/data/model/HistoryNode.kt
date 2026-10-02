package com.example.meshguard.data.model

/**
 * Single hop entry in breadcrumb chain.
 */
data class HistoryNode(
    val deviceAlias: String = "",
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val signalStrengthRssi: Int = -70,
    val hopIndex: Int = 0
)
