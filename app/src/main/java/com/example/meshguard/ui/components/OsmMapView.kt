package com.example.meshguard.ui.components

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.io.File

private const val TAG = "OsmMapView"

private var isConfigured = false

/**
 * Configure osmdroid once globally.
 * Sets browser headers so OSM tile server treats requests like Leaflet in a browser.
 */
private fun configureOsmdroid(context: Context) {
    if (isConfigured) return
    isConfigured = true
    Configuration.getInstance().apply {
        load(context, context.getSharedPreferences("osmdroid_prefs", 0))
        osmdroidBasePath = context.cacheDir
        osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        userAgentValue = "MeshGuard/1.0"

        additionalHttpRequestProperties["User-Agent"] =
            "Mozilla/5.0 (Linux; Android 14; MeshGuard) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        additionalHttpRequestProperties["Referer"] = "https://meshguard.app/"
        additionalHttpRequestProperties["Accept"] = "image/png,image/*,*/*"
    }
}

/**
 * Step 11: Composable wrapper around osmdroid MapView.
 *
 * Uses OpenStreetMap MAPNIK tiles (same tile server as Leaflet on the web).
 *
 * Interactivity optimizations:
 * - Touch interception disallow: Prevents parent scrollables (e.g. Compose verticalScroll)
 *   from stealing drag/pan gestures, fixing lag/stutter on embedded mini-maps.
 * - [enableRotation]: Can be disabled for embedded mini-maps to eliminate gesture conflicts
 *   between rotation and pinch-to-zoom.
 * - Zoom buttons overlay disabled to eliminate overlay layout/animation passes.
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    initialCenter: GeoPoint = GeoPoint(20.5937, 78.9629),
    initialZoom: Double = 15.0,
    enableRotation: Boolean = true,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember {
        configureOsmdroid(context)
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            isTilesScaledToDpi = true
            setMultiTouchControls(true)
            isFlingEnabled = true
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setBackgroundColor(Color.parseColor("#1A1F2B"))

            if (enableRotation) {
                val rotationGesture = RotationGestureOverlay(this)
                rotationGesture.isEnabled = true
                overlays.add(rotationGesture)
            }

            // Disallow parent Compose scroll containers from intercepting touch events
            // while interacting with the map. This resolves the inner map drag lag.
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }

            controller.setZoom(initialZoom)
            controller.setCenter(initialCenter)
        }
    }

    DisposableEffect(mapView) {
        onMapReady(mapView)
        onDispose { }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) { mapView.onResume() }
            override fun onPause(owner: LifecycleOwner) { mapView.onPause() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
