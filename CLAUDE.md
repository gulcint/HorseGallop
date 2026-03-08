# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Clean build
./gradlew clean assembleDebug
```

The `enforceSemanticSurfaceTokens` task runs automatically on every `preBuild` and will **fail the build** if direct color usage is detected in the feature/core/navigation layers. Fix by using `LocalSemanticColors.current` tokens instead of `Color(0xFF...)`, `Color.White`, `Color.Black`, or `MaterialTheme.colorScheme.surface/background`.

## Architecture

**Single-module monolith** — only `:app` in `settings.gradle.kts`. All code lives under `com.horsegallop`.

### Layer Structure

```
domain/{feature}/model/        → Pure Kotlin data classes, no Android deps
domain/{feature}/repository/   → Interfaces only
domain/{feature}/usecase/      → Single-responsibility business logic

data/{feature}/repository/     → Implements domain interfaces
data/remote/functions/         → AppFunctionsDataSource (all Firebase calls)
data/remote/dto/               → FunctionsDtos.kt (all DTOs in one file)
data/di/                       → DataModule, FirebaseModule, NetworkModule

feature/{feature}/presentation/ → Screen + ViewModel pairs
core/components/               → Shared Composables
core/debug/AppLog.kt           → Use instead of Log directly
navigation/AppNav.kt           → All routes + NavHost wiring
ui/theme/                      → Theme, SemanticColors, TextColors, Type
```

### Data Flow

All remote calls go through `AppFunctionsDataSource` → Firebase Cloud Functions. The pattern:

```kotlin
// Repository
override suspend fun bookLesson(lessonId: String): Result<Reservation> = runCatching {
    functionsDataSource.bookLesson(lessonId).toDomain()
}

// ViewModel
viewModelScope.launch {
    repo.bookLesson(id)
        .onSuccess { _ui.update { it.copy(result = it) } }
        .onFailure { _ui.update { it.copy(error = it.message) } }
}
```

### DI Pattern

Repositories are bound in `DataModule` using `@Binds @Singleton`:
```kotlin
@Binds @Singleton
abstract fun bindHorseRepository(impl: HorseRepositoryImpl): HorseRepository
```

ViewModels use `@HiltViewModel` + `@Inject constructor`.

### Navigation

All routes defined in sealed class `Dest` in `AppNav.kt`. Parameterized routes use helper functions:
```kotlin
Dest.WriteReview.route(targetId, targetType, android.net.Uri.encode(targetName))
```

Bottom nav is visible on: Home, Barns, Ride, Schedule, Profile.

### Design System

**Never use direct colors in `feature/`, `core/`, `navigation/`, or `MainActivity.kt`.**

Always use:
```kotlin
val semantic = LocalSemanticColors.current
// semantic.screenBase, semantic.cardElevated, semantic.cardSubtle,
// semantic.success, semantic.warning, semantic.destructive,
// semantic.cardStroke, semantic.panelOverlay, semantic.ratingStar, etc.
```

SemanticColors are derived from the Material3 `ColorScheme` in `semanticColorsFrom()`. The brand palette is defined in `Color.kt` (LightBronze, DesertSand, AlmondCream, DrySage, DustyOlive, AshGrey).

### ViewModel State Pattern

One `UiState` data class per screen, exposed as `StateFlow`:
```kotlin
data class ExampleUiState(
    val loading: Boolean = true,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

private val _ui = MutableStateFlow(ExampleUiState())
val ui: StateFlow<ExampleUiState> = _ui
```

Collect in Composable with `collectAsStateWithLifecycle()`.

## Firebase

- **Functions region**: `us-central1`
- **App Check**: `DebugAppCheckProviderFactory` in debug, `PlayIntegrityAppCheckProviderFactory` in release
- **FCM**: Token saved to Firestore on each `onNewToken`. Channels: `general`, `reservation`, `lesson`
- **Auth**: Email/password + Google Sign-In. Deep links for password reset: `horsegallop.page.link/reset-password`

## Google Play Billing

`BillingManager` is a `@Singleton` that wraps `BillingClient`. Billing flow **requires an Activity reference** — call `billingManager.launchBillingFlow(activity, productId)` from the ViewModel, passing the Activity from `LocalContext.current as? Activity` in the Composable.

Product IDs: `horsegallop_pro_monthly`, `horsegallop_pro_yearly`.

## Implemented Features

Auth (Google + Email), Onboarding, Home, Ride Tracking, Schedule + Booking, Barn Discovery + Map, Training Plans (Pro gated), Settings, Horse Profile management, Reviews, FCM push notifications, Google Play Billing.

## Key Constraints

- **`stringResource()` cannot be called inside `LaunchedEffect` or coroutine scope** — pre-compute strings before the effect
- **`@OptIn(ExperimentalMaterial3Api::class)`** required for `CenterAlignedTopAppBar`, `ModalBottomSheet`, etc.
- **Turkish strings** with apostrophes must be escaped: `Pro\'ya Geç` not `Pro'ya Geç`
- **Min SDK 24**, Target SDK 34, JVM 17
