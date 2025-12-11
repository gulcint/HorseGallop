package com.horsegallop.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Light Theme Colors
val Primary = Color(0xFF8B4513) // Saddle Brown
val OnPrimary = Color(0xFFFFFFFF) // White
val PrimaryContainer = Color(0xFFF4A460) // Sandy Brown
val OnPrimaryContainer = Color(0xFFFFFFFF) // White

val Secondary = Color(0xFFD2691E) // Chocolate
val OnSecondary = Color(0xFFFFFFFF) // White
val SecondaryContainer = Color(0xFFFFB366) // Light Orange
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

val SecondaryDark = Color(0xFFFFB366) // Light Orange
val OnSecondaryDark = Color(0xFFFFFFFF) // White
val SecondaryContainerDark = Color(0xFFB8860B) // Dark Goldenrod
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
    onSecondaryContainer = OnSecondaryDark,
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
    val ActionLesson: Color
        @Composable get() = if (isDark()) Color(0xFF8B5A2B) else Color(0xFFB46A3A)
    val ActionSchedule: Color
        @Composable get() = if (isDark()) Color(0xFF9E6A4F) else Color(0xFFC89272)
    val ActionRestaurant: Color
        @Composable get() = if (isDark()) SecondaryContainerDark else Secondary
    val ActionReviews: Color
        @Composable get() = if (isDark()) PrimaryDark else Primary
    
    // Status Colors
    val Success: Color
        @Composable get() = if (isDark()) Color(0xFF66BB6A) else Color(0xFF4CAF50)
    val SuccessLight: Color
        @Composable get() = if (isDark()) Color(0xFF43A047) else Color(0xFF81C784)
    val SuccessDark: Color
        @Composable get() = Color(0xFF388E3C)
    
    val Warning: Color
        @Composable get() = if (isDark()) Color(0xFFFFA726) else Color(0xFFFF9800)
    val WarningLight: Color
        @Composable get() = if (isDark()) Color(0xFFFF8F00) else Color(0xFFFFB74D)
    val WarningDark: Color
        @Composable get() = Color(0xFFF57C00)
    
    val Info: Color
        @Composable get() = if (isDark()) Color(0xFF64B5F6) else Color(0xFF2196F3)
    val InfoLight: Color
        @Composable get() = if (isDark()) Color(0xFF42A5F5) else Color(0xFF64B5F6)
    val InfoDark: Color
        @Composable get() = Color(0xFF1976D2)
    
    // Special Colors
    val SplashBackground: Color
        @Composable get() = if (isDark()) SurfaceDark else Color(0xFFF5E6D3)
    val Divider: Color
        @Composable get() = if (isDark()) Color(0xFF404040) else Color(0xFFE0E0E0)
    val Border: Color
        @Composable get() = if (isDark()) Color(0xFF666666) else Color(0xFFCCCCCC)
    
    // Gradient Colors
    val GradientStart: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val GradientEnd: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
    val GradientLightStart: Color
        @Composable get() = if (isDark()) SurfaceDark else Color(0xFFF5E6D3)
    val GradientLightEnd: Color
        @Composable get() = if (isDark()) SurfaceDark else Color(0xFFFFFFFF)
    
    // Overlay Colors
    val OverlayLight: Color
        @Composable get() = Color(0x80FFFFFF)
    val OverlayDark: Color
        @Composable get() = Color(0x80000000)
    val OverlayPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    // Light Coffee Tone (for light, warm UI overlays/backgrounds)
    val LightCoffee: Color
        @Composable get() = if (isDark()) Color(0xFF3A2E25) else Color(0xFFEAD9C8)

    // Warm Browns for backgrounds (avoid green/beige feel)
    val WarmClay: Color
        @Composable get() = if (isDark()) Color(0xFF8B5A2B) else Color(0xFFB46A3A)
    val SoftSand: Color
        @Composable get() = if (isDark()) Color(0xFF8E7A64) else Color(0xFFD9B08C)
    val ToastedAlmond: Color
        @Composable get() = if (isDark()) Color(0xFF9E6A4F) else Color(0xFFC89272)
    
    // General text colors
    val TextGray: Color
        @Composable get() = if (isDark()) Color(0xFFCCCCCC) else Color(0xFF555555)
}

@Composable
private fun isDark(): Boolean {
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground
    return (bg == BackgroundDark && onBg == OnBackgroundDark) || (bg == SurfaceDark)
}
