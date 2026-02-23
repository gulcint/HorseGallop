package com.horsegallop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TextColors(
  val titlePrimary: Color,
  val bodyPrimary: Color,
  val bodySecondary: Color,
  val bodyTertiary: Color,
  val hint: Color,
  val highlight: Color,
  val inverse: Color
)

val LocalTextColors = staticCompositionLocalOf {
  TextColors(
    titlePrimary = Color(0xFF1A1A1A),
    bodyPrimary = Color(0xFF1A1A1A),
    bodySecondary = Color(0xFF666666),
    bodyTertiary = Color(0xFF7A7A7A),
    hint = Color(0xFF9E9E9E),
    highlight = Color(0xFF8B4513),
    inverse = Color(0xFFFFFFFF)
  )
}

fun textColorsFrom(colorScheme: ColorScheme): TextColors {
  return TextColors(
    titlePrimary = colorScheme.onBackground,
    bodyPrimary = colorScheme.onSurface,
    bodySecondary = colorScheme.onSurfaceVariant,
    bodyTertiary = colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
    hint = colorScheme.outline,
    highlight = colorScheme.primary,
    inverse = colorScheme.inverseOnSurface
  )
}


