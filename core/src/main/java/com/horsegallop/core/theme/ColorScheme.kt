package com.horsegallop.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light Theme Colors
val Primary = Color(0xFF8B4513) // Saddle Brown
val OnPrimary = Color(0xFFFFFFFF) // White
val PrimaryContainer = Color(0xFFF4A460) // Sandy Brown
val OnPrimaryContainer = Color(0xFFFFFFFF) // White

val Secondary = Color(0xFFA0522D) // Sienna - daha uyumlu kahverengi ton
val OnSecondary = Color(0xFFFFFFFF) // White
val SecondaryContainer = Color(0xFFD2B48C) // Tan - daha yumuşak ton
val OnSecondaryContainer = Color(0xFFFFFFFF) // White

val Tertiary = Color(0xFF228B22) // Forest Green
val OnTertiary = Color(0xFFFFFFFF) // White
val TertiaryContainer = Color(0xFF32CD32) // Lime Green
val OnTertiaryContainer = Color(0xFFFFFFFF) // White

val Error = Color(0xFFF44336) // Red
val OnError = Color(0xFFFFFFFF) // White
val ErrorContainer = SecondaryContainer
val OnErrorContainer = Color(0xFFB71C1C) // Dark Red

val Background = Color(0xFFFAFAFA) // Light Gray
val OnBackground = Color(0xFF1A1A1A) // Dark Gray
val Surface = Color(0xFFFFFFFF) // White
val OnSurface = Color(0xFF1A1A1A) // Dark Gray
val SurfaceVariant = Color(0xFFF5F5F5) // Light Gray
val OnSurfaceVariant = Color(0xFF666666) // Medium Gray

val Outline = Color(0xFFCCCCCC) // Medium Gray
val InverseOnSurface = Color(0xFFF5F5F5) // Light Gray
val InverseSurface = Color(0xFF2F2F2F) // Dark Gray
val InversePrimary = Color(0xFFCD853F) // Peru
val Shadow = Color(0xFF000000) // Black
val SurfaceTint = Color(0xFF8B4513) // Primary
val OutlineVariant = Color(0xFFE0E0E0) // Light Gray
val Scrim = Color(0xFF000000) // Black

// Dark Theme Colors
val PrimaryDark = Color(0xFFCD853F) // Peru
val OnPrimaryDark = Color(0xFFFFFFFF) // White
val PrimaryContainerDark = Color(0xFFA0522D) // Sienna
val OnPrimaryContainerDark = Color(0xFFFFFFFF) // White

val SecondaryDark = Color(0xFFD2B48C) // Tan - daha yumuşak ton
val OnSecondaryDark = Color(0xFFFFFFFF) // White
val SecondaryContainerDark = Color(0xFF8B7355) // Daha koyu kahverengi ton
val OnSecondaryContainerDark = Color(0xFFFFFFFF) // White

val TertiaryDark = Color(0xFF32CD32) // Lime Green
val OnTertiaryDark = Color(0xFFFFFFFF) // White
val TertiaryContainerDark = Color(0xFF006400) // Dark Green
val OnTertiaryContainerDark = Color(0xFFFFFFFF) // White

val ErrorDark = Color(0xFFEF5350) // Light Red
val OnErrorDark = Color(0xFFFFFFFF) // White
val ErrorContainerDark = SecondaryContainerDark
val OnErrorContainerDark = Color(0xFFFFFFFF) // White

val BackgroundDark = Color(0xFF1A1A1A) // Dark Gray
val OnBackgroundDark = Color(0xFFF5F5F5) // Light Gray
val SurfaceDark = Color(0xFF2F2F2F) // Dark Gray
val OnSurfaceDark = Color(0xFFF5F5F5) // Light Gray
val SurfaceVariantDark = Color(0xFF404040) // Medium Dark Gray
val OnSurfaceVariantDark = Color(0xFFCCCCCC) // Light Gray

val OutlineDark = Color(0xFF666666) // Medium Gray
val InverseOnSurfaceDark = Color(0xFF1A1A1A) // Dark Gray
val InverseSurfaceDark = Color(0xFFF5F5F5) // Light Gray
val InversePrimaryDark = Color(0xFF8B4513) // Saddle Brown
val ShadowDark = Color(0xFF000000) // Black
val SurfaceTintDark = Color(0xFFCD853F) // Peru
val OutlineVariantDark = Color(0xFF404040) // Medium Dark Gray
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
    inverseOnSurface = InverseOnSurface,
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

// Additional App-Specific Colors
object AppColors {
    // Quick Action Colors
    val ActionLesson = Color(0xFFB46A3A) // WarmClay
    val ActionSchedule = Color(0xFFC89272) // ToastedAlmond
    val ActionRestaurant = Secondary // Chocolate, matches theme
    val ActionReviews = Primary // Saddle Brown, matches theme
    
    // Status Colors
    val Success = Color(0xFF4CAF50) // Green
    val SuccessLight = Color(0xFF81C784) // Light Green
    val SuccessDark = Color(0xFF388E3C) // Dark Green
    
    val Warning = Color(0xFFFF9800) // Orange
    val WarningLight = Color(0xFFFFB74D) // Light Orange
    val WarningDark = Color(0xFFF57C00) // Dark Orange
    
    val Info = Color(0xFF2196F3) // Blue
    val InfoLight = Color(0xFF64B5F6) // Light Blue
    val InfoDark = Color(0xFF1976D2) // Dark Blue
    
    // Special Colors
    val SplashBackground = Color(0xFFF5E6D3) // Cream
    val Divider = Color(0xFFE0E0E0) // Light Gray
    val Border = Color(0xFFCCCCCC) // Medium Gray
    
    // Gradient Colors
    val GradientStart = Color(0xFF8B4513) // Primary
    val GradientEnd = Color(0xFFD2691E) // Secondary
    val GradientLightStart = Color(0xFFF5E6D3) // Cream
    val GradientLightEnd = Color(0xFFFFFFFF) // White
    
    // Overlay Colors
    val OverlayLight = Color(0x80FFFFFF) // Semi-transparent White
    val OverlayDark = Color(0x80000000) // Semi-transparent Black
    val OverlayPrimary = Color(0x808B4513) // Semi-transparent Primary

    // Light Coffee Tone (for light, warm UI overlays/backgrounds)
    val LightCoffee = Color(0xFFEAD9C8)

    // Warm Browns for backgrounds (avoid green/beige feel)
    val WarmClay = Color(0xFFB46A3A)   // rich clay brown
    val SoftSand = Color(0xFFD9B08C)   // soft sand, not pale beige
    val ToastedAlmond = Color(0xFFC89272) // warm almond
}
