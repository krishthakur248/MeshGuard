package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.data.repository.SurvivorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatusPickerUiState(
    val initialStatus: UrgencyStatus = UrgencyStatus.UNKNOWN,
    val selectedStatus: UrgencyStatus = UrgencyStatus.UNKNOWN,
    val sectorCode: String = "SEC-4B",
    val isConfirmed: Boolean = false
)

class StatusPickerViewModel(
    private val survivorRepository: SurvivorRepository = AppDependencies.survivorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusPickerUiState())
    val uiState: StateFlow<StatusPickerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            survivorRepository.mySurvivorPacket.collect { packet ->
                _uiState.update { current ->
                    if (current.selectedStatus == current.initialStatus) {
                        current.copy(
                            initialStatus = packet.statusTag,
                            selectedStatus = packet.statusTag,
                            sectorCode = packet.sectorCode
                        )
                    } else {
                        current.copy(
                            initialStatus = packet.statusTag,
                            sectorCode = packet.sectorCode
                        )
                    }
                }
            }
        }
    }

    fun onSelectStatus(status: UrgencyStatus) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun onConfirmStatus() {
        val selected = _uiState.value.selectedStatus
        survivorRepository.updateMyStatus(selected)
        _uiState.update { it.copy(isConfirmed = true) }
    }
}
