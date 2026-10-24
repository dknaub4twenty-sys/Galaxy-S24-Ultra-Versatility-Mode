package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = TacticalObsidian,
    primaryContainer = TacticalSurfaceElevated,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = TacticalSurfaceElevated,
    onSecondaryContainer = ElectricViolet,
    tertiary = MatrixEmerald,
    onTertiary = TacticalObsidian,
    background = TacticalObsidian,
    onBackground = TerminalText,
    surface = TacticalSurface,
    onSurface = TerminalText,
    surfaceVariant = TacticalSurfaceElevated,
    onSurfaceVariant = TerminalTextMuted,
    outline = TacticalBorder,
    error = AlertCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TacticalDarkColorScheme,
        typography = Typography,
        content = content
    )
}
