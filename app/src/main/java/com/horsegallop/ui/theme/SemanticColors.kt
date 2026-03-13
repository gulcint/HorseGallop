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
    val badgeNeutral: Color,
    val badgePositive: Color,
    val badgeWarning: Color,
    val stateOverlaySuccess: Color,
    val stateOverlayWarning: Color,
    val stateOverlayInfo: Color,
    val imageOverlayStrong: Color,
    val imageOverlaySoft: Color,
    val panelOverlay: Color,
    val cardElevated: Color,
    val cardSubtle: Color,
    val cardStroke: Color,
    val dividerStrong: Color,
    val dividerSoft: Color,
    val chipSelected: Color,
    val chipUnselected: Color,
    val mapGrid: Color,
    val mapPin: Color,
    val ratingStar: Color,
    val onImageOverlay: Color,
    val calloutInfoContainer: Color,
    val calloutSuccessContainer: Color,
    val calloutWarningContainer: Color,
    val calloutErrorContainer: Color,
    val calloutOnContainer: Color,
    val calloutBorderInfo: Color,
    val calloutBorderSuccess: Color,
    val calloutBorderWarning: Color,
    val calloutBorderError: Color,
    // Gait colors for ride polyline
    val gaitWalk: Color,
    val gaitTrot: Color,
    val gaitCanter: Color
)

val LocalSemanticColors = staticCompositionLocalOf {
    semanticColorsFrom(LightColorScheme, isDark = false)
}

fun semanticColorsFrom(colorScheme: ColorScheme, isDark: Boolean): SemanticColors {
    // Semantic tones derived from the White Elegance + Saddle Brown palette
    val successTone     = if (isDark) Color(0xFF6FCF97) else Color(0xFF27AE60)   // clean green
    val warningTone     = if (isDark) Color(0xFFFFB74D) else SaddleBrown          // saddle brown warmth
    val infoTone        = if (isDark) Color(0xFF90A4AE) else Color(0xFF455A64)    // cool blue-grey
    // Replace harsh red with warm amber — keeps "caution" feel without red
    val destructiveTone = if (isDark) Color(0xFFE8A000) else Color(0xFFB45309)    // warm amber/burnt sienna

    return SemanticColors(
        screenBase = colorScheme.background,
        screenTopBar = if (isDark) colorScheme.surface else colorScheme.surfaceContainerLow,
        success = successTone,
        warning = warningTone,
        info = infoTone,
        destructive = destructiveTone,
        badgeNeutral = colorScheme.surfaceContainerHigh,
        badgePositive = if (isDark) colorScheme.tertiaryContainer else colorScheme.secondaryContainer,
        badgeWarning = if (isDark) colorScheme.primaryContainer else colorScheme.primaryContainer.copy(alpha = 0.85f),
        stateOverlaySuccess = successTone.copy(alpha = if (isDark) 0.26f else 0.16f),
        stateOverlayWarning = warningTone.copy(alpha = if (isDark) 0.26f else 0.14f),
        stateOverlayInfo = infoTone.copy(alpha = if (isDark) 0.24f else 0.14f),
        imageOverlayStrong = colorScheme.scrim.copy(alpha = if (isDark) 0.58f else 0.46f),
        imageOverlaySoft = colorScheme.scrim.copy(alpha = if (isDark) 0.28f else 0.18f),
        panelOverlay = colorScheme.surfaceContainer,
        cardElevated = if (isDark) colorScheme.surfaceContainerLow else colorScheme.surfaceContainerLowest,
        cardSubtle = if (isDark) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLow,
        cardStroke = colorScheme.outlineVariant.copy(alpha = if (isDark) 0.62f else 0.46f),
        dividerStrong = colorScheme.outline.copy(alpha = if (isDark) 0.72f else 0.52f),
        dividerSoft = colorScheme.outlineVariant.copy(alpha = if (isDark) 0.46f else 0.34f),
        chipSelected = colorScheme.primaryContainer,
        chipUnselected = if (isDark) colorScheme.surfaceContainer else colorScheme.surfaceContainerLow,
        mapGrid = colorScheme.onSurface.copy(alpha = if (isDark) 0.16f else 0.08f),
        mapPin = colorScheme.primary,
        ratingStar = colorScheme.secondary,
        onImageOverlay = colorScheme.inverseOnSurface,
        calloutInfoContainer = colorScheme.secondaryContainer,
        calloutSuccessContainer = colorScheme.tertiaryContainer,
        calloutWarningContainer = colorScheme.primaryContainer,
        calloutErrorContainer = destructiveTone.copy(alpha = if (isDark) 0.22f else 0.12f),
        calloutOnContainer = colorScheme.onSurface,
        calloutBorderInfo = infoTone,
        calloutBorderSuccess = successTone,
        calloutBorderWarning = warningTone,
        calloutBorderError = destructiveTone,
        gaitWalk   = if (isDark) Color(0xFF80C4EE) else Color(0xFF3A8BD1),
        gaitTrot   = successTone,
        gaitCanter = if (isDark) Color(0xFFFF9842) else Color(0xFFE07B39)
    )
}
