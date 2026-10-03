package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.repository.MeshRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TimelineHopItem(
    val id: String,
    val deviceAlias: String,
    val roleBadge: String,
    val hopIndex: Int,
    val timeAgoFormatted: String,
    val timestampExact: String,
    val rssi: Int,
    val isAnchorOrBeacon: Boolean = false,
    val isDestination: Boolean = false,
    val isLocalDevice: Boolean = false,
    val notes: String
)

data class BreadcrumbChainUiState(
    val originSector: String = "SEC-4B",
    val destinationSector: String = "SEC-4 (Safe Haven)",
    val totalHops: Int = 3,
    val totalTimeSpanMinutes: Int = 32,
    val estimatedDistanceMeters: Int = 450,
    val chainHops: List<TimelineHopItem> = emptyList(),
    val isVerifyingChain: Boolean = false,
    val isRelayHealthy: Boolean = true
)

class BreadcrumbChainViewModel(
    private val meshRepository: MeshRepository = AppDependencies.meshRepository
) : ViewModel() {

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _uiState = MutableStateFlow(buildDefaultUiState())
    val uiState: StateFlow<BreadcrumbChainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            meshRepository.breadcrumbChain.collect { hops ->
                if (hops.isNotEmpty()) {
                    val timelineItems = mutableListOf<TimelineHopItem>()
                    timelineItems.add(
                        TimelineHopItem(
                            id = "local-origin",
                            deviceAlias = "You (Local Device)",
                            roleBadge = "ORIGIN SOURCE",
                            hopIndex = 0,
                            timeAgoFormatted = "Origin",
                            timestampExact = timeFormatter.format(Date(hops.firstOrNull()?.lastSeenTimestamp ?: System.currentTimeMillis())),
                            rssi = -48,
                            isLocalDevice = true,
                            notes = "Local distress beacon created on this phone"
                        )
                    )

                    hops.forEachIndexed { index, node ->
                        val isLast = index == hops.lastIndex
                        val diffMinutes = ((System.currentTimeMillis() - node.lastSeenTimestamp) / 60000L).coerceAtLeast(0)
                        timelineItems.add(
                            TimelineHopItem(
                                id = "hop-${index + 1}",
                                deviceAlias = node.deviceAlias.ifBlank { "Relay Mule #${index + 1}" },
                                roleBadge = if (isLast) "LATEST RELAY" else "DATA MULE",
                                hopIndex = index + 1,
                                timeAgoFormatted = if (diffMinutes == 0L) "Just now" else "$diffMinutes min ago",
                                timestampExact = timeFormatter.format(Date(node.lastSeenTimestamp)),
                                rssi = node.signalStrengthRssi,
                                isDestination = isLast,
                                notes = "Relayed hop #${index + 1} over peer Bluetooth LE"
                            )
                        )
                    }

                    _uiState.update { current ->
                        current.copy(
                            totalHops = hops.size,
                            chainHops = timelineItems,
                            isRelayHealthy = true
                        )
                    }
                }
            }
        }
    }

    private fun buildDefaultUiState(): BreadcrumbChainUiState {
        val now = System.currentTimeMillis()
        val defaultItems = listOf(
            TimelineHopItem(
                id = "node-0",
                deviceAlias = "You (Local Device)",
                roleBadge = "ORIGIN SOURCE",
                hopIndex = 0,
                timeAgoFormatted = "Just now",
                timestampExact = timeFormatter.format(Date(now)),
                rssi = -50,
                isLocalDevice = true,
                notes = "Local distress beacon broadcasting over Nearby BLE"
            )
        )
        return BreadcrumbChainUiState(
            originSector = "SEC-4B",
            destinationSector = "SEC-4 (Safe Haven)",
            totalHops = 0,
            totalTimeSpanMinutes = 0,
            estimatedDistanceMeters = 50,
            chainHops = defaultItems,
            isVerifyingChain = false,
            isRelayHealthy = true
        )
    }

    fun onRefreshProximityChain() {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingChain = true) }
            meshRepository.rescan()
            delay(1000L)
            _uiState.update { it.copy(isVerifyingChain = false) }
        }
    }
}
