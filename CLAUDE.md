# CLAUDE.md

## Session Start

- Read `memory.md` first, then this file.
- Multi-agent work: use `.claude/context/shared/agent-contracts.md` + task files under `.claude/context/tasks/<task-id>/`.
- Agent model: `Conductor (tech-lead) → Researcher / Builder / Operator → Reviewer (qa-verifier)`

## Build Commands

```bash
bash scripts/setup-hooks.sh        # one-time git hook setup
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew androidQualityConventions
bash scripts/pr-pipeline-merge.sh  # run local gate + open/update PR
```

Git hooks: `pre-commit` → conflict/syntax check | `pre-push` → lint + tests
Claude hooks: `PreToolUse` blocks dangerous commands | `PostToolUse` auto-runs Gradle | `SubagentStart` injects context paths

`enforceSemanticSurfaceTokens` runs on every `preBuild` — **build fails** on direct color usage in `feature/`, `core/`, `navigation/`. Use `LocalSemanticColors.current` tokens only.

## Agent Orchestration

**When to use agents:**
- Direct answer: question / debug / 1-2 file change
- Agents (tech-lead): new feature (3+ layers), cross-cutting refactor, ambiguous scope

**QA Gate (mandatory after every implementation):** `qa-verifier` PASS required before saying "done" / "ready" / "commit". FAIL → fix → re-run QA → then commit.

**New multi-step task:**
```bash
python3 scripts/init_claude_task_context.py --task-id T-123 --title "Task Title"
```

## Architecture

**Single-module monolith** — only `:app`. All code under `com.horsegallop`.

```
domain/{feature}/model/         → Pure Kotlin data classes
domain/{feature}/repository/    → Interfaces
domain/{feature}/usecase/       → Single-responsibility business logic
data/{feature}/repository/      → Implements domain interfaces
data/remote/functions/          → AppFunctionsDataSource (all Firebase calls)
data/remote/dto/                → FunctionsDtos.kt
data/di/                        → DataModule, FirebaseModule, NetworkModule
feature/{feature}/presentation/ → Screen + ViewModel pairs
core/components/                → Shared Composables
navigation/AppNav.kt            → All routes + NavHost
ui/theme/                       → SemanticColors, Type, Theme
```

All remote calls: `AppFunctionsDataSource` → Firebase Cloud Functions.

DI: `@Binds @Singleton` in `DataModule`. ViewModels: `@HiltViewModel` + `@Inject constructor`.

Routes: sealed class `Dest` in `AppNav.kt`. Bottom nav: Home, Barns, Ride, Schedule, Profile.

## Design System

**Never use direct colors in `feature/`, `core/`, `navigation/`, `MainActivity.kt`.**

```kotlin
val semantic = LocalSemanticColors.current
// semantic.screenBase, .cardElevated, .cardSubtle, .cardStroke,
// .success, .warning, .destructive, .ratingStar, .panelOverlay
```

ViewModel state: one `UiState` data class, `StateFlow`, collect with `collectAsStateWithLifecycle()`.

## Firebase

- Functions region: `us-central1`
- App Check: `DebugAppCheckProviderFactory` (debug) / `PlayIntegrityAppCheckProviderFactory` (release)
- FCM channels: `general`, `reservation`, `lesson`
- Auth: Email/password + Google. Password reset deep link: `horsegallop.page.link/reset-password`

## Google Play Billing

`BillingManager` is `@Singleton`. Billing flow requires Activity — call `billingManager.launchBillingFlow(activity, productId)`. Product IDs: `horsegallop_pro_monthly`, `horsegallop_pro_yearly`.

## Compose Rules

1. `LazyColumn`/`LazyRow` — always `key()` + `contentType()`
2. Expensive calc → `remember()`
3. Derived state → `derivedStateOf()`
4. Lambdas → hoist or `remember { {} }`
5. Data classes → `@Stable`/`@Immutable` where possible
6. `contentDescription` — all interactive elements via `stringResource()`

## Key Constraints

- `stringResource()` cannot be called inside `LaunchedEffect` — pre-compute outside
- `@OptIn(ExperimentalMaterial3Api::class)` for `CenterAlignedTopAppBar`, `ModalBottomSheet`
- Turkish strings with apostrophes: `Pro\'ya Geç`
- Min SDK 24, Target SDK 34, JVM 17
