package com.talos.forge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF000014),
    primaryContainer = AccentLight,
    secondary = AccentDark,
    background = BgLight,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = Surface,
    onSurfaceVariant = TextSecondary,
    error = Danger,
    outline = Border
)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF000014),
    primaryContainer = Color(0xFF282828),
    secondary = Color(0xFF888888),
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = CardBgDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    error = Danger,
    outline = BorderDark
)

@Composable
fun TalosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
