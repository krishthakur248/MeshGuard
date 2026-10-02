package com.example.meshguard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val DarkColorScheme = darkColorScheme(
    primary = MeshCyan,
    onPrimary = ColorBackgroundDark,
    primaryContainer = ColorSurfaceElevatedDark,
    onPrimaryContainer = TextPrimary,
    secondary = EmergencyOrange,
    onSecondary = ColorBackgroundDark,
    tertiary = EmergencyGreen,
    onTertiary = ColorBackgroundDark,
    background = ColorBackgroundDark,
    onBackground = TextPrimary,
    surface = ColorSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = ColorSurfaceElevatedDark,
    onSurfaceVariant = TextSecondary,
    outline = ColorSurfaceBorder,
    error = EmergencyRed,
    onError = ColorBackgroundDark
)

@Composable
fun MeshGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MeshTypography,
        content = content
    )
}
