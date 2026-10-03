package com.example.meshguard.ui.screens.rescuer

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.ui.components.EmptyState
import com.example.meshguard.ui.components.OsmMapView
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceBorder
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.EmergencyGreen
import com.example.meshguard.ui.theme.EmergencyOrange
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.EmergencyYellow
import com.example.meshguard.ui.theme.MeshCyan
import com.example.meshguard.ui.theme.RescuerBadgeBlue
import com.example.meshguard.ui.theme.TextMuted
import com.example.meshguard.ui.theme.TextPrimary
import com.example.meshguard.ui.theme.TextSecondary
import com.example.meshguard.ui.viewmodel.ResponderDashboardViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Step 11 – Map 2: Full-screen overview map for Rescuers.
 * Shows one coloured marker per survivor. Tapping a marker shows a detail card
 * with a "View Details" button that navigates to SurvivorDetailScreen.
 *
 * Only reachable by Rescuers (nav graph enforces this).
 */
@Composable
fun SurvivorMapRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSurvivorDetail: (String) -> Unit,
    viewModel: ResponderDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SurvivorMapScreen(
        survivors = uiState.allSurvivors,
        onNavigateBack = onNavigateBack,
        onNavigateToSurvivorDetail = onNavigateToSurvivorDetail
    )
}

@Composable
fun SurvivorMapScreen(
    survivors: List<SurvivorPacket>,
    onNavigateBack: () -> Unit,
    onNavigateToSurvivorDetail: (String) -> Unit
) {
    val context = LocalContext.current

    // Track the survivor the user has tapped on
    var selectedSurvivor by remember { mutableStateOf<SurvivorPacket?>(null) }

    // Keep a reference to the MapView so we can update markers when survivors change
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Filter only survivors with valid GPS coordinates
    val survivorsWithGps = remember(survivors) {
        survivors.filter { it.latitude != 0.0 || it.longitude != 0.0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
    ) {
        // ── Top bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "INCIDENT OVERVIEW MAP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${survivorsWithGps.size} of ${survivors.size} survivors with GPS fix",
                    style = MaterialTheme.typography.labelSmall,
                    color = RescuerBadgeBlue
                )
            }

            // Map icon badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(RescuerBadgeBlue.copy(alpha = 0.15f))
                    .border(1.dp, RescuerBadgeBlue.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = RescuerBadgeBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Map or empty state ───────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            if (survivorsWithGps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "No GPS Locations Available",
                        message = if (survivors.isEmpty())
                            "No survivor packets received yet. The map will populate automatically as packets arrive."
                        else
                            "Received ${survivors.size} packet(s) but none carry GPS coordinates yet.",
                        icon = Icons.Default.SensorsOff
                    )
                }
            } else {
                // Centre the initial view on the first survivor with GPS
                val centre = GeoPoint(
                    survivorsWithGps[0].latitude,
                    survivorsWithGps[0].longitude
                )
                val zoom = if (survivorsWithGps.size == 1) 15.0 else 13.0

                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    initialCenter = centre,
                    initialZoom = zoom,
                    onMapReady = { mapView ->
                        mapViewRef = mapView
                        addSurvivorMarkers(
                            mapView = mapView,
                            context = context,
                            survivors = survivorsWithGps,
                            onMarkerTap = { survivor ->
                                selectedSurvivor = survivor
                            }
                        )
                    }
                )

                // Re-add markers when survivors list changes (new packets arrive)
                DisposableEffect(survivorsWithGps) {
                    mapViewRef?.let { mapView ->
                        // Remove only Marker overlays (keep RotationGestureOverlay)
                        mapView.overlays.removeAll { it is Marker }
                        addSurvivorMarkers(
                            mapView = mapView,
                            context = context,
                            survivors = survivorsWithGps,
                            onMarkerTap = { survivor ->
                                selectedSurvivor = survivor
                            }
                        )
                        mapView.invalidate()
                    }
                    onDispose { }
                }
            }

            // ── Selected survivor popup card ─────────────────────────────────
            selectedSurvivor?.let { survivor ->
                SurvivorMapPopup(
                    survivor = survivor,
                    onDismiss = { selectedSurvivor = null },
                    onViewDetails = {
                        selectedSurvivor = null
                        onNavigateToSurvivorDetail(survivor.survivorId)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }

        // ── Status legend ────────────────────────────────────────────────────
        MapLegend()
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Add one osmdroid Marker per survivor, coloured by status. */
private fun addSurvivorMarkers(
    mapView: MapView,
    context: android.content.Context,
    survivors: List<SurvivorPacket>,
    onMarkerTap: (SurvivorPacket) -> Unit
) {
    for (survivor in survivors) {
        val geoPoint = GeoPoint(survivor.latitude, survivor.longitude)
        val marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = survivor.survivorName.ifBlank { "Unknown" }
            snippet = survivor.statusTag.label

            // Use a tinted default marker icon (osmdroid default person-pin)
            icon = getTintedMarkerDrawable(context, survivor.statusTag)

            // On tap: show our Compose popup instead of the default osmdroid info window
            setOnMarkerClickListener { _, _ ->
                onMarkerTap(survivor)
                true
            }

            // Hide the default osmdroid info window
            infoWindow = null
        }
        mapView.overlays.add(marker)
    }
    mapView.invalidate()
}

/** Returns a coloured drawable for the marker based on triage status. */
internal fun getTintedMarkerDrawable(
    context: android.content.Context,
    status: UrgencyStatus
): Drawable {
    val base = androidx.core.content.ContextCompat.getDrawable(
        context,
        org.osmdroid.library.R.drawable.marker_default
    )?.mutate()

    val color = when (status) {
        UrgencyStatus.TRAPPED     -> android.graphics.Color.parseColor("#FF3B3B") // EmergencyRed
        UrgencyStatus.INJURED     -> android.graphics.Color.parseColor("#FF8C42") // EmergencyOrange
        UrgencyStatus.NEEDS_INSULIN -> android.graphics.Color.parseColor("#FFD60A") // EmergencyYellow
        UrgencyStatus.NEED_WATER  -> android.graphics.Color.parseColor("#00B0FF")
        UrgencyStatus.SAFE        -> android.graphics.Color.parseColor("#4CAF50") // EmergencyGreen
        UrgencyStatus.UNKNOWN     -> android.graphics.Color.parseColor("#9E9E9E")
    }

    base?.colorFilter = android.graphics.PorterDuffColorFilter(
        color,
        android.graphics.PorterDuff.Mode.SRC_IN
    )
    return base ?: android.graphics.drawable.ColorDrawable(color)
}

// ── Composable sub-widgets ────────────────────────────────────────────────────

@Composable
private fun SurvivorMapPopup(
    survivor: SurvivorPacket,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = survivorStatusColor(survivor.statusTag)
    val timeFormatted = remember(survivor.timestamp) {
        runCatching {
            val sdf = SimpleDateFormat("HH:mm · dd MMM", Locale.getDefault())
            sdf.format(Date(survivor.timestamp))
        }.getOrDefault("Recently")
    }
    val elapsedMins = ((System.currentTimeMillis() - survivor.timestamp) / 60_000L).coerceAtLeast(0)
    val elapsedText = if (elapsedMins < 1) "Just now" else "$elapsedMins min ago"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = survivor.statusTag.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "P${survivor.priority}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Dismiss",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = survivor.survivorName.ifBlank { "Unidentified Survivor" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "ID: ${survivor.survivorId}",
                style = MaterialTheme.typography.labelSmall,
                color = RescuerBadgeBlue,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "$elapsedText  ·  $timeFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row {
                Text(
                    text = "%.5f°, %.5f°".format(Locale.US, survivor.latitude, survivor.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Button(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = RescuerBadgeBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "View Full Dossier",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLegend() {
    Surface(
        color = ColorSurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STATUS:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            LegendDot("TRAPPED", EmergencyRed)
            LegendDot("INJURED", EmergencyOrange)
            LegendDot("MEDS", EmergencyYellow)
            LegendDot("SAFE", EmergencyGreen)
            LegendDot("UNKNOWN", TextMuted)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp
        )
    }
}

/** Utility shared between SurvivorMapScreen and the mini-map in SurvivorDetailScreen. */
fun survivorStatusColor(status: UrgencyStatus): Color = when (status) {
    UrgencyStatus.TRAPPED      -> EmergencyRed
    UrgencyStatus.INJURED      -> EmergencyOrange
    UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow
    UrgencyStatus.NEED_WATER   -> Color(0xFF00B0FF)
    UrgencyStatus.SAFE         -> EmergencyGreen
    UrgencyStatus.UNKNOWN      -> TextMuted
}
