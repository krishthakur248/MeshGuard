package com.example.meshguard.ui.screens.rescuer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.R
import com.example.meshguard.data.model.UrgencyStatus
import com.example.meshguard.ui.components.EmptyState
import com.example.meshguard.ui.components.SectionHeader
import com.example.meshguard.ui.components.SurvivorListItem
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
import com.example.meshguard.ui.viewmodel.ResponderDashboardUiState
import com.example.meshguard.ui.viewmodel.ResponderDashboardViewModel
import com.example.meshguard.ui.viewmodel.UrgencyFilter

@Composable
fun ResponderDashboardRoute(
    onNavigateToSurvivorDetail: (String) -> Unit,
    onNavigateToMap: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ResponderDashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ResponderDashboardScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSurvivorClick = onNavigateToSurvivorDetail,
        onClearQueue = viewModel::clearQueue,
        onNavigateToMap = onNavigateToMap,
        modifier = modifier
    )
}

@Composable
fun ResponderDashboardScreen(
    uiState: ResponderDashboardUiState,
    onFilterSelected: (UrgencyFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSurvivorClick: (String) -> Unit,
    onClearQueue: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = ColorSurfaceDark,
            title = {
                Text(
                    text = "Clear Casualty Queue?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This will delete all received survivor packets from local storage so you can start a fresh test.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearQueue()
                    }
                ) {
                    Text("Clear All", color = EmergencyRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tactical Header: Incident Command
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RescuerBadgeBlue.copy(alpha = 0.15f))
                        .border(1.dp, RescuerBadgeBlue.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = RescuerBadgeBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.nav_responder_dashboard),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "INCIDENT COMMAND • OFFLINE MESH",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = RescuerBadgeBlue,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Radio Peer Indicator Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ColorSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.peerCount > 0) EmergencyGreen else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.peerCount} PEERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Triage Stats Cards Row — 3 cards: Active, Critical, Handled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriageStatCard(
                    label = "ACTIVE",
                    value = uiState.activeSurvivors.size.toString(),
                    accentColor = MeshCyan,
                    modifier = Modifier.weight(1f)
                )
                TriageStatCard(
                    label = "CRITICAL",
                    value = uiState.criticalCount.toString(),
                    accentColor = EmergencyRed,
                    modifier = Modifier.weight(1f)
                )
                TriageStatCard(
                    label = "HANDLED",
                    value = uiState.acknowledgedSurvivors.size.toString(),
                    accentColor = EmergencyGreen,
                    modifier = Modifier.weight(1f)
                )
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search by survivor name, ID, or sector...",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorSurfaceDark,
                unfocusedContainerColor = ColorSurfaceDark,
                focusedBorderColor = RescuerBadgeBlue,
                unfocusedBorderColor = ColorSurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UrgencyFilter.values().forEach { filter ->
                val isSelected = uiState.selectedFilter == filter
                val chipColor = when (filter.targetStatus) {
                    UrgencyStatus.TRAPPED -> EmergencyRed
                    UrgencyStatus.INJURED -> EmergencyOrange
                    UrgencyStatus.NEEDS_INSULIN -> EmergencyYellow
                    UrgencyStatus.NEED_WATER -> Color(0xFF00B0FF)
                    UrgencyStatus.SAFE -> EmergencyGreen
                    UrgencyStatus.UNKNOWN -> TextSecondary
                    null -> RescuerBadgeBlue
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) chipColor.copy(alpha = 0.2f) else ColorSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) chipColor else ColorSurfaceBorder
                    ),
                    modifier = Modifier
                        .clickable { onFilterSelected(filter) }
                ) {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) chipColor else TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val totalMatches = uiState.activeSurvivors.size + uiState.acknowledgedSurvivors.size

        // Section Header: Live Casualty List with Clear All button on the right
        SectionHeader(
            title = "Casualty Queue",
            icon = Icons.Default.FilterList,
            badgeText = "$totalMatches",
            badgeColor = RescuerBadgeBlue,
            trailingAction = {
                if (uiState.totalCount > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showClearDialog = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear All",
                            tint = EmergencyRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRed,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Casualty List or Empty State
        if (uiState.totalCount == 0 || (uiState.activeSurvivors.isEmpty() && uiState.acknowledgedSurvivors.isEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = if (uiState.totalCount == 0) "No Distress Packets Received" else "No Matching Casualties",
                    message = if (uiState.totalCount == 0)
                        "Listening on Nearby mesh radio... Packets will appear automatically as soon as survivor devices broadcast within range or relay through mules."
                    else
                        "No casualties match the selected status filter or search query.",
                    icon = Icons.Default.SensorsOff
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. ACTIVE / PENDING CASUALTIES SECTION
                if (uiState.activeSurvivors.isNotEmpty()) {
                    items(
                        items = uiState.activeSurvivors,
                        key = { it.survivorId.ifBlank { it.packetId } }
                    ) { survivor ->
                        SurvivorListItem(
                            survivor = survivor,
                            onClick = { onSurvivorClick(survivor.survivorId) }
                        )
                    }
                } else if (uiState.acknowledgedSurvivors.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ColorSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmergencyGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All active casualties in this view have been acknowledged.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmergencyGreen
                                )
                            }
                        }
                    }
                }

                // 2. SEPARATE ACKNOWLEDGED / HANDLED SECTION
                if (uiState.acknowledgedSurvivors.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionHeader(
                            title = "Handled / Dispatched",
                            icon = Icons.Default.CheckCircle,
                            iconTint = EmergencyGreen,
                            badgeText = "${uiState.acknowledgedSurvivors.size}",
                            badgeColor = EmergencyGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(
                        items = uiState.acknowledgedSurvivors,
                        key = { "ack_${it.survivorId.ifBlank { it.packetId }}" }
                    ) { survivor ->
                        SurvivorListItem(
                            survivor = survivor,
                            onClick = { onSurvivorClick(survivor.survivorId) }
                        )
                    }
                }
            }
        }
    }

        // Circular floating map button on bottom right, directly above the Mesh Messages tab
        FloatingActionButton(
            onClick = onNavigateToMap,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            shape = CircleShape,
            containerColor = RescuerBadgeBlue,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = "Open Survivor Map",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TriageStatCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, ColorSurfaceBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
        ) {
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
