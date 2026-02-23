package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Saddle-brown-first Material 3 palette for HorseGallop.
// The goal is warmer contrast and less washed-out surfaces.

// Light theme
val Primary = Color(0xFF7B3F20)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEBC8AA)
val OnPrimaryContainer = Color(0xFF2D160B)

val Secondary = Color(0xFF8A5B3C)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF1D8C4)
val OnSecondaryContainer = Color(0xFF2B1A10)

val Tertiary = Color(0xFF566B3A)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFD9E3C5)
val OnTertiaryContainer = Color(0xFF1B2710)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val Background = Color(0xFFF8F1E8)
val OnBackground = Color(0xFF20150E)
val Surface = Color(0xFFFFF8F2)
val OnSurface = Color(0xFF24180F)
val SurfaceVariant = Color(0xFFE7D8CA)
val OnSurfaceVariant = Color(0xFF554538)
val Outline = Color(0xFF8C7766)
val OnInverseSurface = Color(0xFFFDEEE1)
val InverseSurface = Color(0xFF3A2B21)
val InversePrimary = Color(0xFFFFB88D)
val Shadow = Color(0xFF000000)
val SurfaceTint = Primary
val OutlineVariant = Color(0xFFCDB9A8)
val Scrim = Color(0xFF000000)

// Dark theme
val PrimaryDark = Color(0xFFE2B28B)
val OnPrimaryDark = Color(0xFF45210E)
val PrimaryContainerDark = Color(0xFF6B3419)
val OnPrimaryContainerDark = Color(0xFFF8DDC8)

val SecondaryDark = Color(0xFFD6B59A)
val OnSecondaryDark = Color(0xFF3F281B)
val SecondaryContainerDark = Color(0xFF5D4334)
val OnSecondaryContainerDark = Color(0xFFF3DDCB)

val TertiaryDark = Color(0xFFB9CAA0)
val OnTertiaryDark = Color(0xFF273517)
val TertiaryContainerDark = Color(0xFF3B4E2A)
val OnTertiaryContainerDark = Color(0xFFDCE8C8)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF18100A)
val OnBackgroundDark = Color(0xFFF2E2D4)
val SurfaceDark = Color(0xFF21170F)
val OnSurfaceDark = Color(0xFFF6E7D9)
val SurfaceVariantDark = Color(0xFF534337)
val OnSurfaceVariantDark = Color(0xFFD4C0AD)
val OutlineDark = Color(0xFFA58D79)
val InverseOnSurfaceDark = Color(0xFF2A1D13)
val InverseSurfaceDark = Color(0xFFF2E2D4)
val InversePrimaryDark = Primary
val ShadowDark = Color(0xFF000000)
val SurfaceTintDark = PrimaryDark
val OutlineVariantDark = Color(0xFF6E5A4A)
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

object AppColors {
    val ActionLesson = Color(0xFFC97B3A)
    val ActionSchedule = Color(0xFF7B3F20)
    val ActionRestaurant = Color(0xFF566B3A)
    val ActionReviews = Color(0xFFE5B789)
    
    val StatusOnline = Color(0xFF3F8F52)
    val StatusOffline = Color(0xFF85776D)
    val StatusBusy = Color(0xFFC53929)
    
    val Destructive = Color(0xFFB3261E)
    val Success = Color(0xFF3F8F52)
    val SuccessLight = Color(0xFF67B27A)
    val SuccessDark = Color(0xFF2B6E3B)
    
    val Warning = Color(0xFFE38B2E)
    val WarningLight = Color(0xFFF2AE64)
    val WarningDark = Color(0xFFB5671D)
    
    val Info = Color(0xFF4F7EA6)
    val InfoLight = Color(0xFF7CA5C7)
    val InfoDark = Color(0xFF325A7E)
    
    val SplashBackground = Color(0xFFF7E4D0)
    val Divider = Color(0xFFD8C6B6)
    val Border = Color(0xFFBFA892)
    
    val GradientStart = Color(0xFF7B3F20)
    val GradientEnd = Color(0xFFE7B78C)
    val GradientLightStart = Color(0xFFF3E2D3)
    val GradientLightEnd = Color(0xFFFFF7F0)
    
    val OverlayLight = Color(0x80FFFFFF)
    val OverlayDark = Color(0x80000000)
    val OverlayPrimary = Color(0x807B3F20)

    val LightCoffee = Color(0xFFEAD9C8)
    val WarmClay = Color(0xFF9C5A31)
    val SoftSand = Color(0xFFD3A67D)
    val ToastedAlmond = Color(0xFFBE8663)
    
    val Cream = Color(0xFFF5E6D3)
    val Tan = Color(0xFFD2B48C)
    val Buff = Color(0xFFF0DC82)
    val Camel = Color(0xFFC19A6B)
    
    val SaddleBrown = Color(0xFF8B4513)
    val Leather = Color(0xFF6F4E37)
    val Hay = Color(0xFFF0DC82)
    val Stable = Color(0xFFCBB5A3)
    
    val ActiveRide = Color(0xFF3F8F52)
    val RidePaused = Color(0xFFE7A53F)
    val RideCompleted = Color(0xFF4F7EA6)
}
