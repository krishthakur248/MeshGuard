package com.example.meshguard.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.meshguard.AppConfig
import com.example.meshguard.R
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.ui.AppStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingSlide(
    val titleRes: Int,
    val descRes: Int,
    val iconName: String
)

data class OnboardingUiState(
    val currentSlideIndex: Int = 0,
    val totalSlides: Int = 3,
    val isShowingRoleSelection: Boolean = false,
    val selectedRole: UserRole = UserRole.SURVIVOR,
    val rescuerAuthCode: String = "",
    val authCodeErrorRes: Int? = null,
    val isRoleConfirmed: Boolean = false
)

class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNextClicked() {
        if (_uiState.value.currentSlideIndex < _uiState.value.totalSlides - 1) {
            _uiState.update { it.copy(currentSlideIndex = it.currentSlideIndex + 1) }
        } else {
            _uiState.update { it.copy(isShowingRoleSelection = true) }
        }
    }

    fun onSkipClicked() {
        _uiState.update { it.copy(isShowingRoleSelection = true) }
    }

    fun onSlideChanged(newIndex: Int) {
        _uiState.update { it.copy(currentSlideIndex = newIndex.coerceIn(0, it.totalSlides - 1)) }
    }

    fun onSelectRole(role: UserRole) {
        _uiState.update { 
            it.copy(
                selectedRole = role,
                authCodeErrorRes = null
            )
        }
    }

    fun onRescuerAuthCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                rescuerAuthCode = code,
                authCodeErrorRes = null
            )
        }
    }

    fun onConfirmRole() {
        val currentState = _uiState.value
        if (currentState.selectedRole == UserRole.RESCUER) {
            val trimmedCode = currentState.rescuerAuthCode.trim()
            if (trimmedCode.isEmpty()) {
                _uiState.update { it.copy(authCodeErrorRes = R.string.onboarding_err_code_empty) }
                return
            }
            if (trimmedCode != AppConfig.RESCUER_ACCESS_CODE_DEMO) {
                _uiState.update { it.copy(authCodeErrorRes = R.string.onboarding_err_code_invalid) }
                return
            }
            AppStateManager.setUserRole(UserRole.RESCUER, trimmedCode)
        } else {
            AppStateManager.setUserRole(UserRole.SURVIVOR, null)
        }

        AppStateManager.completeOnboarding()
        _uiState.update { it.copy(isRoleConfirmed = true) }
    }
}
