# HorseGallop Color Standard (Brand Seed Dynamic, Light-Only)

## Source Of Truth
1. `/Users/gulcintas/Documents/HorseGallop/app/src/main/java/com/horsegallop/ui/theme/BrandThemeSpec.kt`
2. `/Users/gulcintas/Documents/HorseGallop/app/src/main/java/com/horsegallop/ui/theme/BrandColorEngine.kt`
3. `/Users/gulcintas/Documents/HorseGallop/app/src/main/java/com/horsegallop/ui/theme/Color.kt`
4. `/Users/gulcintas/Documents/HorseGallop/app/src/main/java/com/horsegallop/ui/theme/SemanticColors.kt`
5. `/Users/gulcintas/Documents/HorseGallop/app/src/main/java/com/horsegallop/ui/theme/ComponentColors.kt`
6. `/Users/gulcintas/Documents/HorseGallop/app/src/main/java/com/horsegallop/ui/theme/Theme.kt`

## Token Flow
1. `BrandThemeSpec` (saddle-brown seed set)
2. `buildLightBrandColorScheme(spec)` -> Material `ColorScheme`
3. `semanticColorsFrom(colorScheme, isDark = false)` -> screen/card/callout semantics
4. `componentColorsFrom(colorScheme, semanticColors)` -> button/input/tint/snackbar semantics

## Surface Usage Matrix
1. `screenBase`: Scaffold/root backgrounds.
2. `screenTopBar`: Top bars.
3. `cardElevated`: Main cards and primary panels.
4. `cardSubtle`: Secondary/nested panels.
5. `panelOverlay`: Menus, dropdowns, floating overlays.
6. `cardStroke`: Borders/dividers for surface separation.

## Component Usage Matrix
1. Buttons: `buttonPrimary*`, `buttonSecondary*`, `buttonTonal*`, `buttonDanger*`
2. Tint/Icon: `tintStrong`, `tintMuted`, `tintInverse`
3. Inputs: `inputContainer`, `inputContainerSubtle`, `inputBorderFocused`, `inputBorderUnfocused`
4. Feedback: `snackbarAction`, `snackbarDismiss`

## Forbidden In UI Layer
1. `Color(0x...)`
2. `Color.White`, `Color.Black`, `Color.Gray`
3. `containerColor = MaterialTheme.colorScheme.surface`
4. `containerColor = MaterialTheme.colorScheme.background`
5. `.background(MaterialTheme.colorScheme.surface/background)`

Guardrail task: `:app:enforceSemanticSurfaceTokens`

## XML Bridge Policy
1. XML color/theme files are bridge-only for splash/launcher/system theme bootstrap.
2. Runtime UI color decisions must come from Compose theme locals.

## Future Dark Mode
1. Current production mode is light-only.
2. `ThemeMode` is retained for forward compatibility.
3. Dark mode enablement will require explicit token pass, not ad-hoc overrides.
