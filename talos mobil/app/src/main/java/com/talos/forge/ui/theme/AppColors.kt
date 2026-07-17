package com.talos.forge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object AppColors {
    val bg: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF000014) else Color(0xFFF0F0F0)

    val cardBg: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF141414) else Color(0xFFFFFFFF)

    val cardBgAlt: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF282828) else Color(0xFFE8E8E8)

    val cardBgSubtle: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF282828) else Color(0xFFDCDCDC)

    val textPrimary: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFFF0F0F0) else Color(0xFF141414)

    val textSecondary: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF888888) else Color(0xFF555555)

    val textOnAccent: Color
        @Composable @ReadOnlyComposable get() = Color(0xFF000014)

    val accent: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFA0F03C)

    val accentDark: Color
        @Composable @ReadOnlyComposable get() = Color(0xFF8BD028)

    val accentLight: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFB8F56A)

    val accentMuted: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFFA0F03C).copy(alpha = 0.12f) else Color(0xFFA0F03C).copy(alpha = 0.10f)

    val success: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFA0F03C)

    val danger: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFFF3B30)

    val warning: Color
        @Composable @ReadOnlyComposable get() = Color(0xFFFF9500)

    val border: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF282828) else Color(0xFFD0D0D0)

    val gradientStart: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF141414) else Color(0xFF282828)

    val gradientEnd: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF000014) else Color(0xFF141414)

    val chipBg: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.06f) else Color(0xFFDCDCDC)

    val divider: Color
        @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) Color(0xFF282828) else Color(0xFFD0D0D0)
}
