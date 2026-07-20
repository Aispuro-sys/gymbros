package com.talos.forge.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.talos.forge.data.AppSettings

object AppColors {
    // Base surfaces — dynamic dark/light
    val bg: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF000000) else Color(0xFFFFFFFF)

    val bg2: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)

    val cardBg: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF111111) else Color(0xFFFFFFFF)

    val cardBgAlt: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF1A1A1A) else Color(0xFFF4F4F4)

    val cardBgSubtle: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF161616) else Color(0xFFF0F0F0)

    val textPrimary: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFFFFFFFF) else Color(0xFF0A0A0A)

    val textSecondary: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFFA3A3A3) else Color(0xFF525252)

    val textTertiary: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF6B6B6B) else Color(0xFF9E9E9E)

    val textOnAccent: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF000000) else Color(0xFFFFFFFF)

    // Accent: black on light, white on dark
    val accent: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFFFFFFFF) else Color(0xFF0A0A0A)

    val accentDark: Color
        @Composable @ReadOnlyComposable get() = Color(0xFF000000)

    val accentLight: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFFE0E0E0) else Color(0xFF262626)

    val accentMuted: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    // Semantic colors
    val success: Color
        @Composable @ReadOnlyComposable get() = Color(0xFF16A34A)

    val danger: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFDC2626)

    val warning: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFB45309)

    val info: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFFB0B0B0) else Color(0xFF404040)

    val border: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f)

    val borderStrong: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color.White.copy(alpha = 0.24f) else Color.Black.copy(alpha = 0.20f)

    val gradientStart: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF1A1A1A) else Color(0xFFF4F4F4)

    val gradientEnd: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color(0xFF0A0A0A) else Color(0xFFFFFFFF)

    val chipBg: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)

    val divider: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f)

    val onSurfaceInverse: Color
        @Composable @ReadOnlyComposable get() = if (AppSettings.isDarkMode.value) Color.Black else Color.White
}
