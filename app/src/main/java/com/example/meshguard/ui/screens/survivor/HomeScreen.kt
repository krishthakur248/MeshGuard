package com.example.meshguard.ui.screens.survivor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.example.meshguard.R
import com.example.meshguard.data.model.MeshPeer
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.ui.components.PeerCard
import com.example.meshguard.ui.components.SectionHeader
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceBorder
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.ColorSurfaceElevatedDark
import com.example.meshguard.ui.theme.EmergencyGreen
import com.example.meshguard.ui.theme.EmergencyOrange
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.EmergencyYellow
import com.example.meshguard.ui.theme.MeshCyan
import com.example.meshguard.ui.theme.TextMuted
import com.example.meshguard.ui.theme.TextPrimary
import com.example.meshguard.ui.theme.TextSecondary
import com.example.meshguard.ui.viewmodel.HomeUiState
import com.example.meshguard.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToStatusPicker: () -> Unit = {},
    onNavigateToMedicalId: () -> Unit = {},
    onNavigateToMeshNetwork: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToBreadcrumbs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        modifier = modifier,
        onToggleBeacon = viewModel::onToggleBeacon,
        onToggleBatterySaver = viewModel::onToggleBatterySaver,
        onQuickStatusSelected = viewModel::onQuickStatusSelected,
        onNavigateToStatusPicker = onNavigateToStatusPicker,
        onNavigateToMedicalId = onNavigateToMedicalId,
        onNavigateToMeshNetwork = onNavigateToMeshNetwork,
        onNavigateToChat = onNavigateToChat,
        onNavigateToBreadcrumbs = onNavigateToBreadcrumbs
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onToggleBeacon: () -> Unit = {},
    onToggleBatterySaver: (Boolean) -> Unit = {},
    onQuickStatusSelected: (UrgencyStatus) -> Unit = {},
    onNavigateToStatusPicker: () -> Unit = {},
    onNavigateToMedicalId: () -> Unit = {},
    onNavigateToMeshNetwork: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToBreadcrumbs: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        PersistentMeshHeader(
            activePeers = uiState.activePeerCount,
            packetsCarried = uiState.packetsCarriedCount,
            isBroadcasting = uiState.isBroadcastingBeacon,
            isBatterySaver = uiState.isBatterySaverActive
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.profileWarningMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorSurfaceDark)
                        .border(1.dp, EmergencyOrange, RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateToMedicalId),
                    colors = CardDefaults.cardColors(containerColor = EmergencyOrange.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = EmergencyOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.profileWarningMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ActiveBeaconHeroCard(
                status = uiState.myStatus,
                sectorCode = uiState.sectorCode,
                isBroadcasting = uiState.isBroadcastingBeacon,
                onToggleBeacon = onToggleBeacon,
                onChangeStatusClick = onNavigateToStatusPicker
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(
                title = "Quick Triage Override",
                icon = Icons.Default.MedicalServices,
                subtitle = "Select current condition to update local beacon chirp"
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickStatusRow(
                currentStatus = uiState.myStatus,
                onStatusSelected = onQuickStatusSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Emergency Actions",
                icon = Icons.Default.Shield,
                subtitle = "Offline disaster tools and data mule shortcuts"
            )

            Spacer(modifier = Modifier.height(8.dp))

            EmergencyShortcutsGrid(
                unreadMessages = uiState.unreadMessageCount,
                onNavigateToMedicalId = onNavigateToMedicalId,
                onNavigateToChat = onNavigateToChat,
                onNavigateToBreadcrumbs = onNavigateToBreadcrumbs,
                onNavigateToMeshNetwork = onNavigateToMeshNetwork
            )

            Spacer(modifier = Modifier.height(24.dp))

            BatterySaverControlCard(
                isEcoActive = uiState.isBatterySaverActive,
                onToggleEco = onToggleBatterySaver
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Nearby Mesh Relays",
                icon = Icons.Default.Hub,
                badgeText = "${uiState.activePeerCount} Connected",
                badgeColor = MeshCyan,
                trailingAction = {
                    Text(
                        text = "VIEW ALL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MeshCyan,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToMeshNetwork)
                            .padding(4.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.nearbyPeersPreview.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = ColorSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.status_searching_peers),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                uiState.nearbyPeersPreview.forEach { peer ->
                    PeerCard(
                        peer = peer,
                        onClick = onNavigateToMeshNetwork
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PersistentMeshHeader(
    activePeers: Int,
    packetsCarried: Int,
    isBroadcasting: Boolean,
    isBatterySaver: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = ColorSurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isBroadcasting) EmergencyGreen else EmergencyOrange)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.status_offline_mesh, activePeers, packetsCarried),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (isBatterySaver) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MeshCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.status_eco),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MeshCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveBeaconHeroCard(
    status: UrgencyStatus,
    sectorCode: String,
    isBroadcasting: Boolean,
    onToggleBeacon: () -> Unit,
    onChangeStatusClick: () -> Unit
) {
    val statusColor = when (status) {
        UrgencyStatus.TRAPPED -> EmergencyRed
        UrgencyStatus.INJURED -> EmergencyOrange
        UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow
        UrgencyStatus.UNKNOWN -> TextMuted
        UrgencyStatus.NEED_WATER -> Color(0xFF00B0FF)
        UrgencyStatus.SAFE -> EmergencyGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurfaceDark)
            .border(2.dp, statusColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY BEACON ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ColorSurfaceElevatedDark
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
                            text = sectorCode,
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
                text = status.label.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Broadcasting offline BLE chirps to nearby peers and data mules.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onChangeStatusClick)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Change triage status"
                        },
                    color = ColorSurfaceElevatedDark
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHANGE STATUS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleBeacon)
                        .semantics {
                            role = Role.Button
                            contentDescription = if (isBroadcasting) "Pause beacon broadcast" else "Resume beacon broadcast"
                        },
                    color = if (isBroadcasting) statusColor.copy(alpha = 0.2f) else ColorSurfaceElevatedDark
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = if (isBroadcasting) statusColor else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBroadcasting) "PAUSE" else "RESUME",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBroadcasting) statusColor else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatusRow(
    currentStatus: UrgencyStatus,
    onStatusSelected: (UrgencyStatus) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val statuses = listOf(
            UrgencyStatus.TRAPPED,
            UrgencyStatus.INJURED,
            UrgencyStatus.NEEDS_INSULIN,
            UrgencyStatus.NEED_WATER,
            UrgencyStatus.SAFE
        )

        statuses.forEach { status ->
            val isSelected = currentStatus == status
            val badgeColor = when (status) {
                UrgencyStatus.TRAPPED -> EmergencyRed
                UrgencyStatus.INJURED -> EmergencyOrange
                UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow
                UrgencyStatus.UNKNOWN -> TextMuted
                UrgencyStatus.NEED_WATER -> Color(0xFF00B0FF)
                UrgencyStatus.SAFE -> EmergencyGreen
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) badgeColor else ColorSurfaceBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onStatusSelected(status) }
                    .semantics {
                        role = Role.RadioButton
                        contentDescription = "Select status ${status.label}"
                    },
                color = if (isSelected) badgeColor.copy(alpha = 0.2f) else ColorSurfaceDark
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) badgeColor else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyShortcutsGrid(
    unreadMessages: Int,
    onNavigateToMedicalId: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToBreadcrumbs: () -> Unit,
    onNavigateToMeshNetwork: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShortcutCard(
                title = "Medical ID",
                subtitle = "Encrypted Profile",
                icon = Icons.Default.MedicalServices,
                accentColor = EmergencyRed,
                onClick = onNavigateToMedicalId,
                modifier = Modifier.weight(1f)
            )

            ShortcutCard(
                title = "Mesh Chat",
                subtitle = "15s Voice Drops",
                icon = Icons.Default.ChatBubble,
                accentColor = MeshCyan,
                badgeCount = unreadMessages,
                onClick = onNavigateToChat,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShortcutCard(
                title = "Proximity Chain",
                subtitle = "Hop History",
                icon = Icons.Default.SwapHoriz,
                accentColor = EmergencyOrange,
                onClick = onNavigateToBreadcrumbs,
                modifier = Modifier.weight(1f)
            )

            ShortcutCard(
                title = "Network Topology",
                subtitle = "Radio Status",
                icon = Icons.Default.Hub,
                accentColor = EmergencyGreen,
                onClick = onNavigateToMeshNetwork,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Surface(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "$title, $subtitle"
            },
        color = ColorSurfaceDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = EmergencyRed,
                            contentColor = Color.White
                        ) {
                            Text(text = badgeCount.toString())
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorSurfaceElevatedDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BatterySaverControlCard(
    isEcoActive: Boolean,
    onToggleEco: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ColorSurfaceDark)
            .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ColorSurfaceElevatedDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Tactical Battery Saver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isEcoActive) "10s BLE duty-cycle duty cap active" else "Continuous scanning enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Switch(
                checked = isEcoActive,
                onCheckedChange = onToggleEco,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MeshCyan,
                    checkedTrackColor = ColorSurfaceElevatedDark,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = ColorSurfaceElevatedDark
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Surface(color = ColorBackgroundDark) {
        HomeScreenContent(
            uiState = HomeUiState()
        )
    }
}
