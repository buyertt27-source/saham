package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LiquidGlassDarkColorScheme = darkColorScheme(
    primary = IcyBlueAccent,
    onPrimary = DeepObsidian,
    primaryContainer = TranslucentGraphiteElevated,
    onPrimaryContainer = SoftWhite,
    secondary = SilverReflection,
    onSecondary = DeepObsidian,
    secondaryContainer = GlassSurfaceHigh,
    onSecondaryContainer = SoftWhite,
    tertiary = EmeraldBullish,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = SoftWhite,
    surface = CharcoalBase,
    onSurface = SoftWhite,
    surfaceVariant = TranslucentGraphite,
    onSurfaceVariant = CoolGray,
    error = SoftRedBearish,
    onError = SoftWhite
)

@Composable
fun GlobalStockIntelligenceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LiquidGlassDarkColorScheme,
        typography = Typography,
        content = content
    )
}
