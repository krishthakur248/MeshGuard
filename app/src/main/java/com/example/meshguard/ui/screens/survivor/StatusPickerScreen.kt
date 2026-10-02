package com.example.meshguard.ui.screens.survivor

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceBorder
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.ColorSurfaceElevatedDark
import com.example.meshguard.ui.theme.EmergencyGreen
import com.example.meshguard.ui.theme.EmergencyOrange
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.EmergencyYellow
import com.example.meshguard.ui.theme.MeshCyan
import com.example.meshguard.ui.theme.TextPrimary
import com.example.meshguard.ui.theme.TextSecondary
import com.example.meshguard.ui.viewmodel.StatusPickerUiState
import com.example.meshguard.ui.viewmodel.StatusPickerViewModel

@Composable
fun StatusPickerScreen(
    modifier: Modifier = Modifier,
    viewModel: StatusPickerViewModel = viewModel(),
    onConfirmComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    StatusPickerScreenContent(
        uiState = uiState,
        modifier = modifier,
        onSelectStatus = viewModel::onSelectStatus,
        onConfirmStatus = {
            viewModel.onConfirmStatus()
            onConfirmComplete()
        }
    )
}

@Composable
fun StatusPickerScreenContent(
    uiState: StatusPickerUiState,
    modifier: Modifier = Modifier,
    onSelectStatus: (UrgencyStatus) -> Unit = {},
    onConfirmStatus: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val statuses = UrgencyStatus.entries

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRIAGE STATUS SELECTION",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = ColorSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = uiState.sectorCode,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MeshCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Broadcast Urgency Tag",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Select your current status to update local BLE beacon chirps for incoming rescue relays.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            statuses.forEach { status ->
                StatusTileOption(
                    status = status,
                    isSelected = uiState.selectedStatus == status,
                    onSelect = { onSelectStatus(status) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        val currentStatusColor = when (uiState.selectedStatus) {
            UrgencyStatus.TRAPPED -> EmergencyRed
            UrgencyStatus.INJURED -> EmergencyOrange
            UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow
            UrgencyStatus.UNKNOWN -> TextSecondary
            UrgencyStatus.NEED_WATER -> Color(0xFF00B0FF)
            UrgencyStatus.SAFE -> EmergencyGreen
        }

        Button(
            onClick = onConfirmStatus,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = currentStatusColor,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "BROADCAST STATUS UPDATE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatusTileOption(
    status: UrgencyStatus,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val statusPair: Pair<Color, ImageVector> = when (status) {
        UrgencyStatus.TRAPPED -> EmergencyRed to Icons.Default.Warning
        UrgencyStatus.INJURED -> EmergencyOrange to Icons.Default.Healing
        UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow to Icons.Default.MedicalServices
        UrgencyStatus.UNKNOWN -> TextSecondary to Icons.Default.NearMe
        UrgencyStatus.NEED_WATER -> Color(0xFF00B0FF) to Icons.Default.WaterDrop
        UrgencyStatus.SAFE -> EmergencyGreen to Icons.Default.CheckCircle
    }
    val statusColor = statusPair.first
    val statusIcon = statusPair.second

    val descriptionText = when (status) {
        UrgencyStatus.TRAPPED -> "Structural collapse / blocked exit. Immediate extraction needed."
        UrgencyStatus.INJURED -> "Bleeding, fracture, or trauma requiring field medic."
        UrgencyStatus.NEEDS_INSULIN -> "Critical medication required within hours."
        UrgencyStatus.UNKNOWN -> "Status not yet determined. Broadcasting position."
        UrgencyStatus.NEED_WATER -> "Potable water / food rations required."
        UrgencyStatus.SAFE -> "Uninjured, secure location. Functioning as relay node."
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) statusColor else ColorSurfaceBorder,
        label = "statusBorderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ColorSurfaceDark)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onSelect)
            .semantics {
                role = Role.RadioButton
                contentDescription = "${status.label}, $descriptionText"
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) statusColor.copy(alpha = 0.15f) else ColorSurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) statusColor else ColorSurfaceElevatedDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = status.label.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "PRIORITY ${status.priorityLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) statusColor else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusPickerScreenPreview() {
    Surface(color = ColorBackgroundDark) {
        StatusPickerScreenContent(
            uiState = StatusPickerUiState()
        )
    }
}
