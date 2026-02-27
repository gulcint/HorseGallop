package com.horsegallop.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandColorEngineTest {

    @Test
    fun buildLightBrandColorScheme_primaryAndSecondaryAreDistinct() {
        val scheme = buildLightBrandColorScheme(defaultSaddleBrown())

        assertNotEquals("primary and secondary must not collapse to same color", scheme.primary, scheme.secondary)
    }

    @Test
    fun buildLightBrandColorScheme_surfaceLadderHasOrderedLuminance() {
        val scheme = buildLightBrandColorScheme(defaultSaddleBrown())

        val ladder = listOf(
            scheme.surfaceContainerLowest,
            scheme.surfaceContainerLow,
            scheme.surfaceContainer,
            scheme.surfaceContainerHigh,
            scheme.surfaceContainerHighest,
            scheme.surfaceDim
        )

        ladder.zipWithNext().forEachIndexed { index, (first, second) ->
            assertTrue(
                "surface ladder broken at step $index: ${first.luminance()} < ${second.luminance()}",
                first.luminance() >= second.luminance()
            )
        }
    }

    @Test
    fun buildLightBrandColorScheme_onColorsMeetContrastThreshold() {
        val scheme = buildLightBrandColorScheme(defaultSaddleBrown())

        assertTrue(
            "onPrimary contrast too low",
            contrastRatio(scheme.primary, scheme.onPrimary) >= 4.5
        )
        assertTrue(
            "onSurface contrast too low",
            contrastRatio(scheme.surface, scheme.onSurface) >= 4.5
        )
    }

    private fun contrastRatio(a: Color, b: Color): Double {
        val l1 = a.luminance().toDouble()
        val l2 = b.luminance().toDouble()
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }
}
