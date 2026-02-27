# HorseGallop Color Standard

## Purpose
Keep a clear visual depth between page backgrounds and UI components.  
Avoid flat, same-tone screens by using semantic surface tokens everywhere.

## Semantic Roles
- `screenBase`: primary page background.
- `screenTopBar`: top app bar and system bar base color.
- `panelOverlay`: floating controls and overlay containers.
- `cardElevated`: default card and form container surface.
- `cardSubtle`: low-emphasis inner sections and grouped blocks.
- `cardStroke`: card/input border color.
- `chipSelected` / `chipUnselected`: chip and filter states.

## Usage Rules
- Do use `LocalSemanticColors.current` in screens and components.
- Do keep visual hierarchy:
  - page -> `screenBase`
  - major containers -> `cardElevated`
  - nested grouped containers -> `cardSubtle`
  - floating controls -> `panelOverlay`
- Do use `cardStroke` for borders.

## Forbidden Patterns
- `CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)`
- `containerColor = MaterialTheme.colorScheme.background`
- `.background(MaterialTheme.colorScheme.background)`
- `color = MaterialTheme.colorScheme.surface` for container surfaces

## Allowed Exceptions
- Transparent overlays (`Color.Transparent`) for gradients/shimmer/masks.
- Image overlays that use `imageOverlayStrong` or `imageOverlaySoft`.

## Screen Checklist
Before merge, verify:
1. Cards are visually separated from page background in light mode.
2. Dark mode keeps readability and does not collapse layers.
3. Top bar is distinct from content surface.
4. No forbidden patterns in modified files.
