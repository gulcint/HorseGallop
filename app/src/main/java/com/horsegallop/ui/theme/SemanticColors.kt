package com.horsegallop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class SemanticColors(
    val screenBase: Color,
    val screenTopBar: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val destructive: Color,
    val imageOverlayStrong: Color,
    val imageOverlaySoft: Color,
    val panelOverlay: Color,
    val cardElevated: Color,
    val cardSubtle: Color,
    val cardStroke: Color,
    val chipSelected: Color,
    val chipUnselected: Color,
    val mapGrid: Color,
    val mapPin: Color,
    val ratingStar: Color,
    val onImageOverlay: Color
)

val LocalSemanticColors = staticCompositionLocalOf {
    semanticColorsFrom(LightColorScheme, isDark = false)
}

fun semanticColorsFrom(colorScheme: ColorScheme, isDark: Boolean): SemanticColors {
    val overlayStrongAlpha = if (isDark) 0.58f else 0.48f
    val overlaySoftAlpha = if (isDark) 0.24f else 0.14f
    return SemanticColors(
        screenBase = colorScheme.background,
        screenTopBar = if (isDark) colorScheme.surface else colorScheme.surfaceContainerLow,
        success = if (isDark) Color(0xFF74D38B) else Color(0xFF2F7A3E),
        warning = if (isDark) Color(0xFFF0BC72) else Color(0xFFB86C1D),
        info = if (isDark) Color(0xFF8EB9E0) else Color(0xFF356C9B),
        destructive = colorScheme.error,
        imageOverlayStrong = Color.Black.copy(alpha = overlayStrongAlpha),
        imageOverlaySoft = Color.Black.copy(alpha = overlaySoftAlpha),
        panelOverlay = colorScheme.surfaceContainer,
        cardElevated = if (isDark) colorScheme.surfaceContainerLow else colorScheme.surfaceContainerLowest,
        cardSubtle = if (isDark) {
            colorScheme.surfaceContainerHigh
        } else {
            colorScheme.surfaceContainerLow
        },
        cardStroke = colorScheme.outlineVariant.copy(alpha = if (isDark) 0.55f else 0.45f),
        chipSelected = colorScheme.primaryContainer,
        chipUnselected = if (isDark) {
            colorScheme.surfaceContainer
        } else {
            colorScheme.surfaceContainerLowest
        },
        mapGrid = colorScheme.onSurface.copy(alpha = if (isDark) 0.18f else 0.04f),
        mapPin = colorScheme.primary,
        ratingStar = if (isDark) Color(0xFFFFD47A) else Color(0xFFFFB74D),
        onImageOverlay = Color.White
    )
}
