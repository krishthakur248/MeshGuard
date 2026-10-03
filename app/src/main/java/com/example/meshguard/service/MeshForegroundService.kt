package com.example.meshguard.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.meshguard.AppDependencies
import com.example.meshguard.MainActivity
import com.example.meshguard.R
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.data.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Step 9: Foreground service that keeps the mesh radio alive when the app is
 * minimised or the screen is off.
 *
 * Survivor role  → shows a persistent notification with current status + peer count.
 *                  Keeps sending/relaying packets. Location tracking stays on.
 * Rescuer role   → shows a persistent notification with peer/relay stats.
 *                  Still advertises and discovers so it keeps relaying survivor packets.
 *
 * The service is started via startForegroundService() from MainActivity/HomeViewModel
 * and stopped when the user presses Pause / turns off the toggle, or when
 * isBroadcastingBeacon becomes false.
 */
class MeshForegroundService : Service() {

    companion object {
        private const val TAG = "MeshFgService"

        // Notification
        const val CHANNEL_ID = "meshguard_mesh_channel"
        const val NOTIF_ID = 1001

        // Intent actions
        const val ACTION_START = "com.example.meshguard.action.START_MESH"
        const val ACTION_STOP  = "com.example.meshguard.action.STOP_MESH"

        /** Convenience helper: start the service from any Context. */
        fun start(context: Context) {
            val intent = Intent(context, MeshForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Convenience helper: stop the service from any Context. */
        fun stop(context: Context) {
            val intent = Intent(context, MeshForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var notifUpdateJob: Job? = null

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        return when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                START_NOT_STICKY
            }
            else -> {
                // Move to foreground immediately so Android doesn't kill us.
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ServiceCompat.startForeground(
                            this,
                            NOTIF_ID,
                            buildNotification(),
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                        )
                    } else {
                        startForeground(NOTIF_ID, buildNotification())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting foreground service", e)
                }

                // Make sure AppDependencies is initialized (in case the process
                // was cold-started directly into the service by Android).
                AppDependencies.init(applicationContext)

                // Make sure mesh radio is running.
                AppDependencies.meshRepository.toggleBroadcast(true)

                // Make sure location tracking is on for survivors.
                val accountRepo = AccountRepository.getInstance(applicationContext)
                val isRescuer = accountRepo.userRole.value == UserRole.RESCUER
                if (!isRescuer) {
                    AppDependencies.locationProvider.startTracking()
                }

                // Refresh the notification every ~5 seconds so the user sees
                // live stats (peer count, status) without draining the battery.
                startNotificationUpdates()

                // START_STICKY: if Android kills the service to reclaim memory,
                // it will automatically restart it (without the original Intent,
                // which defaults to ACTION_START in onStartCommand's else branch).
                START_STICKY
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // -------------------------------------------------------------------------
    // Notification helpers
    // -------------------------------------------------------------------------

    /** Create the notification channel (required on Android 8+). */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                // LOW importance → no sound/vibration, but still shows in status bar.
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    /** Build the persistent foreground notification. */
    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val accountRepo = AccountRepository.getInstance(applicationContext)
        val isRescuer = accountRepo.userRole.value == UserRole.RESCUER

        // Try to get live data from the repository if it's initialized.
        val peerCount = runCatching {
            AppDependencies.meshRepository.nearbyPeers.value.size
        }.getOrDefault(0)

        val carriedCount = runCatching {
            AppDependencies.meshRepository.packetsCarriedCount.value
        }.getOrDefault(0)

        val myStatus: UrgencyStatus = runCatching {
            AppDependencies.survivorRepository.mySurvivorPacket.value.statusTag
        }.getOrDefault(UrgencyStatus.UNKNOWN)

        val (title, body) = if (isRescuer) {
            getString(R.string.notif_title_rescuer) to
                getString(R.string.notif_body_rescuer, peerCount, carriedCount)
        } else {
            getString(R.string.notif_title_survivor) to
                getString(R.string.notif_body_survivor, myStatus.name, peerCount)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_mesh_notif)        // created below in drawable
            .setContentIntent(pendingIntent)
            .setOngoing(true)                              // cannot be dismissed by swipe
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    /** Post updated stats to the notification every 5 seconds. */
    private fun startNotificationUpdates() {
        notifUpdateJob?.cancel()
        notifUpdateJob = serviceScope.launch {
            while (isActive) {
                delay(5_000L)
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIF_ID, buildNotification())
            }
        }
    }
}
