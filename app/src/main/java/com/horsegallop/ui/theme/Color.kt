package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Saddle-brown-first Material 3 palette for HorseGallop.
// The goal is warmer contrast and less washed-out surfaces.

// Light theme
val Primary = Color(0xFF8B4513)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFF0C49C)
val OnPrimaryContainer = Color(0xFF2F1305)

val Secondary = Color(0xFFC86E2A)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFF7D6B7)
val OnSecondaryContainer = Color(0xFF3B1D09)

val Tertiary = Color(0xFF6E5A2B)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFEADFB2)
val OnTertiaryContainer = Color(0xFF2A2108)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val Background = Color(0xFFF7E5D3)
val OnBackground = Color(0xFF23160C)
val Surface = Color(0xFFFFF7EE)
val OnSurface = Color(0xFF27180E)
val SurfaceVariant = Color(0xFFECD9C6)
val OnSurfaceVariant = Color(0xFF5C4735)
val Outline = Color(0xFF9D7D63)
val OnInverseSurface = Color(0xFFFDEFE2)
val InverseSurface = Color(0xFF3A2719)
val InversePrimary = Color(0xFFF3B287)
val Shadow = Color(0xFF000000)
val SurfaceTint = Primary
val OutlineVariant = Color(0xFFD2BDA8)
val Scrim = Color(0xFF000000)

// Dark theme
val PrimaryDark = Color(0xFFF2B37E)
val OnPrimaryDark = Color(0xFF4A2309)
val PrimaryContainerDark = Color(0xFF6F3613)
val OnPrimaryContainerDark = Color(0xFFFFE1C5)

val SecondaryDark = Color(0xFFFFC08C)
val OnSecondaryDark = Color(0xFF4D260B)
val SecondaryContainerDark = Color(0xFF7A4420)
val OnSecondaryContainerDark = Color(0xFFFFE2C8)

val TertiaryDark = Color(0xFFD8C98C)
val OnTertiaryDark = Color(0xFF3A3012)
val TertiaryContainerDark = Color(0xFF54451E)
val OnTertiaryContainerDark = Color(0xFFF3E8B8)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF1E1209)
val OnBackgroundDark = Color(0xFFF3DFCC)
val SurfaceDark = Color(0xFF28180E)
val OnSurfaceDark = Color(0xFFF8E9DA)
val SurfaceVariantDark = Color(0xFF5A4535)
val OnSurfaceVariantDark = Color(0xFFDBC0A7)
val OutlineDark = Color(0xFFB7987B)
val InverseOnSurfaceDark = Color(0xFF2D1D12)
val InverseSurfaceDark = Color(0xFFF3DFCC)
val InversePrimaryDark = Primary
val ShadowDark = Color(0xFF000000)
val SurfaceTintDark = PrimaryDark
val OutlineVariantDark = Color(0xFF745A45)
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
    val ActionLesson = Color(0xFFD78434)
    val ActionSchedule = Color(0xFF8B4513)
    val ActionRestaurant = Color(0xFF6E5A2B)
    val ActionReviews = Color(0xFFF4BB7B)
    
    val StatusOnline = Color(0xFF3F8F52)
    val StatusOffline = Color(0xFF85776D)
    val StatusBusy = Color(0xFFC53929)
    
    val Destructive = Color(0xFFB3261E)
    val Success = Color(0xFF3F8F52)
    val SuccessLight = Color(0xFF67B27A)
    val SuccessDark = Color(0xFF2B6E3B)
    
    val Warning = Color(0xFFE58A34)
    val WarningLight = Color(0xFFF4B36F)
    val WarningDark = Color(0xFFB9681D)
    
    val Info = Color(0xFF4F7EA6)
    val InfoLight = Color(0xFF7CA5C7)
    val InfoDark = Color(0xFF325A7E)
    
    val SplashBackground = Color(0xFFF7E1CC)
    val Divider = Color(0xFFDFCAB3)
    val Border = Color(0xFFC8AA8D)
    
    val GradientStart = Color(0xFF8B4513)
    val GradientEnd = Color(0xFFE99A5A)
    val GradientLightStart = Color(0xFFF3DEC9)
    val GradientLightEnd = Color(0xFFFFF5EA)
    
    val OverlayLight = Color(0x80FFFFFF)
    val OverlayDark = Color(0x80000000)
    val OverlayPrimary = Color(0x808B4513)

    val LightCoffee = Color(0xFFE8D4BF)
    val WarmClay = Color(0xFFA45B2D)
    val SoftSand = Color(0xFFD9A77A)
    val ToastedAlmond = Color(0xFFC98756)
    
    val Cream = Color(0xFFF6E4CF)
    val Tan = Color(0xFFD2B48C)
    val Buff = Color(0xFFF0DC82)
    val Camel = Color(0xFFC19A6B)
    
    val SaddleBrown = Color(0xFF8B4513)
    val Leather = Color(0xFF6F4E37)
    val Hay = Color(0xFFF0DC82)
    val Stable = Color(0xFFD0B7A0)
    
    val ActiveRide = Color(0xFF3F8F52)
    val RidePaused = Color(0xFFE7A53F)
    val RideCompleted = Color(0xFF4F7EA6)
}
