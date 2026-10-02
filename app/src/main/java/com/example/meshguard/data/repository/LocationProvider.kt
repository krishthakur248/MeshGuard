package com.example.meshguard.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Step 8: Provides real GPS location updates for survivor packets.
 * Uses FusedLocationProviderClient (works offline with GPS, no internet needed).
 * Default interval: 30 seconds (per docs/03-architecture.md).
 */
class LocationProvider(private val context: Context) {

    private val TAG = "LocationProvider"

    data class LocationData(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val accuracyMeters: Float = 0f,
        /** GPS fix time (accurate even without internet) or System.currentTimeMillis() as fallback. */
        val capturedAt: Long = 0L
    )

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _currentLocation = MutableStateFlow(LocationData())
    val currentLocation: StateFlow<LocationData> = _currentLocation.asStateFlow()

    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            // Prefer GPS fix time (accurate without internet) over phone clock
            val capturedAt = if (loc.time > 0L) loc.time else System.currentTimeMillis()
            val data = LocationData(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracy,
                capturedAt = capturedAt
            )
            _currentLocation.value = data
            Log.d(TAG, "Location update: lat=${data.latitude}, lon=${data.longitude}, acc=${data.accuracyMeters}m")
        }
    }

    /**
     * Start tracking location every [intervalMs] milliseconds.
     * Requires ACCESS_FINE_LOCATION permission to be granted already.
     */
    fun startTracking(intervalMs: Long = 30_000L) {
        if (isTracking) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "startTracking: ACCESS_FINE_LOCATION not granted — skipping")
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            isTracking = true
            Log.d(TAG, "✓ Location tracking started (interval=${intervalMs}ms)")

            // Also grab the last known location immediately so we don't wait 30s
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val capturedAt = if (loc.time > 0L) loc.time else System.currentTimeMillis()
                    _currentLocation.value = LocationData(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracyMeters = loc.accuracy,
                        capturedAt = capturedAt
                    )
                    Log.d(TAG, "lastLocation: lat=${loc.latitude}, lon=${loc.longitude}")
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting location tracking", e)
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        fusedClient.removeLocationUpdates(locationCallback)
        isTracking = false
        Log.d(TAG, "Location tracking stopped")
    }

    /** Check if we have a real location fix (non-zero coordinates). */
    fun hasLocation(): Boolean {
        val loc = _currentLocation.value
        return loc.latitude != 0.0 || loc.longitude != 0.0
    }
}
