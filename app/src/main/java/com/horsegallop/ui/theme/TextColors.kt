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
    titlePrimary = Color.Unspecified,
    bodyPrimary = Color.Unspecified,
    bodySecondary = Color.Unspecified,
    bodyTertiary = Color.Unspecified,
    hint = Color.Unspecified,
    highlight = Color.Unspecified,
    inverse = Color.Unspecified
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

