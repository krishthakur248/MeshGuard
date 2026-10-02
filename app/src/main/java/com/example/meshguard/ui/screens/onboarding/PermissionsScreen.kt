package com.example.meshguard.ui.screens.onboarding

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meshguard.AppDependencies
import com.example.meshguard.R
import com.example.meshguard.data.model.UserRole
import com.example.meshguard.ui.AppStateManager
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
import com.example.meshguard.ui.viewmodel.MeshPermissionType
import com.example.meshguard.ui.viewmodel.PermissionItemState
import com.example.meshguard.ui.viewmodel.PermissionsUiState
import com.example.meshguard.ui.viewmodel.PermissionsViewModel

@Composable
fun PermissionsRoute(
    onNavigateToHome: () -> Unit,
    viewModel: PermissionsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAndInitializePermissions(context)
    }

    PermissionsScreen(
        uiState = uiState,
        onPermissionRequested = {
            viewModel.onPermissionsResult(context)
        },
        onGrantAllOrContinue = {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val isBtOn = btManager.adapter?.isEnabled == true
            
            val locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isLocOn = LocationManagerCompat.isLocationEnabled(locManager)

            if (!isBtOn) {
                Toast.makeText(context, "Please turn ON Bluetooth for offline mesh.", Toast.LENGTH_LONG).show()
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            } else if (!isLocOn) {
                Toast.makeText(context, "Please turn ON Location for offline mesh discovery.", Toast.LENGTH_LONG).show()
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else {
                viewModel.onCompletePermissions(context)
                AppDependencies.meshRepository.toggleBroadcast(true)
                onNavigateToHome()
            }
        }
    )
}

@Composable
fun PermissionsScreen(
    uiState: PermissionsUiState,
    onPermissionRequested: () -> Unit,
    onGrantAllOrContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSdk = Build.VERSION.SDK_INT

    val ungrantedPermissions = uiState.permissions
        .filter { it.isApplicableForDevice && !it.isGranted }
        .flatMap { it.requiredPermissions }
        .distinct()

    val batchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionRequested()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = ColorBackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorSurfaceElevatedDark)
                        .border(1.dp, MeshCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MeshCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "SECURITY & RADIOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MeshCyan,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = stringResource(R.string.perm_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            }

            Text(
                text = stringResource(R.string.perm_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ColorSurfaceElevatedDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Device OS: Android API $currentSdk (${
                            when {
                                currentSdk >= Build.VERSION_CODES.TIRAMISU -> "Android 13+ (Target Tier)"
                                currentSdk >= Build.VERSION_CODES.S -> "Android 12 / 12L (Granular BT)"
                                else -> "Android 8–11 (Legacy Location Scans)"
                            }
                        })",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.permissions) { item ->
                    PermissionCard(
                        item = item,
                        onPermissionGranted = onPermissionRequested
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Button(
                    onClick = {
                        if (ungrantedPermissions.isNotEmpty()) {
                            batchLauncher.launch(ungrantedPermissions.toTypedArray())
                        } else {
                            onGrantAllOrContinue()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.allEssentialGranted) EmergencyGreen else MeshCyan,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (uiState.allEssentialGranted) {
                                "ENTER MESHGUARD"
                            } else {
                                stringResource(R.string.perm_grant_all).uppercase()
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (uiState.allEssentialGranted) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    item: PermissionItemState,
    onPermissionGranted: () -> Unit
) {
    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionGranted()
    }

    val iconPair: Pair<ImageVector, Color> = when (item.type) {
        MeshPermissionType.BLUETOOTH -> Icons.Default.Bluetooth to MeshCyan
        MeshPermissionType.WIFI_DIRECT -> Icons.Default.Wifi to MeshCyan
        MeshPermissionType.LOCATION_LEGACY -> Icons.Default.LocationOn to EmergencyYellow
        MeshPermissionType.AUDIO_RECORD -> Icons.Default.Mic to EmergencyOrange
        MeshPermissionType.NOTIFICATIONS -> Icons.Default.Notifications to EmergencyRed
    }
    val icon = iconPair.first
    val tintColor = iconPair.second

    val titleStr = stringResource(item.titleRes)
    val descStr = stringResource(item.descriptionRes)
    val statusStr = if (item.isGranted) "Granted" else "Required"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isGranted) ColorSurfaceDark else ColorSurfaceElevatedDark
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (item.isGranted) EmergencyGreen.copy(alpha = 0.6f) else ColorSurfaceBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$titleStr. $descStr. Status: $statusStr"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (item.isGranted) EmergencyGreen.copy(alpha = 0.15f) else ColorSurfaceDark)
                        .border(1.dp, if (item.isGranted) EmergencyGreen else tintColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isGranted) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = if (item.isGranted) EmergencyGreen else tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleStr,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.apiNote,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (item.isGranted) EmergencyGreen else MeshCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (item.isGranted) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = EmergencyGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = EmergencyGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (!item.isApplicableForDevice) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ColorSurfaceDark
                    ) {
                        Text(
                            text = "NOT REQUIRED",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            if (item.requiredPermissions.isNotEmpty()) {
                                singleLauncher.launch(item.requiredPermissions.toTypedArray())
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tintColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ColorSurfaceDark,
                            contentColor = tintColor
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Grant",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descStr,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionsScreenPreview() {
    Surface(color = ColorBackgroundDark) {
        PermissionsScreen(
            uiState = PermissionsUiState(
                permissions = emptyList(),
                allEssentialGranted = false
            ),
            onPermissionRequested = {},
            onGrantAllOrContinue = {}
        )
    }
}
