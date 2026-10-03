package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.MeshPeer
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.data.repository.ChatRepository
import com.example.meshguard.data.repository.LocationProvider
import com.example.meshguard.data.repository.MeshRepository
import com.example.meshguard.data.repository.SurvivorRepository
import com.example.meshguard.service.MeshForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val myStatus: UrgencyStatus = UrgencyStatus.UNKNOWN,
    val isBroadcastingBeacon: Boolean = true,
    val isBatterySaverActive: Boolean = true,
    val activePeerCount: Int = 0,
    val packetsCarriedCount: Int = 0,
    val lastSyncAgoSec: Int = 4,
    val unreadMessageCount: Int = 0,
    val sectorCode: String = "SEC-4B",
    val nearbyPeersPreview: List<MeshPeer> = emptyList(),
    val survivorName: String = "Alex Rivera",
    val survivorAge: Int = 28,
    val profileWarningMessage: String? = null
)

class HomeViewModel @JvmOverloads constructor(
    private val survivorRepository: SurvivorRepository = AppDependencies.survivorRepository,
    private val meshRepository: MeshRepository = AppDependencies.meshRepository,
    private val chatRepository: ChatRepository = AppDependencies.chatRepository,
    private val locationProvider: LocationProvider = AppDependencies.locationProvider
) : ViewModel() {

    init {
        if (meshRepository.isBroadcastingBeacon.value) {
            locationProvider.startTracking()
        }
    }

    private val _warningMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        survivorRepository.mySurvivorPacket,
        combine(
            meshRepository.isBroadcastingBeacon,
            meshRepository.isBatterySaverActive,
            meshRepository.nearbyPeers,
            meshRepository.packetsCarriedCount
        ) { isBroadcasting, isEco, peers, carriedCount ->
            listOf(isBroadcasting, isEco, peers, carriedCount)
        },
        combine(chatRepository.messages, _warningMessage) { msgs, warning -> msgs to warning }
    ) { myPacket, meshData, (messages, warning) ->
        val isBroadcasting = meshData[0] as Boolean
        val isEco = meshData[1] as Boolean
        @Suppress("UNCHECKED_CAST")
        val peers = meshData[2] as List<MeshPeer>
        val carriedCount = meshData[3] as Int

        val mostRecentSync = peers.minOfOrNull { it.lastSyncAgoSec } ?: 0
        val unread = messages.count { !it.isFromMe }

        HomeUiState(
            myStatus = myPacket.statusTag,
            isBroadcastingBeacon = isBroadcasting,
            isBatterySaverActive = isEco,
            activePeerCount = peers.size,
            packetsCarriedCount = carriedCount,
            lastSyncAgoSec = mostRecentSync,
            unreadMessageCount = unread,
            sectorCode = myPacket.sectorCode,
            nearbyPeersPreview = peers.take(3),
            survivorName = myPacket.medicalData.name,
            survivorAge = myPacket.medicalData.age,
            profileWarningMessage = warning
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onToggleBeacon() {
        val currentPacket = survivorRepository.mySurvivorPacket.value
        val name = currentPacket.medicalData.name.trim()
        val age = currentPacket.medicalData.age

        if (name.isBlank() || age <= 0) {
            _warningMessage.value = "Please complete Name and Age in Medical ID before broadcasting SOS."
            meshRepository.toggleBroadcast(false)
            // Step 9: Stop the foreground service if the broadcast is being turned off due to
            // profile validation failure.
            MeshForegroundService.stop(AppDependencies.appContext)
        } else {
            _warningMessage.value = null
            meshRepository.toggleBroadcast()

            // Step 8: Start/stop GPS location tracking with the broadcast toggle
            if (meshRepository.isBroadcastingBeacon.value) {
                locationProvider.startTracking()
                // Step 9: User turned broadcasting ON — start the foreground service so the
                // mesh keeps running even if they press Home right after.
                MeshForegroundService.start(AppDependencies.appContext)
            } else {
                locationProvider.stopTracking()
                // Step 9: User pressed Pause / turned broadcasting OFF — stop the service.
                MeshForegroundService.stop(AppDependencies.appContext)
            }
        }
    }

    fun onToggleBatterySaver(enabled: Boolean) {
        meshRepository.setBatterySaver(enabled)
    }

    fun onQuickStatusSelected(status: UrgencyStatus) {
        survivorRepository.updateMyStatus(status)
    }

    fun onDismissWarning() {
        _warningMessage.value = null
    }
}
