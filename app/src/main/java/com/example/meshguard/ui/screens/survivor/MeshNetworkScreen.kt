package com.example.meshguard.ui.screens.survivor

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.R
import com.example.meshguard.data.model.MeshPeer
import com.example.meshguard.ui.components.EmptyState
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
import com.example.meshguard.ui.viewmodel.MeshNetworkUiState
import com.example.meshguard.ui.viewmodel.MeshNetworkViewModel
import com.example.meshguard.ui.viewmodel.MeshSyncLogEntry

@Composable
fun MeshNetworkRoute(
    onNavigateBack: () -> Unit,
    viewModel: MeshNetworkViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.onForceRescan()
    }

    MeshNetworkScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onForceRescan = viewModel::onForceRescan,
        onToggleBroadcast = viewModel::onToggleBroadcast,
        onPeerClicked = viewModel::onPeerClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshNetworkScreen(
    uiState: MeshNetworkUiState,
    onNavigateBack: () -> Unit,
    onForceRescan: () -> Unit,
    onToggleBroadcast: () -> Unit,
    onPeerClicked: (MeshPeer) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ColorBackgroundDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorSurfaceDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = MeshCyan
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = TextPrimary
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "OFFLINE MESH TOPOLOGY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.peers.isNotEmpty()) EmergencyGreen else EmergencyOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.peers.isNotEmpty()) {
                                    "${uiState.peers.size} PEERS IN RANGE • DATA MULE ACTIVE"
                                } else {
                                    "SEARCHING OPPORUNISTIC RELAYS..."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (uiState.peers.isNotEmpty()) EmergencyGreen else EmergencyOrange
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onForceRescan,
                        enabled = !uiState.isScanning,
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MeshCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Force mesh radio rescan",
                                tint = MeshCyan
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MeshTelemetryCard(
                    packetsCarried = uiState.packetsCarriedCount,
                    peerCount = uiState.peers.size,
                    isBroadcasting = uiState.isBroadcasting,
                    isBatterySaver = uiState.isBatterySaverActive,
                    onToggleBroadcast = onToggleBroadcast
                )
            }

            item {
                GossipBufferCard(
                    packetsCarried = uiState.packetsCarriedCount,
                    maxCapacity = uiState.maxBufferCapacity,
                    trappedCount = uiState.trappedCount,
                    injuredCount = uiState.injuredCount,
                    needsMedsCount = uiState.needsMedsCount,
                    safeCount = uiState.safeCount
                )
            }

            item {
                SectionHeader(
                    title = "Nearby Mesh Relays",
                    icon = Icons.Default.Hub,
                    badgeText = "${uiState.peers.size} In Range",
                    subtitle = "Opportunistic BLE and Wi-Fi Direct gossip radio contacts"
                )
            }

            if (uiState.peers.isEmpty()) {
                item {
                    EmptyState(
                        title = "No Nearby Mesh Peers Detected",
                        message = "Your phone is broadcasting offline BLE beacon chirps. As other data mules, citizens, or rescuers enter proximity, they will appear automatically.",
                        actionLabel = "Force Radio Scan",
                        onActionClick = onForceRescan
                    )
                }
            } else {
                items(uiState.peers) { peer ->
                    PeerCard(
                        peer = peer,
                        onClick = { onPeerClicked(peer) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    title = "Live Gossip Sync Log",
                    icon = Icons.Default.SwapHoriz,
                    badgeText = "Real-time",
                    badgeColor = EmergencyGreen,
                    subtitle = "Hop-by-hop epidemic packet replication events"
                )
            }

            item {
                SyncLogCard(logs = uiState.syncLogs)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MeshTelemetryCard(
    packetsCarried: Int,
    peerCount: Int,
    isBroadcasting: Boolean,
    isBatterySaver: Boolean,
    onToggleBroadcast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ColorSurfaceElevatedDark)
                            .border(1.dp, MeshCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MeshCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DATA MULE STORE",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "$packetsCarried Epidemic Packets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isBroadcasting) EmergencyGreen.copy(alpha = 0.15f) else ColorSurfaceElevatedDark
                ) {
                    Text(
                        text = if (isBroadcasting) "RELAY ACTIVE" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isBroadcasting) EmergencyGreen else TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryMiniTile(
                    label = "Carried Pkts",
                    value = packetsCarried.toString(),
                    accentColor = MeshCyan,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMiniTile(
                    label = "Direct Links",
                    value = peerCount.toString(),
                    accentColor = EmergencyGreen,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMiniTile(
                    label = "Duty Cycle",
                    value = if (isBatterySaver) "5s / 25s" else "100%",
                    accentColor = EmergencyYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryMiniTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ColorSurfaceElevatedDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun GossipBufferCard(
    packetsCarried: Int,
    maxCapacity: Int,
    trappedCount: Int,
    injuredCount: Int,
    needsMedsCount: Int,
    safeCount: Int,
    modifier: Modifier = Modifier
) {
    val usageRatio = (packetsCarried.toFloat() / maxCapacity.coerceAtLeast(1)).coerceIn(0f, 1f)
    val usagePercent = (usageRatio * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GOSSIP STORE & TRIAGE CACHE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MeshCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "EPIDEMIC BUFFER",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MeshCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Buffer Utilization header + Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Local Mule Storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$packetsCarried / $maxCapacity packets ($usagePercent%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MeshCyan
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Custom Sleek Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(ColorSurfaceElevatedDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(usageRatio.coerceAtLeast(0.02f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (usagePercent > 80) EmergencyOrange else MeshCyan)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Triage Breakdown Mini Grid (4 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TriageStatChip(
                    count = trappedCount,
                    label = "TRAPPED",
                    color = EmergencyRed,
                    modifier = Modifier.weight(1f)
                )
                TriageStatChip(
                    count = injuredCount,
                    label = "INJURED",
                    color = EmergencyOrange,
                    modifier = Modifier.weight(1f)
                )
                TriageStatChip(
                    count = needsMedsCount,
                    label = "MEDS/WATER",
                    color = EmergencyYellow,
                    modifier = Modifier.weight(1f)
                )
                TriageStatChip(
                    count = safeCount,
                    label = "SAFE",
                    color = EmergencyGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Policy Tag Footer
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = ColorSurfaceElevatedDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ROUTING RULES: Highest Urgency First • TTL 10 Hops • Newest Wins",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TriageStatChip(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = ColorSurfaceElevatedDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SyncLogCard(
    logs: List<MeshSyncLogEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            logs.take(8).forEachIndexed { index, entry ->
                SyncLogItemRow(entry = entry)
                if (index < logs.take(8).size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ColorSurfaceBorder.copy(alpha = 0.5f))
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncLogItemRow(
    entry: MeshSyncLogEntry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (entry.isOutbound) MeshCyan.copy(alpha = 0.15f) else EmergencyGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (entry.isOutbound) Icons.Default.Upload else Icons.Default.Download,
                    contentDescription = null,
                    tint = if (entry.isOutbound) MeshCyan else EmergencyGreen,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.peerAlias,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.timestampFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
                Text(
                    text = entry.eventType,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isOutbound) MeshCyan else EmergencyGreen,
                    fontSize = 10.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.rssi} dBm",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = when {
                    entry.rssi >= -65 -> EmergencyGreen
                    entry.rssi >= -75 -> MeshCyan
                    else -> EmergencyYellow
                }
            )
            if (entry.packetCount > 0) {
                Text(
                    text = "+${entry.packetCount} pkts",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeshNetworkScreenPreview() {
    Surface(color = ColorBackgroundDark) {
        MeshNetworkScreen(
            uiState = MeshNetworkUiState(
                peers = emptyList(),
                packetsCarriedCount = 28,
                isBroadcasting = true,
                isBatterySaverActive = true,
                syncLogs = emptyList()
            ),
            onNavigateBack = {},
            onForceRescan = {},
            onToggleBroadcast = {},
            onPeerClicked = {}
        )
    }
}
