package com.horsegallop.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Modern Material 3 Color Palette - HorseGallop Theme
// Warm browns with modern accents for equestrian app

// Light Theme Colors (Modern & Clean)
val Primary = Color(0xFF8B5A2B) // Modern Saddle Brown
val OnPrimary = Color(0xFFFFFFFF) // White
val PrimaryContainer = Color(0xFFFFD7B3) // Warm Light Brown
val OnPrimaryContainer = Color(0xFF2D1B0E) // Dark Brown

val Secondary = Color(0xFF6D5A42) // Warm Gray-Brown
val OnSecondary = Color(0xFFFFFFFF) // White
val SecondaryContainer = Color(0xFFE8D5C2) // Light Warm Beige
val OnSecondaryContainer = Color(0xFF1F160E) // Dark Brown

val Tertiary = Color(0xFF4D6B3A) // Muted Olive Green
val OnTertiary = Color(0xFFFFFFFF) // White
val TertiaryContainer = Color(0xFFC7E5A3) // Light Green
val OnTertiaryContainer = Color(0xFF192D10) // Dark Green

val Error = Color(0xFFBA1A1A) // Modern Red
val OnError = Color(0xFFFFFFFF) // White
val ErrorContainer = Color(0xFFFFDAD6) // Light Red
val OnErrorContainer = Color(0xFF410002) // Dark Red

val Background = Color(0xFFFDF7F0) // Cream Warm
val OnBackground = Color(0xFF1A1A1A) // Dark Gray
val Surface = Color(0xFFFFFFFF) // White
val OnSurface = Color(0xFF1A1A1A) // Dark Gray
val SurfaceVariant = Color(0xFFF5EFE6) // Soft Cream
val OnSurfaceVariant = Color(0xFF4A453D) // Gray Brown
val Outline = Color(0xFF79746B) // Gray Brown
val OnInverseSurface = Color(0xFFF5EFE6) // Soft Cream
val InverseSurface = Color(0xFF2F2A23) // Dark Gray Brown
val InversePrimary = Color(0xFFFFB58C) // Warm Light Orange
val Shadow = Color(0xFF000000) // Black
val SurfaceTint = Color(0xFF8B5A2B) // Modern Saddle Brown
val OutlineVariant = Color(0xFFCDC6BC) // Soft Gray Brown
val Scrim = Color(0xFF000000) // Black

// Dark Theme Colors (Modern)
val PrimaryDark = Color(0xFFFFB58C) // Warm Light Orange
val OnPrimaryDark = Color(0xFF4A2E15) // Dark Brown
val PrimaryContainerDark = Color(0xFF6B421E) // Dark Saddle Brown
val OnPrimaryContainerDark = Color(0xFFFEDDC9) // Light Warm

val SecondaryDark = Color(0xFFCBC3B2) // Soft Gray Beige
val OnSecondaryDark = Color(0xFF34291F) // Dark Gray Brown
val SecondaryContainerDark = Color(0xFF4C4237) // Dark Gray
val OnSecondaryContainerDark = Color(0xFFE8D5C2) // Light Warm Beige

val TertiaryDark = Color(0xFFA9C785) // Light Green
val OnTertiaryDark = Color(0xFF2E471D) // Dark Green
val TertiaryContainerDark = Color(0xFF45632F) // Dark Green
val OnTertiaryContainerDark = Color(0xFFC7E5A3) // Light Green

val ErrorDark = Color(0xFFFFB4AB) // Light Red
val OnErrorDark = Color(0xFF691814) // Dark Red
val ErrorContainerDark = Color(0xFF932018) // Dark Red Container
val OnErrorContainerDark = Color(0xFFFFDAD6) // Light Red

val BackgroundDark = Color(0xFF1A1A1A) // Dark Gray
val OnBackgroundDark = Color(0xFFF5EFE6) // Soft Cream
val SurfaceDark = Color(0xFF1A1A1A) // Dark Gray
val OnSurfaceDark = Color(0xFFF5EFE6) // Soft Cream
val SurfaceVariantDark = Color(0xFF4A453D) // Gray Brown
val OnSurfaceVariantDark = Color(0xFFCDC6BC) // Soft Gray Brown
val OutlineDark = Color(0xFF979186) // Light Gray Brown
val InverseOnSurfaceDark = Color(0xFF1A1A1A) // Dark Gray
val InverseSurfaceDark = Color(0xFFF5EFE6) // Soft Cream
val InversePrimaryDark = Color(0xFF8B5A2B) // Mirror of light primary for dark inverse surfaces
val ShadowDark = Color(0xFF000000) // Black
val SurfaceTintDark = Color(0xFFFFB58C) // Warm Light Orange
val OutlineVariantDark = Color(0xFF4A453D) // Gray Brown
val ScrimDark = Color(0xFF000000) // Black

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

// App-Specific Color Object - Enhanced for Modern UX
object AppColors {
    // Quick Action Colors - Warm and inviting
    val ActionLesson = Color(0xFFE6A45C) // Warm Orange for lessons
    val ActionSchedule = Color(0xFF8B5A2B) // Primary for schedule
    val ActionRestaurant = Color(0xFF4D6B3A) // Tertiary for dining
    val ActionReviews = Color(0xFFFFD7B3) // PrimaryContainer for reviews
    
    // Status Colors - Clear and accessible
    val StatusOnline = Color(0xFF4CAF50) // Green
    val StatusOffline = Color(0xFF9E9E9E) // Gray
    val StatusBusy = Color(0xFFF44336) // Red
    
    // Brand Colors
    val Destructive = Color(0xFFBA1A1A) // Modern Red
    val Success = Color(0xFF4CAF50) // Green
    val SuccessLight = Color(0xFF81C784) // Light Green
    val SuccessDark = Color(0xFF388E3C) // Dark Green
    
    // Warning Colors
    val Warning = Color(0xFFFF9800) // Orange
    val WarningLight = Color(0xFFFFB74D) // Light Orange
    val WarningDark = Color(0xFFF57C00) // Dark Orange
    
    // Info Colors
    val Info = Color(0xFF2196F3) // Blue
    val InfoLight = Color(0xFF64B5F6) // Light Blue
    val InfoDark = Color(0xFF1976D2) // Dark Blue
    
    // Special UI Colors
    val SplashBackground = Color(0xFFF5E6D3) // Cream
    val Divider = Color(0xFFE0E0E0) // Light Gray
    val Border = Color(0xFFCCCCCC) // Medium Gray
    
    // Gradient Colors - Warm and natural
    val GradientStart = Color(0xFF8B5A2B) // Modern Primary
    val GradientEnd = Color(0xFFFFD7B3) // Warm Light Brown
    val GradientLightStart = Color(0xFFF5E6D3) // Cream
    val GradientLightEnd = Color(0xFFFFFFFF) // White
    
    // Overlay Colors - Transparent overlays
    val OverlayLight = Color(0x80FFFFFF) // Semi-transparent White
    val OverlayDark = Color(0x80000000) // Semi-transparent Black
    val OverlayPrimary = Color(0x808B5A2B) // Semi-transparent Primary

    // Warm Coffee & Beige Tones
    val LightCoffee = Color(0xFFEAD9C8)
    val WarmClay = Color(0xFFB46A3A)   // rich clay brown
    val SoftSand = Color(0xFFD9B08C)   // soft sand, not pale beige
    val ToastedAlmond = Color(0xFFC89272) // warm almond
    
    // Additional Warm Tones
    val Cream = Color(0xFFF5E6D3)
    val Tan = Color(0xFFD2B48C)
    val Buff = Color(0xFFF0DC82)
    val Camel = Color(0xFFC19A6B)
    
    // Horse & Riding Theme Colors
    val SaddleBrown = Color(0xFF8B4513)
    val Leather = Color(0xFF6F4E37)
    val Hay = Color(0xFFF0DC82)
    val Stable = Color(0xFFD7CCC8)
    
    // Special Status Colors
    val ActiveRide = Color(0xFF4CAF50) // Green for active rides
    val RidePaused = Color(0xFFFFC107) // Amber for paused
    val RideCompleted = Color(0xFF2196F3) // Blue for completed
}
