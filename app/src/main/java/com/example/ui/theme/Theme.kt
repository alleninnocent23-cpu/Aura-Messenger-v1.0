package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LiquidGlassColorScheme = darkColorScheme(
    primary = GlassFillSolidPill,
    onPrimary = TextDarkPrimary,
    primaryContainer = GlassFillMedium,
    onPrimaryContainer = TextWhitePrimary,
    secondary = Color.White,
    onSecondary = TextDarkPrimary,
    secondaryContainer = GlassFillThin,
    onSecondaryContainer = TextWhiteSecondary,
    tertiary = GlassBorderBright,
    onTertiary = TextDarkPrimary,
    background = DeepMatteBlack,
    onBackground = TextWhitePrimary,
    surface = ObsidianBlack,
    onSurface = TextWhitePrimary,
    surfaceVariant = DarkCharcoal,
    onSurfaceVariant = TextWhiteMuted,
    outline = GlassBorderSubtle,
    outlineVariant = GlassBorderFaint
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LiquidGlassColorScheme,
        typography = Typography,
        content = content
    )
}
