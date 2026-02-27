package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Saddle-brown + nature-sport palette for HorseGallop.

// Light theme
val Primary = Color(0xFF8B4513)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFF2E4D7)
val OnPrimaryContainer = Color(0xFF2E1406)

val Secondary = Color(0xFFA97142)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF6E9DD)
val OnSecondaryContainer = Color(0xFF3A2412)

val Tertiary = Color(0xFF3E5C43)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFDCEBDE)
val OnTertiaryContainer = Color(0xFF142319)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val Background = Color(0xFFF7F1E8)
val OnBackground = Color(0xFF241912)
val Surface = Color(0xFFFFFBF7)
val OnSurface = Color(0xFF241912)
val SurfaceVariant = Color(0xFFEADFD3)
val OnSurfaceVariant = Color(0xFF5C4C3F)
val Outline = Color(0xFF907E71)
val OnInverseSurface = Color(0xFFF9EBDD)
val InverseSurface = Color(0xFF3A2416)
val InversePrimary = Color(0xFFF0BC97)
val Shadow = Color(0xFF000000)
val SurfaceTint = Primary
val OutlineVariant = Color(0xFFD7C8BA)
val Scrim = Color(0xFF000000)
val SurfaceDim = Color(0xFFE4D8CB)
val SurfaceBright = Color(0xFFFFFBF7)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFFBF1E7)
val SurfaceContainer = Color(0xFFF5E9DE)
val SurfaceContainerHigh = Color(0xFFEEDFD1)
val SurfaceContainerHighest = Color(0xFFE7D6C7)

// Dark theme
val PrimaryDark = Color(0xFFE4B08B)
val OnPrimaryDark = Color(0xFF4F2508)
val PrimaryContainerDark = Color(0xFF6E3410)
val OnPrimaryContainerDark = Color(0xFFFFDCC2)

val SecondaryDark = Color(0xFFE2B68F)
val OnSecondaryDark = Color(0xFF4A2E1A)
val SecondaryContainerDark = Color(0xFF6A4630)
val OnSecondaryContainerDark = Color(0xFFFFDCC2)

val TertiaryDark = Color(0xFFA9C8AE)
val OnTertiaryDark = Color(0xFF1F3624)
val TertiaryContainerDark = Color(0xFF25452E)
val OnTertiaryContainerDark = Color(0xFFC8E6CD)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF1E1712)
val OnBackgroundDark = Color(0xFFF2DFD0)
val SurfaceDark = Color(0xFF251B15)
val OnSurfaceDark = Color(0xFFF6E7DB)
val SurfaceVariantDark = Color(0xFF54463B)
val OnSurfaceVariantDark = Color(0xFFD7C3B3)
val OutlineDark = Color(0xFF9F8A7A)
val InverseOnSurfaceDark = Color(0xFF302017)
val InverseSurfaceDark = Color(0xFFF2DFD0)
val InversePrimaryDark = Primary
val ShadowDark = Color(0xFF000000)
val SurfaceTintDark = PrimaryDark
val OutlineVariantDark = Color(0xFF6A584B)
val ScrimDark = Color(0xFF000000)
val SurfaceDimDark = Color(0xFF1B1410)
val SurfaceBrightDark = Color(0xFF3A2A21)
val SurfaceContainerLowestDark = Color(0xFF140E0A)
val SurfaceContainerLowDark = Color(0xFF201611)
val SurfaceContainerDark = Color(0xFF2A1F19)
val SurfaceContainerHighDark = Color(0xFF342820)
val SurfaceContainerHighestDark = Color(0xFF3E3128)

// Light Color Scheme
val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    inverseOnSurface = OnInverseSurface,
    inverseSurface = InverseSurface,
    inversePrimary = InversePrimary,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceTint = SurfaceTint,
    outlineVariant = OutlineVariant,
    scrim = Scrim
)

// Dark Color Scheme
val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inverseSurface = InverseSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceTint = SurfaceTintDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark
)
