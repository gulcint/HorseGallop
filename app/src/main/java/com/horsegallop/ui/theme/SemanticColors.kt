package com.horsegallop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SemanticColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val destructive: Color,
    val imageOverlayStrong: Color,
    val imageOverlaySoft: Color,
    val cardElevated: Color,
    val cardSubtle: Color,
    val chipSelected: Color,
    val chipUnselected: Color,
    val mapGrid: Color,
    val mapPin: Color,
    val ratingStar: Color,
    val onImageOverlay: Color
)

val LocalSemanticColors = staticCompositionLocalOf {
    SemanticColors(
        success = Color.Unspecified,
        warning = Color.Unspecified,
        info = Color.Unspecified,
        destructive = Color.Unspecified,
        imageOverlayStrong = Color.Unspecified,
        imageOverlaySoft = Color.Unspecified,
        cardElevated = Color.Unspecified,
        cardSubtle = Color.Unspecified,
        chipSelected = Color.Unspecified,
        chipUnselected = Color.Unspecified,
        mapGrid = Color.Unspecified,
        mapPin = Color.Unspecified,
        ratingStar = Color.Unspecified,
        onImageOverlay = Color.Unspecified
    )
}

fun semanticColorsFrom(colorScheme: ColorScheme, isDark: Boolean): SemanticColors {
    val overlayStrongAlpha = if (isDark) 0.58f else 0.48f
    val overlaySoftAlpha = if (isDark) 0.24f else 0.14f
    return SemanticColors(
        success = if (isDark) Color(0xFF74D38B) else Color(0xFF2F7A3E),
        warning = if (isDark) Color(0xFFF0BC72) else Color(0xFFB86C1D),
        info = if (isDark) Color(0xFF8EB9E0) else Color(0xFF356C9B),
        destructive = colorScheme.error,
        imageOverlayStrong = Color.Black.copy(alpha = overlayStrongAlpha),
        imageOverlaySoft = Color.Black.copy(alpha = overlaySoftAlpha),
        cardElevated = if (isDark) colorScheme.surface else Color.White,
        cardSubtle = if (isDark) {
            colorScheme.surfaceVariant.copy(alpha = 0.34f)
        } else {
            Color.White.copy(alpha = 0.78f)
        },
        chipSelected = colorScheme.primaryContainer,
        chipUnselected = if (isDark) colorScheme.surface else Color.White,
        mapGrid = colorScheme.onSurface.copy(alpha = if (isDark) 0.18f else 0.04f),
        mapPin = colorScheme.primary,
        ratingStar = if (isDark) Color(0xFFFFD47A) else Color(0xFFFFB74D),
        onImageOverlay = Color.White
    )
}
