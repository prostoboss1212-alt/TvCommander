package com.example.tvcommander.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyanPalette = darkColorScheme(
    primary = AccentCyan,
    onPrimary = DarkBackground,
    primaryContainer = ControlButtonBackground,
    secondary = AccentAzure,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = PowerRed,
    outline = ControlButtonBorder
)

val AmethystPalette = darkColorScheme(
    primary = Color(0xFFC084FC),
    onPrimary = Color(0xFF0D0B14),
    primaryContainer = Color(0xFF221A38),
    secondary = Color(0xFF818CF8),
    background = Color(0xFF0D0B14),
    surface = Color(0xFF161224),
    surfaceVariant = Color(0xFF1D1730),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = PowerRed,
    outline = Color(0xFF3B2D5C)
)

val EmeraldPalette = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF07120E),
    primaryContainer = Color(0xFF132B22),
    secondary = Color(0xFF06B6D4),
    background = Color(0xFF07120E),
    surface = Color(0xFF0E1F18),
    surfaceVariant = Color(0xFF142B21),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = PowerRed,
    outline = Color(0xFF1E4535)
)

val CrimsonPalette = darkColorScheme(
    primary = Color(0xFFFF4B4B),
    onPrimary = Color(0xFF120A0A),
    primaryContainer = Color(0xFF2B1616),
    secondary = Color(0xFFFF9500),
    background = Color(0xFF120A0A),
    surface = Color(0xFF1D1111),
    surfaceVariant = Color(0xFF261616),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = PowerRed,
    outline = Color(0xFF4A2323)
)

@Composable
fun TvCommanderTheme(
    themeName: String = "cyan",
    content: @Composable () -> Unit
) {
    val scheme = when (themeName.lowercase()) {
        "amethyst" -> AmethystPalette
        "emerald" -> EmeraldPalette
        "crimson" -> CrimsonPalette
        else -> CyanPalette
    }
    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
