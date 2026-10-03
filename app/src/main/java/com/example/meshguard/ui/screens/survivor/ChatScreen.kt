package com.example.meshguard.ui.screens.survivor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.R
import com.example.meshguard.data.model.ChatMessage
import com.example.meshguard.data.model.DeliveryState
import com.example.meshguard.data.model.MessageType
import com.example.meshguard.ui.components.AudioWaveformPreview
import com.example.meshguard.ui.theme.ColorBackgroundDark
import com.example.meshguard.ui.theme.ColorSurfaceBorder
import com.example.meshguard.ui.theme.ColorSurfaceDark
import com.example.meshguard.ui.theme.ColorSurfaceElevatedDark
import com.example.meshguard.ui.theme.EmergencyGreen
import com.example.meshguard.ui.theme.EmergencyOrange
import com.example.meshguard.ui.theme.EmergencyRed
import com.example.meshguard.ui.theme.MeshCyan
import com.example.meshguard.ui.theme.RescuerBadgeBlue
import com.example.meshguard.ui.theme.TextMuted
import com.example.meshguard.ui.theme.TextPrimary
import com.example.meshguard.ui.theme.TextSecondary
import com.example.meshguard.ui.viewmodel.ChatUiState
import com.example.meshguard.ui.viewmodel.ChatViewModel
import com.example.meshguard.ui.viewmodel.VoiceRecordingState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatRoute(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onInputTextChanged = viewModel::onInputTextChanged,
        onSendTextMessage = viewModel::onSendTextMessage,
        onSendQuickReply = viewModel::onSendQuickReply,
        onStartRecording = viewModel::onStartRecording,
        onStopRecording = { viewModel.onStopRecording(sendImmediately = true) },
        onCancelRecording = viewModel::onCancelRecording,
        onToggleVoiceMessagePlayback = viewModel::onToggleVoiceMessagePlayback
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onNavigateBack: () -> Unit,
    onInputTextChanged: (String) -> Unit,
    onSendTextMessage: () -> Unit,
    onSendQuickReply: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onToggleVoiceMessagePlayback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "CRISIS MESH CHAT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MeshCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "OFFLINE P2P",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MeshCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Gossip Relay • ${uiState.messages.size} Packets in Buffer",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = ColorSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick reply chips — only show when not recording
                    if (!uiState.recordingState.isRecording) {
                        QuickReplyChips(onSendQuickReply = onSendQuickReply)
                    }

                    if (uiState.recordingState.isRecording) {
                        ActiveVoiceRecordingBar(
                            recordingState = uiState.recordingState,
                            onCancel = onCancelRecording,
                            onSend = onStopRecording
                        )
                    } else {
                        StandardMessageInputBar(
                            text = uiState.inputText,
                            onTextChanged = onInputTextChanged,
                            onSendText = onSendTextMessage,
                            onStartRecording = onStartRecording,
                            onStopRecording = onStopRecording
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                color = ColorSurfaceElevatedDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Messages hop opportunistically through passing Data Mules without internet.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        isPlayingVoice = uiState.activePlayingVoiceMessageId == message.id,
                        onToggleVoicePlay = { onToggleVoiceMessagePlayback(message.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    isPlayingVoice: Boolean,
    onToggleVoicePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(message.timestamp))

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (!message.isFromMe) {
                if (message.senderId.startsWith("RES") || message.senderId.startsWith("MED")) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = RescuerBadgeBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (message.senderId.startsWith("RES") || message.senderId.startsWith("MED")) RescuerBadgeBlue else TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (message.type == MessageType.TEXT) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (message.isFromMe) 14.dp else 2.dp,
                    bottomEnd = if (message.isFromMe) 2.dp else 14.dp
                ),
                color = if (message.isFromMe) ColorSurfaceElevatedDark else ColorSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (message.isFromMe) MeshCyan.copy(alpha = 0.5f) else ColorSurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    lineHeight = 20.sp
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ColorSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (message.isFromMe) MeshCyan.copy(alpha = 0.6f) else EmergencyOrange.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth(0.88f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    AudioWaveformPreview(
                        durationSec = message.audioDurationSec,
                        isPlaying = isPlayingVoice,
                        onTogglePlay = onToggleVoicePlay,
                        activeColor = if (message.isFromMe) MeshCyan else EmergencyOrange
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        MessageDeliveryPill(
            hopCount = message.hopCount,
            deliveryState = message.deliveryState,
            isFromMe = message.isFromMe
        )
    }
}

@Composable
private fun MessageDeliveryPill(
    hopCount: Int,
    deliveryState: DeliveryState,
    isFromMe: Boolean
) {
    val triple: Triple<ImageVector, Color, String> = when (deliveryState) {
        DeliveryState.STORED -> Triple(Icons.Default.Schedule, TextMuted, "Stored locally")
        DeliveryState.RELAYED -> Triple(Icons.Default.Check, MeshCyan, "Relayed via $hopCount hops")
        DeliveryState.DELIVERED_TO_RESCUER -> Triple(Icons.Default.DoneAll, EmergencyGreen, "Delivered to Rescuer")
    }
    val stateIcon = triple.first
    val stateColor = triple.second
    val stateText = triple.third

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (hopCount == 0) "Direct" else "$hopCount hops",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "•", color = TextMuted, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = stateIcon,
                contentDescription = null,
                tint = stateColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stateText,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = stateColor,
                fontSize = 10.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick reply chips
// ─────────────────────────────────────────────────────────────────────────────

/** One-tap emergency phrases shown above the text input. */
private val QUICK_REPLIES = listOf(
    "I'm OK" to EmergencyGreen,
    "Need water" to MeshCyan,
    "Need food" to EmergencyOrange,
    "Need meds" to EmergencyRed,
    "Trapped" to EmergencyRed,
    "Coming to you" to RescuerBadgeBlue,
    "Send help" to EmergencyOrange,
    "All clear" to EmergencyGreen
)

@Composable
private fun QuickReplyChips(
    onSendQuickReply: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(QUICK_REPLIES) { (label, color) ->
            Surface(
                onClick = { onSendQuickReply(label) },
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                modifier = Modifier.semantics {
                    contentDescription = "Quick reply: $label"
                    role = Role.Button
                }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun StandardMessageInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendText: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            placeholder = { Text("Crisis broadcast message...", color = TextMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendText() }),
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MeshCyan,
                unfocusedBorderColor = ColorSurfaceBorder,
                focusedContainerColor = ColorSurfaceElevatedDark,
                unfocusedContainerColor = ColorSurfaceElevatedDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (text.isNotBlank()) {
            IconButton(
                onClick = onSendText,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MeshCyan)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send text message",
                    tint = Color.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmergencyOrange.copy(alpha = 0.2f))
                    .border(1.dp, EmergencyOrange, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onStartRecording()
                                tryAwaitRelease()
                                onStopRecording()
                            }
                        )
                    }
                    .semantics {
                        contentDescription = "Hold to record emergency 15-second voice drop blackbox note"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = EmergencyOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveVoiceRecordingBar(
    recordingState: VoiceRecordingState,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceElevatedDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(EmergencyRed.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REC ${recordingState.durationSec}s / 15s",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = EmergencyRed
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                val amps = if (recordingState.liveAmplitudes.isEmpty()) {
                    listOf(0.3f, 0.6f, 0.4f, 0.8f, 0.5f, 0.7f, 0.3f)
                } else recordingState.liveAmplitudes

                amps.takeLast(12).forEach { amp ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((24.dp * amp).coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(2.dp))
                            .background(EmergencyOrange)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel recording",
                        tint = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmergencyGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Send 15s voice memo",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    Surface(color = ColorBackgroundDark) {
        ChatScreen(
            uiState = ChatUiState(
                messages = emptyList(),
                inputText = "",
                recordingState = VoiceRecordingState()
            ),
            onNavigateBack = {},
            onInputTextChanged = {},
            onSendTextMessage = {},
            onSendQuickReply = {},
            onStartRecording = {},
            onStopRecording = {},
            onCancelRecording = {},
            onToggleVoiceMessagePlayback = {}
        )
    }
}
