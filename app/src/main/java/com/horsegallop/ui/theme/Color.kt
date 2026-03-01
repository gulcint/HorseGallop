package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand palette (single source)
val LightBronze = Color(0xFFCB997E)
val DesertSand = Color(0xFFDDBEA9)
val AlmondCream = Color(0xFFFFE8D6)
val AshGrey = Color(0xFFB7B7A4)
val DrySage = Color(0xFFA5A58D)
val DustyOlive = Color(0xFF6B705C)

// Accessible derived tones from the same palette
private val Olive900 = Color(0xFF2E3229)
private val Olive800 = Color(0xFF3A3F33)
private val Olive700 = Color(0xFF4A4F41)
private val CreamSoft = Color(0xFFFFF3EA)
private val CreamWarm = Color(0xFFF7E1D0)
private val BronzeDeep = Color(0xFF8B6958)
private val BronzeDark = Color(0xFF6E5345)

val LightColorScheme = lightColorScheme(
    primary = LightBronze,
    onPrimary = Olive900,
    primaryContainer = DesertSand,
    onPrimaryContainer = Olive900,
    secondary = DrySage,
    onSecondary = Olive900,
    secondaryContainer = AshGrey,
    onSecondaryContainer = Olive900,
    tertiary = DustyOlive,
    onTertiary = AlmondCream,
    tertiaryContainer = DrySage,
    onTertiaryContainer = Olive900,
    error = BronzeDeep,
    onError = CreamSoft,
    errorContainer = CreamWarm,
    onErrorContainer = Olive900,
    background = AlmondCream,
    onBackground = Olive900,
    surface = CreamSoft,
    onSurface = Olive900,
    surfaceVariant = DesertSand,
    onSurfaceVariant = Olive700,
    outline = DustyOlive,
    inverseOnSurface = AlmondCream,
    inverseSurface = Olive800,
    inversePrimary = DesertSand,
    surfaceDim = DesertSand,
    surfaceBright = CreamSoft,
    surfaceContainerLowest = CreamSoft,
    surfaceContainerLow = AlmondCream,
    surfaceContainer = DesertSand,
    surfaceContainerHigh = AshGrey,
    surfaceContainerHighest = DrySage,
    surfaceTint = LightBronze,
    outlineVariant = AshGrey,
    scrim = Olive900
)

val DarkColorScheme = darkColorScheme(
    primary = DesertSand,
    onPrimary = Olive900,
    primaryContainer = BronzeDark,
    onPrimaryContainer = CreamSoft,
    secondary = DrySage,
    onSecondary = Olive900,
    secondaryContainer = Olive700,
    onSecondaryContainer = AlmondCream,
    tertiary = AshGrey,
    onTertiary = Olive900,
    tertiaryContainer = DustyOlive,
    onTertiaryContainer = CreamSoft,
    error = LightBronze,
    onError = Olive900,
    errorContainer = BronzeDark,
    onErrorContainer = CreamSoft,
    background = Olive900,
    onBackground = AlmondCream,
    surface = Olive800,
    onSurface = AlmondCream,
    surfaceVariant = Olive700,
    onSurfaceVariant = AshGrey,
    outline = DrySage,
    inverseOnSurface = Olive900,
    inverseSurface = AlmondCream,
    inversePrimary = LightBronze,
    surfaceDim = Olive900,
    surfaceBright = Olive700,
    surfaceContainerLowest = Olive900,
    surfaceContainerLow = Olive800,
    surfaceContainer = Olive700,
    surfaceContainerHigh = DustyOlive,
    surfaceContainerHighest = DrySage,
    surfaceTint = DesertSand,
    outlineVariant = DustyOlive,
    scrim = Olive900
)
