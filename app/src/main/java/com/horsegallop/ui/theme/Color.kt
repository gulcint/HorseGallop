package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Saddle-brown Material 3 palette tuned for stronger contrast and clearer role separation.

// Light theme
val Primary = Color(0xFF7A3F14)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFF1E1D2)
val OnPrimaryContainer = Color(0xFF2E1405)

val Secondary = Color(0xFFB7601D)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF6E4D3)
val OnSecondaryContainer = Color(0xFF3C1E08)

val Tertiary = Color(0xFF6C5B2D)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFEDE2C4)
val OnTertiaryContainer = Color(0xFF2D240A)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val Background = Color(0xFFEEE1D1)
val OnBackground = Color(0xFF2A1D12)
val Surface = Color(0xFFFFFCFA)
val OnSurface = Color(0xFF2A1D12)
val SurfaceVariant = Color(0xFFE8D9C8)
val OnSurfaceVariant = Color(0xFF5F5245)
val Outline = Color(0xFFAF9A87)
val OnInverseSurface = Color(0xFFF9EADB)
val InverseSurface = Color(0xFF382519)
val InversePrimary = Color(0xFFE7B184)
val Shadow = Color(0xFF000000)
val SurfaceTint = Primary
val OutlineVariant = Color(0xFFD8CABA)
val Scrim = Color(0xFF000000)
val SurfaceDim = Color(0xFFE4D3C0)
val SurfaceBright = Color(0xFFFFFCFA)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFFDF4EA)
val SurfaceContainer = Color(0xFFF7EADA)
val SurfaceContainerHigh = Color(0xFFF1E2D0)
val SurfaceContainerHighest = Color(0xFFE8D7C4)

// Dark theme
val PrimaryDark = Color(0xFFE7B486)
val OnPrimaryDark = Color(0xFF452006)
val PrimaryContainerDark = Color(0xFF6A3311)
val OnPrimaryContainerDark = Color(0xFFFFDDBF)

val SecondaryDark = Color(0xFFF3B37C)
val OnSecondaryDark = Color(0xFF4A260C)
val SecondaryContainerDark = Color(0xFF7A431D)
val OnSecondaryContainerDark = Color(0xFFFFDDBF)

val TertiaryDark = Color(0xFFD9C78F)
val OnTertiaryDark = Color(0xFF392F12)
val TertiaryContainerDark = Color(0xFF54461F)
val OnTertiaryContainerDark = Color(0xFFF2E8BD)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF1A110B)
val OnBackgroundDark = Color(0xFFF3DFCB)
val SurfaceDark = Color(0xFF2A1B12)
val OnSurfaceDark = Color(0xFFF8E8D8)
val SurfaceVariantDark = Color(0xFF503D2F)
val OnSurfaceVariantDark = Color(0xFFD4BDA8)
val OutlineDark = Color(0xFFA88C76)
val InverseOnSurfaceDark = Color(0xFF2A1C12)
val InverseSurfaceDark = Color(0xFFF2DDC9)
val InversePrimaryDark = Primary
val ShadowDark = Color(0xFF000000)
val SurfaceTintDark = PrimaryDark
val OutlineVariantDark = Color(0xFF6D5546)
val ScrimDark = Color(0xFF000000)
val SurfaceDimDark = Color(0xFF23170F)
val SurfaceBrightDark = Color(0xFF3A281C)
val SurfaceContainerLowestDark = Color(0xFF150D08)
val SurfaceContainerLowDark = Color(0xFF21160F)
val SurfaceContainerDark = Color(0xFF2B1D14)
val SurfaceContainerHighDark = Color(0xFF342419)
val SurfaceContainerHighestDark = Color(0xFF3D2B1F)

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
