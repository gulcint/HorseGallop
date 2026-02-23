package com.horsegallop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme
import com.horsegallop.settings.ThemeMode

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalTextColors provides textColorsFrom(scheme)) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            content = content
        )
    }
}
