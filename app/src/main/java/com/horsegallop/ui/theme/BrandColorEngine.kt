package com.horsegallop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun buildLightBrandColorScheme(spec: BrandThemeSpec): ColorScheme {
    val primary = tone(spec.primarySeed, lightness = 0.34f, saturationScale = 1.05f)
    val secondary = tone(spec.secondarySeed, lightness = 0.42f, saturationScale = 1.0f)
    val tertiary = tone(spec.tertiarySeed, lightness = 0.38f, saturationScale = 0.95f)

    val background = tone(spec.neutralSeed, lightness = 0.95f, saturationScale = 0.42f)
    val surface = tone(spec.neutralSeed, lightness = 0.98f, saturationScale = 0.36f)
    val surfaceVariant = tone(spec.neutralSeed, lightness = 0.84f, saturationScale = 0.52f)

    return lightColorScheme(
        primary = primary,
        onPrimary = tone(spec.primarySeed, lightness = 0.99f, saturationScale = 0.10f),
        primaryContainer = tone(spec.primarySeed, lightness = 0.86f, saturationScale = 0.72f),
        onPrimaryContainer = tone(spec.primarySeed, lightness = 0.17f, saturationScale = 1.0f),
        secondary = secondary,
        onSecondary = tone(spec.secondarySeed, lightness = 0.99f, saturationScale = 0.10f),
        secondaryContainer = tone(spec.secondarySeed, lightness = 0.86f, saturationScale = 0.68f),
        onSecondaryContainer = tone(spec.secondarySeed, lightness = 0.20f, saturationScale = 1.0f),
        tertiary = tertiary,
        onTertiary = tone(spec.tertiarySeed, lightness = 0.99f, saturationScale = 0.08f),
        tertiaryContainer = tone(spec.tertiarySeed, lightness = 0.87f, saturationScale = 0.62f),
        onTertiaryContainer = tone(spec.tertiarySeed, lightness = 0.19f, saturationScale = 1.0f),
        error = tone(spec.errorSeed, lightness = 0.44f, saturationScale = 1.05f),
        onError = tone(spec.errorSeed, lightness = 0.99f, saturationScale = 0.1f),
        errorContainer = tone(spec.errorSeed, lightness = 0.88f, saturationScale = 0.6f),
        onErrorContainer = tone(spec.errorSeed, lightness = 0.21f, saturationScale = 1.0f),
        background = background,
        onBackground = tone(spec.neutralSeed, lightness = 0.12f, saturationScale = 0.38f),
        surface = surface,
        onSurface = tone(spec.neutralSeed, lightness = 0.12f, saturationScale = 0.30f),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = tone(spec.neutralSeed, lightness = 0.30f, saturationScale = 0.42f),
        outline = tone(spec.neutralSeed, lightness = 0.48f, saturationScale = 0.34f),
        outlineVariant = tone(spec.neutralSeed, lightness = 0.70f, saturationScale = 0.34f),
        inverseSurface = tone(spec.neutralSeed, lightness = 0.20f, saturationScale = 0.34f),
        inverseOnSurface = tone(spec.neutralSeed, lightness = 0.95f, saturationScale = 0.22f),
        inversePrimary = tone(spec.primarySeed, lightness = 0.78f, saturationScale = 0.84f),
        surfaceDim = tone(spec.neutralSeed, lightness = 0.84f, saturationScale = 0.35f),
        surfaceBright = tone(spec.neutralSeed, lightness = 1.0f, saturationScale = 0.1f),
        surfaceContainerLowest = tone(spec.neutralSeed, lightness = 1.0f, saturationScale = 0.08f),
        surfaceContainerLow = tone(spec.neutralSeed, lightness = 0.97f, saturationScale = 0.16f),
        surfaceContainer = tone(spec.neutralSeed, lightness = 0.94f, saturationScale = 0.24f),
        surfaceContainerHigh = tone(spec.neutralSeed, lightness = 0.91f, saturationScale = 0.3f),
        surfaceContainerHighest = tone(spec.neutralSeed, lightness = 0.88f, saturationScale = 0.34f),
        surfaceTint = primary,
        scrim = tone(spec.neutralSeed, lightness = 0.0f, saturationScale = 0f)
    )
}

private fun tone(seed: Color, lightness: Float, saturationScale: Float): Color {
    val clampedLightness = lightness.coerceIn(0f, 1f)
    val toned = when {
        clampedLightness >= 0.5f -> {
            val progress = ((clampedLightness - 0.5f) / 0.5f).coerceIn(0f, 1f)
            lerpColor(seed, Color.White, progress)
        }

        else -> {
            val progress = ((0.5f - clampedLightness) / 0.5f).coerceIn(0f, 1f)
            lerpColor(seed, Color.Black, progress)
        }
    }

    return adjustSaturation(toned, saturationScale)
}

private fun lerpColor(start: Color, end: Color, progress: Float): Color {
    val p = progress.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * p,
        green = start.green + (end.green - start.green) * p,
        blue = start.blue + (end.blue - start.blue) * p,
        alpha = start.alpha + (end.alpha - start.alpha) * p
    )
}

private fun adjustSaturation(color: Color, saturationScale: Float): Color {
    val scale = saturationScale.coerceIn(0f, 1.8f)
    val gray = color.luminance()
    return Color(
        red = (gray + (color.red - gray) * scale).coerceIn(0f, 1f),
        green = (gray + (color.green - gray) * scale).coerceIn(0f, 1f),
        blue = (gray + (color.blue - gray) * scale).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}
