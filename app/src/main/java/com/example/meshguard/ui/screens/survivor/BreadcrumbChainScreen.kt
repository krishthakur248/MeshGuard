package com.example.meshguard.ui.screens.survivor

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
import com.example.meshguard.ui.viewmodel.BreadcrumbChainUiState
import com.example.meshguard.ui.viewmodel.BreadcrumbChainViewModel
import com.example.meshguard.ui.viewmodel.TimelineHopItem

@Composable
fun BreadcrumbChainRoute(
    onNavigateBack: () -> Unit,
    viewModel: BreadcrumbChainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BreadcrumbChainScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRefreshChain = viewModel::onRefreshProximityChain
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreadcrumbChainScreen(
    uiState: BreadcrumbChainUiState,
    onNavigateBack: () -> Unit,
    onRefreshChain: () -> Unit,
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
                            text = "PROXIMITY CONTACT CHAIN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(EmergencyGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ZERO-GPS PROXIMITY CHAIN • ${uiState.totalHops} HOPS TO RESCUE",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = EmergencyGreen
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefreshChain,
                        enabled = !uiState.isVerifyingChain,
                        modifier = Modifier.size(56.dp)
                    ) {
                        if (uiState.isVerifyingChain) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MeshCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh hop chain verification",
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
                ChainSummaryCard(uiState = uiState)
            }

            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ColorSurfaceElevatedDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ColorSurfaceDark)
                                .border(1.dp, MeshCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                tint = MeshCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "DECENTRALIZED RELATIVE LOCALIZATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Without GPS or cell towers, rescuers establish survivor search vectors by tracing chronological BLE radio contact handshakes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Hop-by-Hop Relay Trail",
                    icon = Icons.Default.Timeline,
                    badgeText = "${uiState.chainHops.size} Nodes",
                    badgeColor = MeshCyan,
                    subtitle = "Chronological vector from origin survivor to rescue command post"
                )
            }

            itemsIndexed(uiState.chainHops, key = { _, item -> item.id }) { index, hop ->
                TimelineHopRow(
                    hop = hop,
                    isFirst = index == 0,
                    isLast = index == uiState.chainHops.size - 1
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ChainSummaryCard(
    uiState: BreadcrumbChainUiState,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Proximity chain summary: ${uiState.originSector} to ${uiState.destinationSector}, ${uiState.totalHops} hops over ${uiState.totalTimeSpanMinutes} minutes."
            }
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
                Column {
                    Text(
                        text = "ORIGIN QUADRANT",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Text(
                        text = uiState.originSector,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = EmergencyRed
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(32.dp)
                            .background(ColorSurfaceBorder)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MeshCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${uiState.totalHops} HOPS",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MeshCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(32.dp)
                            .background(ColorSurfaceBorder)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TARGET DESTINATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Text(
                        text = uiState.destinationSector,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = EmergencyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChainMetricTile(
                    label = "Total Hops",
                    value = "${uiState.totalHops} Hops",
                    accentColor = MeshCyan,
                    modifier = Modifier.weight(1f)
                )

                ChainMetricTile(
                    label = "Time Elapsed",
                    value = "${uiState.totalTimeSpanMinutes}m ago",
                    accentColor = EmergencyYellow,
                    modifier = Modifier.weight(1f)
                )

                ChainMetricTile(
                    label = "Est. Radius",
                    value = "~${uiState.estimatedDistanceMeters}m",
                    accentColor = EmergencyGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChainMetricTile(
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
private fun TimelineHopRow(
    hop: TimelineHopItem,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val (nodeColor, icon) = when {
        hop.isLocalDevice -> EmergencyRed to Icons.Default.Person
        hop.isDestination -> EmergencyGreen to Icons.Default.Shield
        hop.isAnchorOrBeacon -> MeshCyan to Icons.Default.CellTower
        else -> EmergencyYellow to Icons.Default.Hub
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTimeline")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp)
            ) {
                if (hop.isLocalDevice || hop.isDestination) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(nodeColor.copy(alpha = 0.2f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ColorSurfaceDark)
                        .border(2.dp, nodeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = nodeColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(ColorSurfaceBorder)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (hop.isLocalDevice) EmergencyRed.copy(alpha = 0.6f) else ColorSurfaceBorder
            ),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 14.dp)
                .semantics {
                    contentDescription = "Hop ${hop.hopIndex}: ${hop.deviceAlias}, role ${hop.roleBadge}, reported ${hop.timeAgoFormatted}, signal strength ${hop.rssi} dBm. Note: ${hop.notes}"
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = nodeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "HOP ${hop.hopIndex}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = nodeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = hop.deviceAlias,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = hop.timeAgoFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = hop.roleBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = nodeColor,
                        fontSize = 10.sp
                    )

                    Text(text = "•", color = TextMuted, fontSize = 10.sp)

                    Text(
                        text = "Contact at ${hop.timestampExact}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = hop.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RSSI Link Strength",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SignalStrengthBars(rssi = hop.rssi)

                        Text(
                            text = "${hop.rssi} dBm",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                hop.rssi >= -65 -> EmergencyGreen
                                hop.rssi >= -75 -> MeshCyan
                                else -> EmergencyYellow
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalStrengthBars(rssi: Int) {
    val (color, activeBars) = when {
        rssi >= -65 -> EmergencyGreen to 4
        rssi >= -75 -> MeshCyan to 3
        rssi >= -85 -> EmergencyYellow to 2
        else -> EmergencyRed to 1
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(14.dp)
    ) {
        for (i in 1..4) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((3.5 * i).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (i <= activeBars) color else ColorSurfaceBorder)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BreadcrumbChainScreenPreview() {
    Surface(color = ColorBackgroundDark) {
        BreadcrumbChainScreen(
            uiState = BreadcrumbChainUiState(
                originSector = "SEC-4B",
                destinationSector = "SEC-4 (Safe Haven)",
                totalHops = 4,
                totalTimeSpanMinutes = 32,
                estimatedDistanceMeters = 450,
                chainHops = emptyList()
            ),
            onNavigateBack = {},
            onRefreshChain = {}
        )
    }
}
