package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.preferences.ThemeSetting

private val DarkColorScheme = darkColorScheme(
    primary = DarkPillSelectedBg,
    onPrimary = DarkPillSelectedText,
    primaryContainer = DarkSurfaceSubtle,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceSubtle,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CharcoalPrimary,
    onPrimary = Color.White,
    primaryContainer = CreamSurfaceSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = CharcoalSecondary,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = CreamSurface,
    onSurface = TextPrimary,
    surfaceVariant = CreamSurfaceSubtle,
    onSurfaceVariant = TextSecondary,
    outline = CreamCardBorder
)

private val PastelLavenderColorScheme = lightColorScheme(
    primary = PastelLavenderPrimary,
    onPrimary = Color.White,
    primaryContainer = PastelLavenderSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = PastelLavenderPrimary,
    onSecondary = Color.White,
    background = PastelLavenderBg,
    onBackground = TextPrimary,
    surface = PastelLavenderSurface,
    onSurface = TextPrimary,
    surfaceVariant = PastelLavenderSubtle,
    onSurfaceVariant = TextSecondary,
    outline = PastelLavenderBorder
)

private val PastelMintColorScheme = lightColorScheme(
    primary = PastelMintPrimary,
    onPrimary = Color.White,
    primaryContainer = PastelMintSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = PastelMintPrimary,
    onSecondary = Color.White,
    background = PastelMintBg,
    onBackground = TextPrimary,
    surface = PastelMintSurface,
    onSurface = TextPrimary,
    surfaceVariant = PastelMintSubtle,
    onSurfaceVariant = TextSecondary,
    outline = PastelMintBorder
)

private val PastelPeachColorScheme = lightColorScheme(
    primary = PastelPeachPrimary,
    onPrimary = Color.White,
    primaryContainer = PastelPeachSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = PastelPeachPrimary,
    onSecondary = Color.White,
    background = PastelPeachBg,
    onBackground = TextPrimary,
    surface = PastelPeachSurface,
    onSurface = TextPrimary,
    surfaceVariant = PastelPeachSubtle,
    onSurfaceVariant = TextSecondary,
    outline = PastelPeachBorder
)

private val PastelRoseColorScheme = lightColorScheme(
    primary = PastelRosePrimary,
    onPrimary = Color.White,
    primaryContainer = PastelRoseSubtle,
    onPrimaryContainer = TextPrimary,
    secondary = PastelRosePrimary,
    onSecondary = Color.White,
    background = PastelRoseBg,
    onBackground = TextPrimary,
    surface = PastelRoseSurface,
    onSurface = TextPrimary,
    surfaceVariant = PastelRoseSubtle,
    onSurfaceVariant = TextSecondary,
    outline = PastelRoseBorder
)

private val PastelSkyColorScheme = lightColorScheme(
    primary = PastelSkyPrimary,
    onPrimary = Color.White,
    primaryContainer = PastelSkySubtle,
    onPrimaryContainer = TextPrimary,
    secondary = PastelSkyPrimary,
    onSecondary = Color.White,
    background = PastelSkyBg,
    onBackground = TextPrimary,
    surface = PastelSkySurface,
    onSurface = TextPrimary,
    surfaceVariant = PastelSkySubtle,
    onSurfaceVariant = TextSecondary,
    outline = PastelSkyBorder
)

@Composable
fun MAXNTheme(
    themeSetting: ThemeSetting = ThemeSetting.LIGHT,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeSetting) {
        ThemeSetting.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        ThemeSetting.LIGHT -> LightColorScheme
        ThemeSetting.DARK -> DarkColorScheme
        ThemeSetting.PASTEL_LAVENDER -> PastelLavenderColorScheme
        ThemeSetting.PASTEL_MINT -> PastelMintColorScheme
        ThemeSetting.PASTEL_PEACH -> PastelPeachColorScheme
        ThemeSetting.PASTEL_ROSE -> PastelRoseColorScheme
        ThemeSetting.PASTEL_SKY -> PastelSkyColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
