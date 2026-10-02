package com.example.meshguard.ui

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lightweight global app state holder managing selected UserRole and authentication state.
 */
object AppStateManager {

    private var accountRepository: AccountRepository? = null

    private val _userRole = MutableStateFlow(UserRole.SURVIVOR)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _rescuerToken = MutableStateFlow<String?>(null)
    val rescuerToken: StateFlow<String?> = _rescuerToken.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _hasGrantedPermissions = MutableStateFlow(false)
    val hasGrantedPermissions: StateFlow<Boolean> = _hasGrantedPermissions.asStateFlow()

    fun init(context: Context) {
        val repo = AccountRepository.getInstance(context)
        accountRepository = repo
        _userRole.value = repo.userRole.value
        _hasCompletedOnboarding.value = repo.hasCompletedOnboarding.value

        // Load persisted value, then verify hardware is actually on.
        // If Bluetooth or Location is off, override to false so the routing
        // sends the user to the Permissions screen on app start.
        var permissionsOk = repo.hasGrantedPermissions.value
        if (permissionsOk) {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val isBtOn = btManager?.adapter?.isEnabled == true

            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val isLocOn = locManager != null && LocationManagerCompat.isLocationEnabled(locManager)

            if (!isBtOn || !isLocOn) {
                permissionsOk = false
                // Also persist the revocation so it sticks across process restarts.
                repo.saveGrantedPermissions(false)
            }
        }
        _hasGrantedPermissions.value = permissionsOk
    }

    fun setUserRole(role: UserRole, token: String? = null) {
        _userRole.value = role
        _rescuerToken.value = token
        accountRepository?.saveUserRole(role)
    }

    fun completeOnboarding() {
        _hasCompletedOnboarding.value = true
        accountRepository?.saveCompletedOnboarding(true)
    }

    fun setPermissionsGranted(granted: Boolean) {
        _hasGrantedPermissions.value = granted
        accountRepository?.saveGrantedPermissions(granted)
    }
}

