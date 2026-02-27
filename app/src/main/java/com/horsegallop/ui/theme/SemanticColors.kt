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
    val calloutBorderError: Color
)

val LocalSemanticColors = staticCompositionLocalOf {
    semanticColorsFrom(LightColorScheme, isDark = false)
}

fun semanticColorsFrom(colorScheme: ColorScheme, isDark: Boolean): SemanticColors {
    val overlayStrongAlpha = if (isDark) 0.58f else 0.42f
    val overlaySoftAlpha = if (isDark) 0.24f else 0.16f
    val successColor = colorScheme.tertiary
    val warningColor = colorScheme.secondary
    val infoColor = colorScheme.primary

    return SemanticColors(
        screenBase = if (isDark) colorScheme.background else colorScheme.background,
        screenTopBar = if (isDark) colorScheme.surfaceContainer else colorScheme.surfaceContainerLow,
        success = successColor,
        warning = warningColor,
        info = infoColor,
        destructive = colorScheme.error,
        badgeNeutral = colorScheme.surfaceContainerHigh,
        badgePositive = colorScheme.tertiaryContainer.copy(alpha = if (isDark) 0.62f else 0.74f),
        badgeWarning = colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.62f else 0.74f),
        stateOverlaySuccess = successColor.copy(alpha = if (isDark) 0.24f else 0.14f),
        stateOverlayWarning = warningColor.copy(alpha = if (isDark) 0.24f else 0.14f),
        stateOverlayInfo = infoColor.copy(alpha = if (isDark) 0.24f else 0.14f),
        imageOverlayStrong = colorScheme.scrim.copy(alpha = overlayStrongAlpha),
        imageOverlaySoft = colorScheme.scrim.copy(alpha = overlaySoftAlpha),
        panelOverlay = if (isDark) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainer,
        cardElevated = if (isDark) colorScheme.surfaceContainerLow else colorScheme.surfaceContainerLowest,
        cardSubtle = if (isDark) colorScheme.surfaceContainer else colorScheme.surfaceContainerLow,
        cardStroke = colorScheme.outlineVariant.copy(alpha = if (isDark) 0.55f else 0.45f),
        dividerStrong = colorScheme.outline.copy(alpha = if (isDark) 0.62f else 0.44f),
        dividerSoft = colorScheme.outlineVariant.copy(alpha = if (isDark) 0.42f else 0.28f),
        chipSelected = colorScheme.primaryContainer,
        chipUnselected = if (isDark) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLow,
        mapGrid = colorScheme.onSurface.copy(alpha = if (isDark) 0.18f else 0.06f),
        mapPin = colorScheme.primary,
        ratingStar = colorScheme.secondary,
        onImageOverlay = colorScheme.inverseOnSurface,
        calloutInfoContainer = colorScheme.primaryContainer.copy(alpha = if (isDark) 0.52f else 0.82f),
        calloutSuccessContainer = colorScheme.tertiaryContainer.copy(alpha = if (isDark) 0.54f else 0.84f),
        calloutWarningContainer = colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.56f else 0.86f),
        calloutErrorContainer = colorScheme.errorContainer.copy(alpha = if (isDark) 0.56f else 0.88f),
        calloutOnContainer = colorScheme.onSurface,
        calloutBorderInfo = infoColor.copy(alpha = if (isDark) 0.82f else 0.70f),
        calloutBorderSuccess = successColor.copy(alpha = if (isDark) 0.82f else 0.70f),
        calloutBorderWarning = warningColor.copy(alpha = if (isDark) 0.82f else 0.70f),
        calloutBorderError = colorScheme.error.copy(alpha = if (isDark) 0.84f else 0.74f)
    )
}
