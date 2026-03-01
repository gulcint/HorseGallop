package com.horsegallop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

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
        tintStrong = lerp(colorScheme.primary, colorScheme.secondary, 0.18f),
        tintMuted = lerp(colorScheme.onSurfaceVariant, colorScheme.outline, 0.42f),
        tintInverse = colorScheme.inverseOnSurface,
        inputContainer = semanticColors.cardElevated,
        inputContainerSubtle = semanticColors.cardSubtle,
        inputBorderFocused = colorScheme.primary,
        inputBorderUnfocused = colorScheme.outline,
        snackbarAction = semanticColors.calloutOnContainer,
        snackbarDismiss = semanticColors.calloutOnContainer.copy(alpha = 0.72f)
    )
}
