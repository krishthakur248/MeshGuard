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

    private val _uiState = MutableStateFlow(
        BreadcrumbChainUiState(
            chainHops = listOf(
                TimelineHopItem(
                    id = "node-0",
                    deviceAlias = "You (Local Device)",
                    roleBadge = "ORIGIN SOURCE",
                    hopIndex = 0,
                    timeAgoFormatted = "Just now",
                    timestampExact = timeFormatter.format(Date(System.currentTimeMillis())),
                    rssi = -52,
                    isLocalDevice = true,
                    notes = "Distress packet emitted via BLE 20dBm chirp"
                ),
                TimelineHopItem(
                    id = "node-1",
                    deviceAlias = "Device-7 (Mule Relay #04)",
                    roleBadge = "DATA MULE",
                    hopIndex = 1,
                    timeAgoFormatted = "6 min ago",
                    timestampExact = timeFormatter.format(Date(System.currentTimeMillis() - 360000L)),
                    rssi = -64,
                    notes = "Carried across 4th Avenue debris zone"
                ),
                TimelineHopItem(
                    id = "node-2",
                    deviceAlias = "Rescue Command Base (Sector 4)",
                    roleBadge = "RESCUER DESTINATION",
                    hopIndex = 2,
                    timeAgoFormatted = "32 min ago",
                    timestampExact = timeFormatter.format(Date(System.currentTimeMillis() - 1920000L)),
                    rssi = -58,
                    isDestination = true,
                    notes = "Packet ingested into Incident Commander Triage Dashboard"
                )
            )
        )
    )

    val uiState: StateFlow<BreadcrumbChainUiState> = _uiState.asStateFlow()

    fun onRefreshProximityChain() {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingChain = true) }
            delay(1200L)
            _uiState.update { it.copy(isVerifyingChain = false) }
        }
    }
}
