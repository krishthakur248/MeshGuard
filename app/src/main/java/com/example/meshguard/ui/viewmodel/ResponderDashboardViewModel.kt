package com.example.meshguard.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.data.repository.MeshRepository
import com.example.meshguard.data.repository.SurvivorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Filter tags for the Rescuer triage casualty queue.
 */
enum class UrgencyFilter(val label: String, val targetStatus: UrgencyStatus?) {
    ALL("ALL", null),
    TRAPPED("TRAPPED", UrgencyStatus.TRAPPED),
    INJURED("INJURED", UrgencyStatus.INJURED),
    NEEDS_INSULIN("MEDS", UrgencyStatus.NEEDS_INSULIN),
    UNKNOWN("UNKNOWN", UrgencyStatus.UNKNOWN),
    NEED_WATER("WATER", UrgencyStatus.NEED_WATER),
    SAFE("SAFE", UrgencyStatus.SAFE)
}

data class ResponderDashboardUiState(
    val selectedFilter: UrgencyFilter = UrgencyFilter.ALL,
    val searchQuery: String = "",
    val allSurvivors: List<SurvivorPacket> = emptyList(),
    val activeSurvivors: List<SurvivorPacket> = emptyList(),
    val acknowledgedSurvivors: List<SurvivorPacket> = emptyList(),
    val criticalCount: Int = 0,
    val totalCount: Int = 0,
    val peerCount: Int = 0,
    val packetsCarriedCount: Int = 0
)

/**
 * Step 10: ViewModel for the Rescuer Triage Dashboard.
 * Feeds live Room database packets from RoomSurvivorRepository.triagedSurvivors,
 * enforces priority/timestamp sorting, splits into active and acknowledged sections,
 * and provides clear queue capability.
 */
class ResponderDashboardViewModel @JvmOverloads constructor(
    private val survivorRepository: SurvivorRepository = AppDependencies.survivorRepository,
    private val meshRepository: MeshRepository = AppDependencies.meshRepository
) : ViewModel() {

    init {
        // Ensure the mesh radio is running whenever the Triage Dashboard is open.
        // This mirrors MeshNetworkViewModel's init and fixes the bug where nearby
        // devices were only discovered after navigating to the Mesh Network tab.
        Log.d("ResponderDashboardVM", "init: ensuring mesh radio is active and rescanning")
        if (meshRepository.isBroadcastingBeacon.value) {
            // Radio is already supposed to be on — just kick a fresh discovery cycle
            // in case advertising/discovery stalled after onResume.
            meshRepository.rescan()
        } else {
            // Radio was off — turn it on (Rescuers should always be discovering).
            meshRepository.toggleBroadcast(true)
        }
    }

    private val _selectedFilter = MutableStateFlow(UrgencyFilter.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ResponderDashboardUiState> = combine(
        survivorRepository.triagedSurvivors,
        meshRepository.nearbyPeers,
        meshRepository.packetsCarriedCount,
        _selectedFilter,
        _searchQuery
    ) { survivors, peers, carried, filter, query ->
        // Packets are already sorted by priority ASC, timestamp DESC from Room PacketDao.
        val filtered = survivors.filter { survivor ->
            val matchesFilter = filter.targetStatus == null || survivor.statusTag == filter.targetStatus
            val matchesQuery = query.isBlank() ||
                survivor.survivorName.contains(query, ignoreCase = true) ||
                survivor.survivorId.contains(query, ignoreCase = true) ||
                survivor.sectorCode.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }

        val active = filtered.filter { !it.isAcknowledged }
        val acknowledged = filtered.filter { it.isAcknowledged }

        val critical = active.count {
            it.statusTag == UrgencyStatus.TRAPPED || it.statusTag == UrgencyStatus.INJURED
        }

        ResponderDashboardUiState(
            selectedFilter = filter,
            searchQuery = query,
            allSurvivors = survivors,
            activeSurvivors = active,
            acknowledgedSurvivors = acknowledged,
            criticalCount = critical,
            totalCount = survivors.size,
            peerCount = peers.size,
            packetsCarriedCount = carried
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ResponderDashboardUiState()
    )

    fun onFilterSelected(filter: UrgencyFilter) {
        _selectedFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun markAcknowledged(survivorId: String) {
        viewModelScope.launch {
            survivorRepository.markSurvivorAcknowledged(survivorId)
        }
    }

    fun clearQueue() {
        viewModelScope.launch {
            survivorRepository.clearAllPackets()
        }
    }
}
