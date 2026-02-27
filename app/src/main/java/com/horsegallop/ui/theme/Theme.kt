package com.horsegallop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val scheme = LightColorScheme
    val semanticColors = semanticColorsFrom(scheme, isDark = false)
    val componentColors = componentColorsFrom(scheme, semanticColors)

    CompositionLocalProvider(
        LocalTextColors provides textColorsFrom(scheme),
        LocalSemanticColors provides semanticColors,
        LocalComponentColors provides componentColors
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content
        )
    }
}
