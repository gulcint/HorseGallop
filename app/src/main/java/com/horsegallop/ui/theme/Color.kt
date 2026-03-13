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

// ─── Dark Theme — Warm Espresso Palette ─────────────────────────────────────
// Sıcak espresso/kakao tonları: siyah yerine derin sıcak kahve renkleri.
// Saddle Brown primary ile uyumlu, göze yorucu olmayan koyu tema.
private val DarkBg              = Color(0xFF1C1408) // Ana arka plan — derin espresso
private val DarkOnBg            = Color(0xFFEADFCF) // Üst metin — sıcak krem
private val DarkSurfaceVariant  = Color(0xFF3C3425) // Yüzey varyantı — koyu yanık ahşap
private val DarkOnSurfaceVar    = Color(0xFFD0C9B8) // Üst metin varyantı — bej gri

val DarkColorScheme = darkColorScheme(
    primary                = BrownMid,             // #FFB78B — parlak saddle brown
    onPrimary              = Color(0xFF4E2000),
    primaryContainer       = BrownDark,            // #5C2C0A
    onPrimaryContainer     = BrownLight,           // #FFD9C3

    secondary              = Color(0xFFC8C0AE),    // Sıcak bej-gri
    onSecondary            = Color(0xFF2A2014),    // Koyu espresso
    secondaryContainer     = Color(0xFF3A2F1E),    // Orta koyu kahve
    onSecondaryContainer   = Color(0xFFE4DACB),    // Açık krem

    tertiary               = Color(0xFFCFC8B5),    // Sıcak açık gri
    onTertiary             = Color(0xFF302916),
    tertiaryContainer      = Color(0xFF453C28),
    onTertiaryContainer    = Color(0xFFEBE3D0),

    // Warm amber replaces red in dark mode too
    error                  = Color(0xFFE8A000),    // Warm amber for dark mode
    onError                = Color(0xFF2D1300),
    errorContainer         = Color(0xFF3D2200),
    onErrorContainer       = Color(0xFFFFF3E0),

    background             = DarkBg,               // #1C1408 — derin espresso arka plan
    onBackground           = DarkOnBg,             // #EAE0CF — sıcak krem

    surface                = DarkBg,               // Arka planla aynı
    onSurface              = DarkOnBg,
    surfaceVariant         = DarkSurfaceVariant,   // #3C3425 — koyu ahşap
    onSurfaceVariant       = DarkOnSurfaceVar,     // #D0C9B8 — bej gri

    outline                = Color(0xFF998E7E),    // Sıcak orta gri
    outlineVariant         = Color(0xFF3C3425),

    inverseSurface         = Color(0xFFEADFCF),    // Açık krem
    inverseOnSurface       = Color(0xFF32271A),    // Koyu kahve
    inversePrimary         = SaddleBrown,

    surfaceDim             = Color(0xFF150F05),    // En koyu — neredeyse siyah-kahve
    surfaceBright          = Color(0xFF3C2E18),    // Daha açık — parlak yüzey
    surfaceContainerLowest = Color(0xFF100B02),    // Minimum yüzey
    surfaceContainerLow    = Color(0xFF201609),    // Düşük kap
    surfaceContainer       = Color(0xFF26190B),    // Orta kap
    surfaceContainerHigh   = Color(0xFF302210),    // Yüksek kap
    surfaceContainerHighest= Color(0xFF3A2B15),    // Maksimum kap

    surfaceTint            = BrownMid,
    scrim                  = Color(0xFF000000)
)
