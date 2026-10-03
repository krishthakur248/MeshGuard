package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.repository.SurvivorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SurvivorDetailUiState(
    val survivor: SurvivorPacket? = null,
    val isLoading: Boolean = true,
    val isAcknowledged: Boolean = false
)

/**
 * Step 10: ViewModel for the Survivor Detail Screen.
 * Automatically updates if a newer packet for this survivor arrives via gossip relay.
 */
class SurvivorDetailViewModel @JvmOverloads constructor(
    private val survivorRepository: SurvivorRepository = AppDependencies.survivorRepository
) : ViewModel() {

    private val _survivorId = MutableStateFlow("")

    fun loadSurvivor(survivorId: String) {
        _survivorId.value = survivorId
    }

    val uiState: StateFlow<SurvivorDetailUiState> = combine(
        survivorRepository.triagedSurvivors,
        _survivorId
    ) { survivors, id ->
        val found = survivors.firstOrNull { it.survivorId == id }
            ?: survivorRepository.getSurvivorById(id)

        SurvivorDetailUiState(
            survivor = found,
            isLoading = id.isBlank(),
            isAcknowledged = found?.isAcknowledged ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SurvivorDetailUiState()
    )

    fun markAcknowledged(survivorId: String) {
        viewModelScope.launch {
            survivorRepository.markSurvivorAcknowledged(survivorId)
        }
    }
}
