package com.example.meshguard.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.meshguard.R
import com.example.meshguard.ui.AppStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MeshPermissionType {
    BLUETOOTH,
    WIFI_DIRECT,
    LOCATION_LEGACY,
    AUDIO_RECORD,
    NOTIFICATIONS
}

data class PermissionItemState(
    val type: MeshPermissionType,
    val titleRes: Int,
    val descriptionRes: Int,
    val apiNote: String,
    val requiredPermissions: List<String>,
    val isGranted: Boolean = false,
    val isApplicableForDevice: Boolean = true
)

data class PermissionsUiState(
    val permissions: List<PermissionItemState> = emptyList(),
    val allEssentialGranted: Boolean = false
)

class PermissionsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun checkAndInitializePermissions(context: Context) {
        val currentSdk = Build.VERSION.SDK_INT

        val items = mutableListOf<PermissionItemState>()

        // 1. Bluetooth LE Scanning / Advertising / Connect
        val btPermissions = if (currentSdk >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        val btGranted = btPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        items.add(
            PermissionItemState(
                type = MeshPermissionType.BLUETOOTH,
                titleRes = R.string.perm_bt_title,
                descriptionRes = R.string.perm_bt_desc,
                apiNote = if (currentSdk >= Build.VERSION_CODES.S) "Android 12+ (Nearby Mesh Scanning)" else "Legacy Bluetooth Radio",
                requiredPermissions = btPermissions,
                isGranted = btGranted,
                isApplicableForDevice = true
            )
        )

        // 2. Wi-Fi Direct / Nearby Devices
        val wifiPermissions = if (currentSdk >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            listOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
        }
        val wifiGranted = wifiPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        items.add(
            PermissionItemState(
                type = MeshPermissionType.WIFI_DIRECT,
                titleRes = R.string.perm_wifi_title,
                descriptionRes = R.string.perm_wifi_desc,
                apiNote = if (currentSdk >= Build.VERSION_CODES.TIRAMISU) "Android 13+ (Direct P2P Transfer)" else "Standard Wi-Fi Direct State",
                requiredPermissions = wifiPermissions,
                isGranted = wifiGranted,
                isApplicableForDevice = true
            )
        )

        // 3. Location — Nearby Connections requires ACCESS_FINE_LOCATION on ALL
        //    Android versions for BLE/Wi-Fi scanning, even on Android 12+ where
        //    standalone BLE scanning can use neverForLocation.
        val locationPermissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val locationGranted = locationPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        items.add(
            PermissionItemState(
                type = MeshPermissionType.LOCATION_LEGACY,
                titleRes = R.string.perm_location_title,
                descriptionRes = R.string.perm_location_desc,
                apiNote = "Required for Nearby Connections mesh discovery",
                requiredPermissions = locationPermissions,
                isGranted = locationGranted,
                isApplicableForDevice = true   // always required
            )
        )

        // 4. Microphone (Deferred for Step 3 per Rule 6; requested only when Voice Drop feature is built)
        val audioPermissions = listOf(Manifest.permission.RECORD_AUDIO)
        val audioGranted = audioPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        items.add(
            PermissionItemState(
                type = MeshPermissionType.AUDIO_RECORD,
                titleRes = R.string.perm_audio_title,
                descriptionRes = R.string.perm_audio_desc,
                apiNote = "Deferred until Voice Drop feature step",
                requiredPermissions = audioPermissions,
                isGranted = audioGranted,
                isApplicableForDevice = false
            )
        )

        // 5. Notifications (Android 13+ / API 33+)
        val isNotifRuntimeApplicable = currentSdk >= Build.VERSION_CODES.TIRAMISU
        val notifPermissions = if (isNotifRuntimeApplicable) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
        val notifGranted = if (isNotifRuntimeApplicable) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        items.add(
            PermissionItemState(
                type = MeshPermissionType.NOTIFICATIONS,
                titleRes = R.string.perm_notif_title,
                descriptionRes = R.string.perm_notif_desc,
                apiNote = if (isNotifRuntimeApplicable) "Android 13+ (Relay & Rescuer Alerts)" else "Standard Notifications",
                requiredPermissions = notifPermissions,
                isGranted = notifGranted,
                isApplicableForDevice = isNotifRuntimeApplicable
            )
        )

        val allEssential = items.filter { it.isApplicableForDevice }.all { it.isGranted }
        _uiState.update {
            it.copy(
                permissions = items,
                allEssentialGranted = allEssential
            )
        }
        // DO NOT set AppStateManager.setPermissionsGranted here!
        // This method only updates the UI card states.
        // The global flag is set only in onCompletePermissions() after
        // the user taps "ENTER MESHGUARD" and BT+Location are verified on.
    }

    fun onPermissionsResult(context: Context) {
        checkAndInitializePermissions(context)
    }

    /**
     * Called when the user taps "ENTER MESHGUARD".
     * The PermissionsScreen already verified BT + Location hardware are on
     * before calling this, so we just check OS permissions are still granted.
     */
    fun onCompletePermissions(context: Context) {
        checkAndInitializePermissions(context)
        if (_uiState.value.allEssentialGranted) {
            AppStateManager.setPermissionsGranted(true)
        }
    }
}
