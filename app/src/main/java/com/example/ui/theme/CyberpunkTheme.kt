package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tactical Neon Green & Deep Charcoal Cyberpunk Palette
val DeepCharcoalBg = Color(0xFF0D1117)
val DeepCharcoalSurface = Color(0xFF161B22)
val DeepCharcoalElevated = Color(0xFF212836)
val TacticalSlateBorder = Color(0xFF30363D)

val NeonAcidGreen = Color(0xFF39FF14)
val NeonMatrixEmerald = Color(0xFF00FF66)
val CyberCyanAccent = Color(0xFF00F0FF)
val TacticalAmber = Color(0xFFFFB300)
val CriticalRed = Color(0xFFFF3366)

val MonospaceTextPrimary = Color(0xFFE6EDF3)
val MonospaceTextSecondary = Color(0xFF8B949E)

private val CyberpunkDarkColorScheme = darkColorScheme(
    primary = NeonAcidGreen,
    onPrimary = DeepCharcoalBg,
    primaryContainer = DeepCharcoalElevated,
    onPrimaryContainer = NeonAcidGreen,
    secondary = CyberCyanAccent,
    onSecondary = DeepCharcoalBg,
    secondaryContainer = DeepCharcoalElevated,
    onSecondaryContainer = CyberCyanAccent,
    tertiary = NeonMatrixEmerald,
    onTertiary = DeepCharcoalBg,
    background = DeepCharcoalBg,
    onBackground = MonospaceTextPrimary,
    surface = DeepCharcoalSurface,
    onSurface = MonospaceTextPrimary,
    surfaceVariant = DeepCharcoalElevated,
    onSurfaceVariant = MonospaceTextSecondary,
    outline = TacticalSlateBorder,
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun CyberpunkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberpunkDarkColorScheme,
        typography = Typography,
        content = content
    )
}
