package com.example.meshguard.ui.screens.rescuer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.R
import com.example.meshguard.data.model.SurvivorPacket
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.ui.components.EmptyState
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceBorder
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.ColorSurfaceElevatedDark
import com.example.meshguard.ui.theme.EmergencyGreen
import com.example.meshguard.ui.theme.EmergencyOrange
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.EmergencyYellow
import com.example.meshguard.ui.theme.MeshCyan
import com.example.meshguard.ui.theme.RescuerBadgeBlue
import com.example.meshguard.ui.theme.TextMuted
import com.example.meshguard.ui.theme.TextPrimary
import com.example.meshguard.ui.theme.TextSecondary
import com.example.meshguard.ui.viewmodel.SurvivorDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SurvivorDetailRoute(
    survivorId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SurvivorDetailViewModel = viewModel()
) {
    LaunchedEffect(survivorId) {
        viewModel.loadSurvivor(survivorId)
    }

    val uiState by viewModel.uiState.collectAsState()

    SurvivorDetailScreen(
        survivor = uiState.survivor,
        isAcknowledged = uiState.isAcknowledged,
        onAcknowledge = { viewModel.markAcknowledged(survivorId) },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
fun SurvivorDetailScreen(
    survivor: SurvivorPacket?,
    isAcknowledged: Boolean,
    onAcknowledge: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
    ) {
        // Tactical Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CASUALTY DOSSIER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (survivor != null) "ID: ${survivor.survivorId}" else "Loading...",
                    style = MaterialTheme.typography.labelSmall,
                    color = RescuerBadgeBlue,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (survivor != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = RescuerBadgeBlue.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RescuerBadgeBlue.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = survivor.sectorCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RescuerBadgeBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (survivor == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "Casualty Not Found",
                    message = "The requested survivor packet could not be located in local Room storage.",
                    actionLabel = "Back to Queue",
                    onActionClick = onNavigateBack
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status & Urgency Card
                StatusBannerCard(survivor = survivor)

                // GPS & Mesh Hop Routing Card
                LocationAndMeshCard(survivor = survivor)

                // Medical Record & Profile Card
                MedicalProfileCard(survivor = survivor)

                // Action Bar: Dispatch / Acknowledge
                ActionBanner(
                    isAcknowledged = isAcknowledged,
                    onAcknowledge = onAcknowledge
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatusBannerCard(survivor: SurvivorPacket) {
    val statusColor = when (survivor.statusTag) {
        UrgencyStatus.TRAPPED -> EmergencyRed
        UrgencyStatus.INJURED -> EmergencyOrange
        UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow
        UrgencyStatus.NEED_WATER -> Color(0xFF00B0FF)
        UrgencyStatus.SAFE -> EmergencyGreen
        UrgencyStatus.UNKNOWN -> TextMuted
    }

    val timeFormatted = runCatching {
        val sdf = SimpleDateFormat("HH:mm:ss · dd MMM", Locale.getDefault())
        sdf.format(Date(survivor.timestamp))
    }.getOrDefault("Recently")

    val elapsedMins = ((System.currentTimeMillis() - survivor.timestamp) / 60_000L).coerceAtLeast(0)
    val elapsedText = if (elapsedMins < 1) "Just now" else "$elapsedMins min ago"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
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
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = survivor.statusTag.label.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "PRIORITY ${survivor.priority}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = survivor.survivorName.ifBlank { "Unidentified Survivor" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Broadcast: $elapsedText ($timeFormatted)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun LocationAndMeshCard(survivor: SurvivorPacket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = MeshCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GPS POSITION & RADIO PROPAGATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MeshCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val hasGps = survivor.latitude != 0.0 || survivor.longitude != 0.0
            if (hasGps) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailField(
                        label = "LATITUDE",
                        value = "%.6f°".format(Locale.US, survivor.latitude),
                        modifier = Modifier.weight(1f)
                    )
                    DetailField(
                        label = "LONGITUDE",
                        value = "%.6f°".format(Locale.US, survivor.longitude),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (survivor.locationAccuracy > 0f) {
                    DetailField(
                        label = "GPS ACCURACY",
                        value = "±%.1f meters".format(Locale.US, survivor.locationAccuracy)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ColorSurfaceElevatedDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GPS fix pending or unavailable on survivor device",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Radio Hop Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val hopText = if (survivor.hopCount == 0) "Direct Peer (0 hops)" else "${survivor.hopCount} mule hop(s)"
                DetailField(
                    label = "GOSSIP PROPAGATION",
                    value = hopText,
                    modifier = Modifier.weight(1f)
                )
                DetailField(
                    label = "PACKET TTL",
                    value = "${survivor.ttl} hops max",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MedicalProfileCard(survivor: SurvivorPacket) {
    val med = survivor.medicalData

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = EmergencyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MEDICAL ID & IDENTITY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = EmergencyOrange
                    )
                }

                Icon(
                    imageVector = if (med.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (med.isEncrypted) TextMuted else EmergencyGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailField(
                    label = "AGE",
                    value = if (med.age > 0) "${med.age} yrs" else "Not provided",
                    modifier = Modifier.weight(1f)
                )
                DetailField(
                    label = "BLOOD GROUP",
                    value = med.bloodType.ifBlank { "Unknown" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            DetailField(
                label = "ALLERGIES",
                value = if (med.allergies.isNotEmpty()) med.allergies.joinToString(", ") else "None recorded"
            )

            Spacer(modifier = Modifier.height(10.dp))

            DetailField(
                label = "CHRONIC CONDITIONS",
                value = if (med.chronicConditions.isNotEmpty()) med.chronicConditions.joinToString(", ") else "None recorded"
            )

            if (med.emergencyContactName.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                DetailField(
                    label = "EMERGENCY CONTACT",
                    value = "${med.emergencyContactName} (${med.emergencyContactRelation.ifBlank { "Contact" }})"
                )
            }
        }
    }
}

@Composable
private fun ActionBanner(
    isAcknowledged: Boolean,
    onAcknowledge: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (isAcknowledged) EmergencyGreen else RescuerBadgeBlue, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isAcknowledged) EmergencyGreen.copy(alpha = 0.08f) else ColorSurfaceDark
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAcknowledged) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmergencyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CASUALTY ACKNOWLEDGED & DISPATCHED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyGreen
                    )
                }
            } else {
                Button(
                    onClick = onAcknowledge,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RescuerBadgeBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acknowledge / Mark Handled",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
