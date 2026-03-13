package com.horsegallop.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─── White Elegance + Saddle Brown Brand Palette ───────────────────────────
// Crisp whites punctuated by warm Saddle Brown → clean, sophisticated, modern.

val SaddleBrown     = Color(0xFF8B4513) // Primary brand — rich equestrian warmth
val DustGrey        = Color(0xFFDADDD8) // Subtle dusted-stone surface tone
val Parchment       = Color(0xFFECEBE4) // Heritage elegance — card surfaces
val Platinum        = Color(0xFFEEF0F2) // Understated silver-white — elevated surfaces
val GhostWhite      = Color(0xFFFAFAFF) // Pristine clean background

// Derived accessible tones
private val BrownDark       = Color(0xFF5C2C0A) // Deep brown — containers / on-colors
private val BrownDeep       = Color(0xFF2D1300) // Near-black brown — on primary container
private val BrownLight      = Color(0xFFFFD9C3) // Warm apricot — primary container light
private val BrownMid        = Color(0xFFFFB78B) // Warm orange-brown — primary dark mode
private val WarmCharcoal    = Color(0xFF1C1C1E) // Near-black with warmth — primary text
private val WarmGrey70      = Color(0xFF7A7873) // Warm mid-grey — secondary light
private val WarmGrey30      = Color(0xFF49473F) // Dark warm grey — secondary container dark
private val WarmGrey15      = Color(0xFF323028) // Very dark warm grey — onSecondary dark

// ─── Horse Health Event Type Colors ────────────────────────────────────────
// Semantic accent colors for health event types — used in HorseHealthScreen.
val HealthColorFarrier    = Color(0xFF795548)   // Brown — farrier/nalbant
val HealthColorVaccination= Color(0xFF2196F3)   // Blue — vaccination
val HealthColorDental     = Color(0xFF00BCD4)   // Cyan — dental
val HealthColorVet        = Color(0xFFF44336)   // Red — vet
val HealthColorDeworming  = Color(0xFF4CAF50)   // Green — deworming
val HealthColorOther      = Color(0xFF9E9E9E)   // Grey — other

val LightColorScheme = lightColorScheme(
    primary                = SaddleBrown,
    onPrimary              = Color.White,
    primaryContainer       = BrownLight,
    onPrimaryContainer     = BrownDeep,

    secondary              = WarmGrey70,
    onSecondary            = Color.White,
    secondaryContainer     = DustGrey,
    onSecondaryContainer   = WarmCharcoal,

    tertiary               = Color(0xFF6B6A61),
    onTertiary             = Color.White,
    tertiaryContainer      = Parchment,
    onTertiaryContainer    = WarmCharcoal,

    // Warm amber replaces red — brand-aligned "caution" without harsh red
    error                  = Color(0xFFB45309),   // Burnt sienna / warm amber
    onError                = Color.White,
    errorContainer         = Color(0xFFFFF3E0),   // Very light amber tint
    onErrorContainer       = Color(0xFF2D1300),   // Deep brown on amber

    background             = GhostWhite,
    onBackground           = WarmCharcoal,

    surface                = GhostWhite,
    onSurface              = WarmCharcoal,
    surfaceVariant         = Platinum,
    onSurfaceVariant       = Color(0xFF47464B),

    outline                = Color(0xFF787680),
    outlineVariant         = DustGrey,

    inverseSurface         = WarmCharcoal,
    inverseOnSurface       = GhostWhite,
    inversePrimary         = BrownMid,

    surfaceDim             = DustGrey,
    surfaceBright          = GhostWhite,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow    = GhostWhite,
    surfaceContainer       = Platinum,
    surfaceContainerHigh   = Parchment,
    surfaceContainerHighest= DustGrey,

    surfaceTint            = SaddleBrown,
    scrim                  = WarmCharcoal
)

val DarkColorScheme = darkColorScheme(
    primary                = BrownMid,
    onPrimary              = Color(0xFF4E2000),
    primaryContainer       = BrownDark,
    onPrimaryContainer     = BrownLight,

    secondary              = Color(0xFFC5C3BC),
    onSecondary            = WarmGrey15,
    secondaryContainer     = WarmGrey30,
    onSecondaryContainer   = Color(0xFFE1DFD7),

    tertiary               = Color(0xFFCAC7BA),
    onTertiary             = Color(0xFF322F21),
    tertiaryContainer      = Color(0xFF494536),
    onTertiaryContainer    = Color(0xFFE6E3D5),

    // Warm amber replaces red in dark mode too
    error                  = Color(0xFFE8A000),   // Warm amber for dark mode
    onError                = Color(0xFF2D1300),   // Deep brown on amber
    errorContainer         = Color(0xFF3D2200),   // Dark amber container
    onErrorContainer       = Color(0xFFFFF3E0),   // Light amber tint text

    background             = Color(0xFF131318),
    onBackground           = Color(0xFFE5E1E9),

    surface                = Color(0xFF131318),
    onSurface              = Color(0xFFE5E1E9),
    surfaceVariant         = Color(0xFF47464F),
    onSurfaceVariant       = Color(0xFFC8C6D0),

    outline                = Color(0xFF918F9A),
    outlineVariant         = Color(0xFF47464F),

    inverseSurface         = Color(0xFFE5E1E9),
    inverseOnSurface       = Color(0xFF313033),
    inversePrimary         = SaddleBrown,

    surfaceDim             = Color(0xFF0F0F14),
    surfaceBright          = Color(0xFF39383E),
    surfaceContainerLowest = Color(0xFF0A0A0F),
    surfaceContainerLow    = Color(0xFF1C1B22),
    surfaceContainer       = Color(0xFF201F26),
    surfaceContainerHigh   = Color(0xFF2B2930),
    surfaceContainerHighest= Color(0xFF36343B),

    surfaceTint            = BrownMid,
    scrim                  = Color(0xFF000000)
)
