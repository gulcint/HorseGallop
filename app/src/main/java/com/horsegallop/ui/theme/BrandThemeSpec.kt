package com.horsegallop.ui.theme

import androidx.compose.ui.graphics.Color

data class BrandThemeSpec(
    val primarySeed: Color,
    val secondarySeed: Color,
    val tertiarySeed: Color,
    val neutralSeed: Color,
    val errorSeed: Color
)

fun defaultSaddleBrown(): BrandThemeSpec {
    return BrandThemeSpec(
        primarySeed = Color(0xFF8A4518),
        secondarySeed = Color(0xFFB46F3D),
        tertiarySeed = Color(0xFF4F6757),
        neutralSeed = Color(0xFFD7C2AC),
        errorSeed = Color(0xFFB3261E)
    )
}
