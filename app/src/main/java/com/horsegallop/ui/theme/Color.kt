package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Saddle-brown Material 3 palette tuned for stronger contrast and clearer role separation.

// Light theme
val Primary = Color(0xFF7A3F14)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFFCF1E7)
val OnPrimaryContainer = Color(0xFF2E1405)

val Secondary = Color(0xFFC56D23)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFFF5EC)
val OnSecondaryContainer = Color(0xFF3C1E08)

val Tertiary = Color(0xFF6C5B2D)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFAF4E2)
val OnTertiaryContainer = Color(0xFF2D240A)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val Background = Color(0xFFFFFEFC)
val OnBackground = Color(0xFF2A1D12)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF2A1D12)
val SurfaceVariant = Color(0xFFF9F4EE)
val OnSurfaceVariant = Color(0xFF625547)
val Outline = Color(0xFFB9A797)
val OnInverseSurface = Color(0xFFF9EADB)
val InverseSurface = Color(0xFF382519)
val InversePrimary = Color(0xFFE8B387)
val Shadow = Color(0xFF000000)
val SurfaceTint = Primary
val OutlineVariant = Color(0xFFECE1D7)
val Scrim = Color(0xFF000000)

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

val BackgroundDark = Color(0xFF1F140D)
val OnBackgroundDark = Color(0xFFF3DFCB)
val SurfaceDark = Color(0xFF2A1C13)
val OnSurfaceDark = Color(0xFFF8E8D8)
val SurfaceVariantDark = Color(0xFF4F3E32)
val OnSurfaceVariantDark = Color(0xFFD4BDA8)
val OutlineDark = Color(0xFFA88C76)
val InverseOnSurfaceDark = Color(0xFF2A1C12)
val InverseSurfaceDark = Color(0xFFF2DDC9)
val InversePrimaryDark = Primary
val ShadowDark = Color(0xFF000000)
val SurfaceTintDark = PrimaryDark
val OutlineVariantDark = Color(0xFF6D5546)
val ScrimDark = Color(0xFF000000)

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
    surfaceTint = SurfaceTintDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark
)
