# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Session Start

- Once `memory.md`, then read `CLAUDE.md`.
- Treat `memory.md` as the persistent product backlog and project memory file.
- For multi-step agent work, use `.claude/context/shared/agent-contracts.md` and task-specific files under `.claude/context/tasks/<task-id>/`.
- The agent model is: `Conductor -> Researcher / Builder / Operator -> Reviewer`, with Memory/State in the task folder.

## Build Commands

```bash
# One-time git hook setup
bash scripts/setup-hooks.sh

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Run repo-native Android convention checks
./gradlew androidQualityConventions

# Clean build
./gradlew clean assembleDebug

# Open PR only after local gate passes
bash scripts/pr-pipeline-merge.sh
```

Git hook policy:
- `pre-commit`: conflict marker + shell syntax checks
- `pre-push`: mandatory `lintDebug` + `testDebugUnitTest`
- PR creation: use `bash scripts/pr-pipeline-merge.sh`; it runs the same local gate, opens/updates the PR, then enables auto-merge

Claude hook policy:
- `PreToolUse`: dangerous shell/adb/git/gradle commands are blocked or require approval
- `PostToolUse`: Kotlin and Android file edits can trigger compile/test/lint/quality checks
- `Notification`: desktop notification for approvals or waiting states
- `Stop`: task completion notification + sound
- `SubagentStart`: injects canonical context-path contract into subagents

The `enforceSemanticSurfaceTokens` task runs automatically on every `preBuild` and will **fail the build** if direct color usage is detected in the feature/core/navigation layers. Fix by using `LocalSemanticColors.current` tokens instead of `Color(0xFF...)`, `Color.White`, `Color.Black`, or `MaterialTheme.colorScheme.surface/background`.

## Agent Orchestration — Ne Zaman Kullan?

**Direkt cevap ver** (agent yok):
- Soru / açıklama / debug analizi
- 1-2 dosya, net scope (tek string değişikliği, tek bug fix)

**Direkt kod değişikliği yap** (agent yok):
- Küçük edit, tek composable, tek fonksiyon

**tech-lead / conductor → agentlar** (orkestra):
- Yeni feature (domain + data + feature katmanları)
- 3+ dosya etkileyen refactor
- "Tüm X'lere Y ekle" gibi geniş scope değişiklikler

Karar ağacı:
```
Kod değişikliği var mı?
├─ HAYIR → Direkt cevap
└─ EVET
       ├─ 1-2 dosya, net scope → Direkt değişiklik
       └─ 3+ dosya / yeni feature / belirsiz scope → conductor → agentlar
```

### Agent Dispatch Protokolü (Task Tool)

`tech-lead` mevcut conductor roludur. Agentlar yoluna girildiğinde, alt gorevler **Task tool** ile ayri subagent olarak dispatch edilir. "Mentioning" degil, gercek Task cagrisi:

```
Task: android-feature
Görev: [Ne yapılacağı, hangi katmanlar]
Kısıtlar: [SemanticColors, string kuralları, @Preview]
Context oku: `.claude/context/tasks/<task-id>/handoffs/android-feature.md`
Artifact yaz: `.claude/context/tasks/<task-id>/artifacts/android-feature.md`
Kod alanı: [İlgili mevcut dosya yolları]
Beklenen çıktı: [Hangi dosyalar oluşturulacak/değişecek]
```

Rol seti:
- `Conductor`: `tech-lead` agent'i
- `Researcher`: repo / docs / issue / API / web arastirmasi
- `Builder`: `android-feature`, `firebase-backend`, `ui-craft`
- `Reviewer`: `qa-verifier`
- `Operator`: hook, workflow, logging, notification, deploy otomasyonu
- `Memory/State`: `.claude/context/tasks/<task-id>/`

Bağımsız alt görevler (örn. Firebase backend + domain modeli) **paralel** dispatch edilebilir.

Task klasoru yoksa önce:
```bash
python3 scripts/init_claude_task_context.py --task-id T-123 --title "Task Title"
```

Bu komut su dosyalari olusturur:
- `brief.md`
- `state.json`
- `decision-log.md`
- `open-questions.md`
- `artifacts/index.md`

### QA Gate — Atlanamaz

Her implementasyon sonrası `qa-verifier` **zorunludur**:

- `qa-verifier` PASS vermeden şunlar söylenemez: "tamamlandı", "hazır", "commit edebilirsin"
- FAIL → ilgili agent'a geri gönder, düzelt, tekrar QA
- Soru / açıklama / tek satır fix → QA gate geçerli değil

```
✅ QA PASS alındı → Commit öner
❌ QA FAIL → Düzelt → Tekrar QA → Sonra commit
```

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

## Compose Performance Rules

These rules apply to all Composables in `feature/`, `core/`, and `navigation/`:

1. **`LazyColumn`/`LazyRow`** — always use `key()` and `contentType()` for list items
2. **Expensive calculations** — wrap in `remember()` to avoid recomputation on every recomposition
3. **Derived state** — use `derivedStateOf()` when a value depends on another state
4. **Lambdas** — never create lambdas inside composables; hoist them up or use `remember { {} }`
5. **Data classes** — annotate with `@Stable` or `@Immutable` where possible to reduce recompositions
6. **Composable size** — break large composables into smaller focused ones; no single composable doing everything
7. **`Modifier.fillMaxSize()`** — prefer over `Box` with large padding for full-screen layouts
8. **Reusable composables** — extract shared UI into `core/components/` with clear, minimal inputs
9. **Profiling** — use Compose Compiler metrics and Layout Inspector to verify recomposition counts
10. **contentDescription** — all interactive elements (`IconButton`, `FAB`, clickable `Box/Row`) must have `contentDescription` via `stringResource()`

## Key Constraints

- **`stringResource()` cannot be called inside `LaunchedEffect` or coroutine scope** — pre-compute strings before the effect
- **`@OptIn(ExperimentalMaterial3Api::class)`** required for `CenterAlignedTopAppBar`, `ModalBottomSheet`, etc.
- **Turkish strings** with apostrophes must be escaped: `Pro\'ya Geç` not `Pro'ya Geç`
- **Min SDK 24**, Target SDK 34, JVM 17

## Context Topology

- Ortak baglam: `.claude/context/shared/`
- Gorev baglami: `.claude/context/tasks/<task-id>/`
- Karar kaydi: `.claude/context/tasks/<task-id>/decision-log.md`
- Acik sorular: `.claude/context/tasks/<task-id>/open-questions.md`
- Handoff: `.claude/context/tasks/<task-id>/handoffs/<agent-name>.md`
- Artifact: `.claude/context/tasks/<task-id>/artifacts/<agent-name>.md`
- Artifact index: `.claude/context/tasks/<task-id>/artifacts/index.md`
- QA raporu: `.claude/context/tasks/<task-id>/reports/qa.md`

Subagent'lar serbest mesaj zincirini degil, kendilerine verilen context path'lerini birincil kaynak kabul etmelidir.
