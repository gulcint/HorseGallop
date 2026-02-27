package com.horsegallop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ComponentColors(
    val buttonPrimaryContainer: Color,
    val buttonPrimaryContent: Color,
    val buttonSecondaryContainer: Color,
    val buttonSecondaryContent: Color,
    val buttonTonalContainer: Color,
    val buttonTonalContent: Color,
    val buttonDangerContainer: Color,
    val buttonDangerContent: Color,
    val tintStrong: Color,
    val tintMuted: Color,
    val tintInverse: Color,
    val inputContainer: Color,
    val inputContainerSubtle: Color,
    val inputBorderFocused: Color,
    val inputBorderUnfocused: Color,
    val snackbarAction: Color,
    val snackbarDismiss: Color
)

val LocalComponentColors = staticCompositionLocalOf {
    componentColorsFrom(LightColorScheme, semanticColorsFrom(LightColorScheme, isDark = false))
}

fun componentColorsFrom(
    colorScheme: ColorScheme,
    semanticColors: SemanticColors
): ComponentColors {
    return ComponentColors(
        buttonPrimaryContainer = colorScheme.primary,
        buttonPrimaryContent = colorScheme.onPrimary,
        buttonSecondaryContainer = colorScheme.secondaryContainer,
        buttonSecondaryContent = colorScheme.onSecondaryContainer,
        buttonTonalContainer = semanticColors.cardSubtle,
        buttonTonalContent = colorScheme.primary,
        buttonDangerContainer = colorScheme.error,
        buttonDangerContent = colorScheme.onError,
        tintStrong = colorScheme.primary,
        tintMuted = colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
        tintInverse = colorScheme.inverseOnSurface,
        inputContainer = semanticColors.cardElevated,
        inputContainerSubtle = semanticColors.cardSubtle,
        inputBorderFocused = colorScheme.primary,
        inputBorderUnfocused = colorScheme.outline,
        snackbarAction = semanticColors.calloutOnContainer,
        snackbarDismiss = semanticColors.calloutOnContainer.copy(alpha = 0.84f)
    )
}
