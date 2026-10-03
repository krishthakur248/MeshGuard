package com.example.meshguard

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.location.LocationManagerCompat
import androidx.navigation.compose.rememberNavController
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.data.repository.AccountRepository
import com.example.meshguard.service.MeshForegroundService
import com.example.meshguard.ui.AppStateManager
import com.example.meshguard.ui.navigation.MeshGuardNavGraph
import com.example.meshguard.ui.navigation.Screen
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.MeshGuardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppDependencies.init(applicationContext)
        AppStateManager.init(this)

        setContent {
            MeshGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ColorBackgroundDark
                ) {
                    val navController = rememberNavController()
                    val hasCompletedOnboarding by AppStateManager.hasCompletedOnboarding.collectAsState()
                    val hasGrantedPermissions by AppStateManager.hasGrantedPermissions.collectAsState()
                    val userRole by AppStateManager.userRole.collectAsState()

                    val startDestination = when {
                        !hasCompletedOnboarding -> Screen.Onboarding.route
                        !hasGrantedPermissions -> Screen.Permissions.route
                        userRole == UserRole.RESCUER -> Screen.ResponderDashboard.route
                        else -> Screen.Home.route
                    }

                    // Reactive guard: whenever hasGrantedPermissions becomes false
                    // (from onResume detecting BT/Location off, or from init()),
                    // navigate to the Permissions screen immediately.
                    // This also fires on first composition if the NavController
                    // restored saved state from a previous session that had Home
                    // as the top screen — startDestination alone won't fix that.
                    LaunchedEffect(hasCompletedOnboarding, hasGrantedPermissions) {
                        if (hasCompletedOnboarding && !hasGrantedPermissions) {
                            val currentRoute = navController.currentBackStackEntry
                                ?.destination?.route
                            if (currentRoute != null
                                && currentRoute != Screen.Permissions.route
                                && currentRoute != Screen.Onboarding.route
                            ) {
                                navController.navigate(Screen.Permissions.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }

                    MeshGuardNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    /**
     * Every time the app comes to the foreground we re-check hardware.
     * We also start the foreground service if broadcasting is active so
     * it shows the latest notification state right away.
     */
    override fun onResume() {
        super.onResume()
        if (!AppStateManager.hasCompletedOnboarding.value) return

        val btManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val isBtOn = btManager?.adapter?.isEnabled == true

        val locManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isLocOn = locManager != null && LocationManagerCompat.isLocationEnabled(locManager)

        if (!isBtOn || !isLocOn) {
            AppStateManager.setPermissionsGranted(false)
        } else if (AppStateManager.hasGrantedPermissions.value) {
            // Hardware and permissions are OK.
            // If user is a Rescuer or broadcasting is enabled, ensure mesh radio is actively running.
            val accountRepo = AccountRepository.getInstance(applicationContext)
            val isRescuer = AppStateManager.userRole.value == UserRole.RESCUER
            if (isRescuer || accountRepo.loadIsBroadcasting()) {
                AppDependencies.meshRepository.toggleBroadcast(true)
                // Step 8: Start GPS location tracking alongside mesh radio
                if (!isRescuer) {
                    AppDependencies.locationProvider.startTracking()
                }
                // Step 9: Ensure foreground service is alive when we return to foreground.
                MeshForegroundService.start(this)
            }
        }
    }

    /**
     * Step 9: When the activity goes to the background (user presses Home or
     * switches apps), start the foreground service so the mesh radio keeps running.
     * The service shows a persistent notification — just like Spotify or Google Maps.
     */
    override fun onStop() {
        super.onStop()
        if (!AppStateManager.hasCompletedOnboarding.value) return
        if (!AppStateManager.hasGrantedPermissions.value) return

        val accountRepo = AccountRepository.getInstance(applicationContext)
        val isRescuer = AppStateManager.userRole.value == UserRole.RESCUER
        // Only keep the service alive if broadcasting is actually enabled.
        if (isRescuer || accountRepo.loadIsBroadcasting()) {
            MeshForegroundService.start(this)
        }
    }
}
