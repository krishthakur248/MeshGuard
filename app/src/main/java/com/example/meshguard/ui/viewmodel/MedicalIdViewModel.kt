package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meshguard.AppConfig
import com.example.meshguard.AppDependencies
import com.example.meshguard.data.model.MedicalRecord
import com.example.meshguard.data.repository.MedicalIdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicalIdUiState(
    val record: MedicalRecord = MedicalRecord(),
    val isLocked: Boolean = true,
    val isEditing: Boolean = false,
    val isRescuerSimulatedInRange: Boolean = false,
    val nameInput: String = "Alex Rivera",
    val ageInput: String = "28",
    val bloodTypeInput: String = "O+",
    val allergiesInput: List<String> = listOf("Penicillin", "Latex"),
    val conditionsInput: List<String> = listOf("Type-1 Diabetes", "Asthma"),
    val emergencyContactNameInput: String = "Sarah Rivera",
    val emergencyContactRelationInput: String = "Spouse (Phone: +1 555-0192)",
    val newAllergyText: String = "",
    val newConditionText: String = "",
    val rescuerAuthCodeInput: String = AppConfig.RESCUER_ACCESS_CODE_DEMO,
    val unlockErrorMessage: String? = null,
    val isSavedSuccess: Boolean = false
)

class MedicalIdViewModel(
    private val medicalIdRepository: MedicalIdRepository = AppDependencies.medicalIdRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalIdUiState())
    val uiState: StateFlow<MedicalIdUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            medicalIdRepository.medicalRecord.collect { record ->
                _uiState.update { current ->
                    current.copy(
                        record = record,
                        isLocked = record.isEncrypted,
                        nameInput = record.name,
                        ageInput = record.age.toString(),
                        bloodTypeInput = record.bloodType,
                        allergiesInput = record.allergies,
                        conditionsInput = record.chronicConditions,
                        emergencyContactNameInput = record.emergencyContactName,
                        emergencyContactRelationInput = record.emergencyContactRelation
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(nameInput = name) }
    }

    fun onAgeChanged(age: String) {
        _uiState.update { it.copy(ageInput = age.filter { char -> char.isDigit() }) }
    }

    fun onSimulateRescuerToggle(simulatedInRange: Boolean) {
        _uiState.update { current ->
            current.copy(
                isRescuerSimulatedInRange = simulatedInRange,
                unlockErrorMessage = null
            )
        }
        if (simulatedInRange) {
            val unlocked = medicalIdRepository.unlockWithRescuerToken(_uiState.value.rescuerAuthCodeInput)
            if (unlocked) {
                _uiState.update { it.copy(isLocked = false) }
            }
        } else {
            medicalIdRepository.toggleEncryption(true)
            _uiState.update { it.copy(isLocked = true, isEditing = false) }
        }
    }

    fun onToggleEditMode(enable: Boolean) {
        if (_uiState.value.isLocked && enable) {
            _uiState.update { it.copy(unlockErrorMessage = "Medical record is encrypted. Unlock with rescuer token first.") }
            return
        }
        _uiState.update { 
            it.copy(
                isEditing = enable,
                unlockErrorMessage = null,
                isSavedSuccess = false
            )
        }
    }

    fun onBloodTypeSelected(bloodType: String) {
        _uiState.update { it.copy(bloodTypeInput = bloodType) }
    }

    fun onEmergencyContactNameChanged(name: String) {
        _uiState.update { it.copy(emergencyContactNameInput = name) }
    }

    fun onEmergencyContactRelationChanged(relation: String) {
        _uiState.update { it.copy(emergencyContactRelationInput = relation) }
    }

    fun onNewAllergyTextChanged(text: String) {
        _uiState.update { it.copy(newAllergyText = text) }
    }

    fun onAddAllergy() {
        val trimmed = _uiState.value.newAllergyText.trim()
        if (trimmed.isNotBlank() && !_uiState.value.allergiesInput.contains(trimmed)) {
            _uiState.update { 
                it.copy(
                    allergiesInput = it.allergiesInput + trimmed,
                    newAllergyText = ""
                )
            }
        }
    }

    fun onRemoveAllergy(allergy: String) {
        _uiState.update { 
            it.copy(allergiesInput = it.allergiesInput - allergy)
        }
    }

    fun onNewConditionTextChanged(text: String) {
        _uiState.update { it.copy(newConditionText = text) }
    }

    fun onAddCondition() {
        val trimmed = _uiState.value.newConditionText.trim()
        if (trimmed.isNotBlank() && !_uiState.value.conditionsInput.contains(trimmed)) {
            _uiState.update { 
                it.copy(
                    conditionsInput = it.conditionsInput + trimmed,
                    newConditionText = ""
                )
            }
        }
    }

    fun onRemoveCondition(condition: String) {
        _uiState.update { 
            it.copy(conditionsInput = it.conditionsInput - condition)
        }
    }

    fun onSaveRecord() {
        val current = _uiState.value
        val ageVal = current.ageInput.toIntOrNull() ?: 0
        val updatedRecord = current.record.copy(
            name = current.nameInput.trim(),
            age = ageVal,
            bloodType = current.bloodTypeInput,
            allergies = current.allergiesInput,
            chronicConditions = current.conditionsInput,
            emergencyContactName = current.emergencyContactNameInput,
            emergencyContactRelation = current.emergencyContactRelationInput,
            isEncrypted = current.isLocked
        )
        medicalIdRepository.updateMedicalRecord(updatedRecord)
        _uiState.update { 
            it.copy(
                isEditing = false,
                isSavedSuccess = true
            ) 
        }
    }

    fun onRescuerAuthCodeChanged(code: String) {
        _uiState.update { it.copy(rescuerAuthCodeInput = code, unlockErrorMessage = null) }
    }

    fun onManualUnlockAttempt() {
        val success = medicalIdRepository.unlockWithRescuerToken(_uiState.value.rescuerAuthCodeInput)
        if (success) {
            _uiState.update { it.copy(isLocked = false, unlockErrorMessage = null) }
        } else {
            _uiState.update { it.copy(unlockErrorMessage = "Invalid Rescuer cryptographic key.") }
        }
    }
}
