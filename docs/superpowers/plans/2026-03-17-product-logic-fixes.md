# Product Logic Fixes — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 15 product logic issues identified in the codebase audit — remove Safety feature, fix navigation dead-ends, deduplicate screens, restructure Profile, add Schedule tabs, integrate features, and polish UX.

**Architecture:** Single-module Android (`:app`). All code under `com.horsegallop`. Navigation centralized in `AppNav.kt` via `Dest` sealed class. SemanticColors required everywhere — no direct Color usage.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, StateFlow, Navigation Compose, Material3, Firebase Cloud Functions.

**Build command:** `./gradlew assembleDebug` — must pass after every chunk.

---

## Chunk 1: Safety Feature Removal (Issue #12)

Remove all Safety code: domain, data, DI binding, ViewModel usage, UI dialogs, navigation route.

### Files to DELETE
- `app/src/main/java/com/horsegallop/feature/safety/presentation/SafetyScreen.kt`
- `app/src/main/java/com/horsegallop/feature/safety/presentation/SafetyViewModel.kt`
- `app/src/main/java/com/horsegallop/domain/safety/model/SafetySettings.kt`
- `app/src/main/java/com/horsegallop/domain/safety/model/SafetyContact.kt`
- `app/src/main/java/com/horsegallop/domain/safety/repository/SafetyRepository.kt`
- `app/src/main/java/com/horsegallop/domain/safety/usecase/AddSafetyContactUseCase.kt`
- `app/src/main/java/com/horsegallop/domain/safety/usecase/GetSafetySettingsUseCase.kt`
- `app/src/main/java/com/horsegallop/domain/safety/usecase/RemoveSafetyContactUseCase.kt`
- `app/src/main/java/com/horsegallop/domain/safety/usecase/TriggerSafetyAlarmUseCase.kt`
- `app/src/main/java/com/horsegallop/domain/safety/usecase/UpdateSafetyEnabledUseCase.kt`
- `app/src/main/java/com/horsegallop/data/safety/repository/SafetyRepositoryImpl.kt`

### Task 1.1: Delete Safety domain + data files

- [ ] **Step 1: Delete all files listed above**

```bash
BASE=app/src/main/java/com/horsegallop
rm -f $BASE/feature/safety/presentation/SafetyScreen.kt
rm -f $BASE/feature/safety/presentation/SafetyViewModel.kt
rm -f $BASE/domain/safety/model/SafetySettings.kt
rm -f $BASE/domain/safety/model/SafetyContact.kt
rm -f $BASE/domain/safety/repository/SafetyRepository.kt
rm -f $BASE/domain/safety/usecase/AddSafetyContactUseCase.kt
rm -f $BASE/domain/safety/usecase/GetSafetySettingsUseCase.kt
rm -f $BASE/domain/safety/usecase/RemoveSafetyContactUseCase.kt
rm -f $BASE/domain/safety/usecase/TriggerSafetyAlarmUseCase.kt
rm -f $BASE/domain/safety/usecase/UpdateSafetyEnabledUseCase.kt
rm -f $BASE/data/safety/repository/SafetyRepositoryImpl.kt
# Clean up empty dirs
find $BASE/feature/safety $BASE/domain/safety $BASE/data/safety -type d -empty -delete 2>/dev/null
```

- [ ] **Step 2: Commit deletions**

```bash
git add -A
git commit -m "chore: remove Safety feature domain/data/ui files"
```

### Task 1.2: Remove Safety from DataModule

**File:** `app/src/main/java/com/horsegallop/data/di/DataModule.kt`

- [ ] **Step 1: Remove these 3 lines from DataModule.kt**

Remove the import lines:
```kotlin
import com.horsegallop.data.safety.repository.SafetyRepositoryImpl
import com.horsegallop.domain.safety.repository.SafetyRepository
```

Remove the @Binds function (find and delete the entire function block):
```kotlin
@Binds @Singleton
abstract fun bindSafetyRepository(impl: SafetyRepositoryImpl): SafetyRepository
```

- [ ] **Step 2: Verify build compiles**

```bash
./gradlew assembleDebug 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL (or only safety-related errors remaining)

### Task 1.3: Remove Safety from RideTrackingViewModel + UiModels

**Files:**
- `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingViewModel.kt`
- `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingUiModels.kt`

- [ ] **Step 1: In RideTrackingViewModel.kt — remove Safety imports**

Remove lines:
```kotlin
import com.horsegallop.domain.safety.usecase.GetSafetySettingsUseCase
import com.horsegallop.domain.safety.usecase.TriggerSafetyAlarmUseCase
```

- [ ] **Step 2: In RideTrackingViewModel.kt — remove Safety constructor params**

Remove from `@HiltViewModel class RideTrackingViewModel @Inject constructor(...)`:
```kotlin
private val getSafetySettingsUseCase: GetSafetySettingsUseCase,
private val triggerSafetyAlarmUseCase: TriggerSafetyAlarmUseCase
```

- [ ] **Step 3: In RideTrackingViewModel.kt — remove Safety from init block**

Replace:
```kotlin
s.copy(
    showAutoStopDialog = true,
    showSafetyAlarmDialog = s.safetyEnabled
)
```
With:
```kotlin
s.copy(showAutoStopDialog = true)
```

Also remove the `loadSafetySettings()` call from `init` block (find the line `loadSafetySettings()` and remove it).

- [ ] **Step 4: In RideTrackingViewModel.kt — remove Safety functions**

Remove these entire functions:
- `fun dismissSafetyAlarmDialog()`
- `fun confirmSafetyAlarm()`
- `private fun loadSafetySettings()` (find and remove the entire function)

- [ ] **Step 5: In RideTrackingUiModels.kt — remove Safety fields from RideUiState**

Remove from `data class RideUiState(...)`:
```kotlin
val showSafetyAlarmDialog: Boolean = false,
val safetyEnabled: Boolean = false,
```

### Task 1.4: Remove SafetyAlarmDialog from RideTrackingScreen

**File:** `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingScreen.kt`

- [ ] **Step 1: Remove the dialog call**

Find and remove the block:
```kotlin
if (state.showSafetyAlarmDialog) {
    RideSafetyAlarmDialog(
        ...
    )
}
```

- [ ] **Step 2: Remove the RideSafetyAlarmDialog composable definition**

Find `private fun RideSafetyAlarmDialog(...)` and remove the entire composable.

### Task 1.5: Remove SafetyNavigationCard from SettingsScreen

**File:** `app/src/main/java/com/horsegallop/feature/settings/presentation/SettingsScreen.kt`

- [ ] **Step 1: Remove onSafety parameter from SettingsScreen**

Change:
```kotlin
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    onSafety: () -> Unit = {},
    ...
)
```
To:
```kotlin
fun SettingsScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    ...
)
```

- [ ] **Step 2: Remove SafetyNavigationCard call**

Find `SafetyNavigationCard(onClick = onSafety)` and remove it.

- [ ] **Step 3: Remove SafetyNavigationCard composable definition**

Find `private fun SafetyNavigationCard(onClick: () -> Unit)` and remove the entire composable (lines ~389–438 per grep).

### Task 1.6: Remove Safety from AppNav + FunctionsDtos

**Files:**
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`
- `app/src/main/java/com/horsegallop/data/remote/dto/FunctionsDtos.kt`
- `app/src/main/java/com/horsegallop/data/remote/functions/AppFunctionsDataSource.kt`

- [ ] **Step 1: In AppNav.kt — remove Safety import**

Remove:
```kotlin
import com.horsegallop.feature.safety.presentation.SafetyScreen
```

- [ ] **Step 2: In AppNav.kt — remove Dest.Safety**

Remove from sealed class Dest:
```kotlin
object Safety : Dest("safety")
```

- [ ] **Step 3: In AppNav.kt — remove Safety composable route**

Remove the block:
```kotlin
composable(Dest.Safety.route) {
    BackHandler { navController.popBackStack() }
    SafetyScreen(onBack = { navController.popBackStack() })
}
```

- [ ] **Step 4: In AppNav.kt — remove onSafety from Settings composable**

Change:
```kotlin
composable(Dest.Settings.route) {
    com.horsegallop.feature.settings.presentation.SettingsScreen(
        onBack = { navController.popBackStack() },
        onAccountDeleted = { ... },
        onSafety = { navController.navigate(Dest.Safety.route) }
    )
}
```
To:
```kotlin
composable(Dest.Settings.route) {
    com.horsegallop.feature.settings.presentation.SettingsScreen(
        onBack = { navController.popBackStack() },
        onAccountDeleted = { ... }
    )
}
```

- [ ] **Step 5: In FunctionsDtos.kt and AppFunctionsDataSource.kt — remove Safety DTOs and functions**

Search for safety-related DTOs/functions:
```bash
grep -n "safety\|Safety\|SafetyContact\|SafetySettings\|triggerSafety\|getSafety\|addSafetyContact\|removeSafetyContact" \
  app/src/main/java/com/horsegallop/data/remote/dto/FunctionsDtos.kt \
  app/src/main/java/com/horsegallop/data/remote/functions/AppFunctionsDataSource.kt
```

Remove all safety-related data classes, functions, and their imports.

- [ ] **Step 6: Build verify**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: remove Safety feature completely — replaced by simpler manual SOS design in future"
```

---

## Chunk 2: Quick Wins (#1, #7, #13)

### Task 2.1: Notification Route Validation (Issue #1)

**File:** `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

Add a route validation helper so invalid backend notification routes don't crash the app.

- [ ] **Step 1: Add validRoutes set inside AppNavHost**

Add after the `Dest` sealed class definition (top of AppNavHost function, before NavHost):

```kotlin
val validRoutes = remember {
    setOf(
        Dest.Home.route,
        Dest.Barns.route,
        Dest.Ride.route,
        Dest.Schedule.route,
        Dest.Profile.route,
        Dest.Notifications.route,
        Dest.MyReservations.route,
        Dest.RecentActivityDetail.route
        // parameterized routes are matched by prefix below
    )
}
```

- [ ] **Step 2: Replace direct navigate call with validated version**

Find:
```kotlin
onOpenTargetRoute = { route ->
    navController.navigate(route)
}
```

Replace with:
```kotlin
onOpenTargetRoute = { route ->
    val isValid = validRoutes.contains(route) ||
        route.startsWith("rideDetail/") ||
        route.startsWith("barnDetail/") ||
        route.startsWith("horseHealth/") ||
        route.startsWith("tbf_event_detail/")
    if (isValid) {
        navController.navigate(route)
    } else {
        com.horsegallop.core.debug.AppLog.w("AppNav", "Invalid notification route ignored: $route")
    }
}
```

- [ ] **Step 3: Build verify**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
```

- [ ] **Step 4: Commit**

```bash
git commit -m "fix: validate notification routes before navigating to prevent crashes"
```

---

### Task 2.2: Post-Ride Navigation to Activity List (Issue #7)

After ride ends, add "Sürüş Geçmişi" button to the existing SavedRideSummary card so users can reach their ride history in one tap.

**Files:**
- `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingScreen.kt`
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

- [ ] **Step 1: Add onViewRideHistory callback to RideTrackingRoute**

In `RideTrackingScreen.kt`, change:
```kotlin
@Composable
fun RideTrackingRoute(
    onHomeClick: () -> Unit,
    onBarnsClick: () -> Unit,
    ...
)
```
To:
```kotlin
@Composable
fun RideTrackingRoute(
    onHomeClick: () -> Unit,
    onBarnsClick: () -> Unit,
    onViewRideHistory: () -> Unit = {},
    ...
)
```

Pass `onViewRideHistory` down to `RideTrackingScreen(...)`.

- [ ] **Step 2: In RideTrackingScreen — find the SavedRideSummary card and add a button**

Search for `SavedSummaryCard` or `savedRideSummary` usage in the Screen composable. Inside that card (or after it), add:

```kotlin
// After ride stats are shown in savedRideSummary card:
OutlinedButton(
    onClick = onViewRideHistory,
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
    contentDescription = stringResource(R.string.ride_view_history_cd)
) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.List,
        contentDescription = null,
        modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = stringResource(R.string.ride_view_history))
}
```

- [ ] **Step 3: Add strings to strings.xml (both res/values/ and res/values-tr/)**

In `res/values/strings.xml`:
```xml
<string name="ride_view_history">View Ride History</string>
<string name="ride_view_history_cd">View ride history</string>
```

In `res/values-tr/strings.xml`:
```xml
<string name="ride_view_history">Sürüş Geçmişine Git</string>
<string name="ride_view_history_cd">Sürüş geçmişine git</string>
```

- [ ] **Step 4: Wire in AppNav.kt**

Find:
```kotlin
composable(Dest.Ride.route) {
    com.horsegallop.feature.ride.presentation.RideTrackingRoute(
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onBarnsClick = { navController.navigate(Dest.Barns.route) }
    )
}
```

Add `onViewRideHistory`:
```kotlin
composable(Dest.Ride.route) {
    com.horsegallop.feature.ride.presentation.RideTrackingRoute(
        onHomeClick = { navController.navigate(Dest.Home.route) },
        onBarnsClick = { navController.navigate(Dest.Barns.route) },
        onViewRideHistory = { navController.navigate(Dest.RecentActivityDetail.route) }
    )
}
```

- [ ] **Step 5: Build verify + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: add ride history shortcut after ride completes"
```

---

### Task 2.3: WriteReview Title by Target Type (Issue #13)

**File:** `app/src/main/java/com/horsegallop/feature/review/presentation/WriteReviewScreen.kt`

- [ ] **Step 1: Find the TopAppBar title in WriteReviewScreen**

Search for the `CenterAlignedTopAppBar` or `TopAppBar` in the file. Find its title text.

- [ ] **Step 2: Make the title dynamic based on targetType**

Replace static title like `stringResource(R.string.write_review_title)` with:

```kotlin
val titleRes = when (targetType) {
    ReviewTargetType.LESSON -> R.string.review_title_lesson
    ReviewTargetType.INSTRUCTOR -> R.string.review_title_instructor
}
// Use titleRes in TopAppBar title
```

- [ ] **Step 3: Add strings to strings.xml**

`res/values/strings.xml`:
```xml
<string name="review_title_lesson">Review Lesson</string>
<string name="review_title_instructor">Review Instructor</string>
```

`res/values-tr/strings.xml`:
```xml
<string name="review_title_lesson">Ders Değerlendirmesi</string>
<string name="review_title_instructor">Eğitmen Değerlendirmesi</string>
```

- [ ] **Step 4: Build verify + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "fix: show correct review type (lesson vs instructor) in WriteReview title"
```

---

## Chunk 3: Screen Deduplication (#2, #10)

### Task 3.1: Clarify HealthScreen vs HorseHealthScreen (Issue #2)

> **Note:** These are NOT exact duplicates — `HealthScreen` (feature/health) is a **user-level** health calendar (rider's own health events). `HorseHealthScreen` (feature/horse) is **horse-specific**. Both are valid, but their AppBar titles must make this clear.

**Files:**
- `app/src/main/java/com/horsegallop/feature/health/presentation/HealthScreen.kt`
- `app/src/main/java/com/horsegallop/feature/horse/presentation/HorseHealthScreen.kt`

- [ ] **Step 1: Verify HealthScreen title**

Read `HealthScreen.kt`. The TopAppBar title should be `stringResource(R.string.health_calendar_title)`. Verify the string value says "Sağlık Takvimim" or "My Health Calendar" (rider-focused, NOT horse-focused).

If it says just "Sağlık Takvimi" (ambiguous), update the string:

`res/values/strings.xml`:
```xml
<string name="health_calendar_title">My Health Calendar</string>
```
`res/values-tr/strings.xml`:
```xml
<string name="health_calendar_title">Sağlık Takvimim</string>
```

- [ ] **Step 2: Verify HorseHealthScreen title shows the horse name**

In `HorseHealthScreen.kt`, the TopAppBar title should include the horse name, e.g.:
```kotlin
title = { Text(text = horseName) }
```
If it doesn't, update it to show `horseName` as the screen title, making it clear this is the horse's health calendar.

- [ ] **Step 3: Commit**

```bash
git commit -m "fix: clarify Health Calendar screen titles — rider vs horse distinction"
```

---

### Task 3.2: Merge EquestrianAgenda + TBF into Unified News Screen (Issue #10)

These two features both show equestrian news/events. Merge them into a single `NewsScreen` with tabs. Access it from both Home (replacing EquestrianAgenda) and Profile (replacing TBF Events).

**Files to MODIFY:**
- `app/src/main/java/com/horsegallop/feature/equestrian/presentation/EquestrianAgendaScreen.kt` → becomes the merged screen
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt` → Dest.TbfEvents redirects to EquestrianAgenda
- `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileComponents.kt` → "TBF Events" button navigates to EquestrianAgenda route

**Files to DELETE (after merge):**
- `app/src/main/java/com/horsegallop/feature/tbf/presentation/TbfScreen.kt`
- `app/src/main/java/com/horsegallop/feature/tbf/presentation/TbfViewModel.kt`
- `app/src/main/java/com/horsegallop/feature/tbf/presentation/TbfEventDetailScreen.kt`
- `app/src/main/java/com/horsegallop/feature/tbf/presentation/TbfEventDetailViewModel.kt`

> **Decision:** Keep EquestrianAgendaScreen as the unified screen. Add a "TBF Yarışları" tab to it that shows the TBF events content (move TbfScreen content into a tab in EquestrianAgendaScreen). The separate TBF screens become a tab composable inside EquestrianAgenda.

- [ ] **Step 1: Read EquestrianAgendaScreen.kt to understand its current tab structure**

```bash
cat app/src/main/java/com/horsegallop/feature/equestrian/presentation/EquestrianAgendaScreen.kt
```

Note the existing tabs. It likely has "Yarışmalar", "Duyurular", "Sağlık" tabs.

- [ ] **Step 2: Read TbfScreen.kt to understand content**

```bash
cat app/src/main/java/com/horsegallop/feature/tbf/presentation/TbfScreen.kt
```

Note what TBF shows — venues, competitions, athletes.

- [ ] **Step 3: Add TBF value to EquestrianAgendaTab enum**

In `EquestrianAgendaViewModel.kt`, the existing enum is:
```kotlin
enum class EquestrianAgendaTab {
    ANNOUNCEMENTS,
    COMPETITIONS
}
```

Add the new value:
```kotlin
enum class EquestrianAgendaTab {
    ANNOUNCEMENTS,
    COMPETITIONS,
    TBF  // New tab for racing federation events
}
```

Then in `EquestrianAgendaScreen.kt`, find the `TabRow` and add one more tab using the correct enum type `EquestrianAgendaTab.TBF`:

```kotlin
// Add to the tabs list:
Tab(
    selected = selectedTab == EquestrianAgendaTab.TBF,
    onClick = { viewModel.selectTab(EquestrianAgendaTab.TBF) },
    text = { Text(stringResource(R.string.equestrian_tab_tbf)) }
)
```

Move the TBF content (venue list, competition cards) into a `TbfTabContent()` composable inside `EquestrianAgendaScreen.kt`. The TBF ViewModel data can be passed in or the EquestrianAgendaViewModel can absorb it.

> **Simplest approach:** The TBF tab just shows a list of events loaded by `TbfViewModel`. Keep `TbfViewModel` but use it inside `EquestrianAgendaScreen` (inject via `hiltViewModel()`). Show the content in the new tab.

- [ ] **Step 4: Update AppNav.kt — TbfEvents route redirects to EquestrianAgenda**

Change:
```kotlin
composable(Dest.TbfEvents.route) {
    BackHandler { navController.popBackStack() }
    TbfScreen(
        onBack = { navController.popBackStack() },
        onEventClick = { ... }
    )
}
```
To:
```kotlin
composable(Dest.TbfEvents.route) {
    BackHandler { navController.popBackStack() }
    EquestrianAgendaScreen(
        onBack = { navController.popBackStack() },
        initialTab = EquestrianAgendaTab.TBF  // Use correct enum type, not EquestrianTab
    )
}
```

Add `initialTab: EquestrianAgendaTab = EquestrianAgendaTab.ANNOUNCEMENTS` parameter to `EquestrianAgendaScreen`. In the composable's `init`/`LaunchedEffect`, call `viewModel.selectTab(initialTab)` once. Keep `Dest.TbfEvents` in the sealed class for backward compatibility.

- [ ] **Step 5: Remove TbfScreen but keep TbfViewModel + TbfEventDetailScreen**

> **IMPORTANT:** Do NOT delete `TbfViewModel.kt` — it is used inside `EquestrianAgendaScreen`'s TBF tab via `hiltViewModel()`.

Files to delete: Only `TbfScreen.kt` (the standalone screen). `TbfViewModel.kt`, `TbfEventDetailScreen.kt`, `TbfEventDetailViewModel.kt` all remain.

The TBF tab in `EquestrianAgendaScreen` uses `TbfViewModel` for data and navigates to `Dest.TbfEventDetail` route for detail views (same as before).

- [ ] **Step 6: Update the rename string for the action button in ProfileComponents**

In `ProfileComponents.kt`, find the action button that calls `onTbfEvents`. Update its label to show it navigates to the unified news screen:

```xml
<!-- res/values/strings.xml -->
<string name="profile_action_news">Equestrian News</string>
<!-- res/values-tr/strings.xml -->
<string name="profile_action_news">At Sporları Haberleri</string>
```

- [ ] **Step 7: Build verify + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: merge TBF events into EquestrianAgenda as a new tab — unified news screen"
```

---

## Chunk 4: Profile + Navigation Restructure (#3, #4, #5, #6)

### Task 4.1: Remove Redundant Settings from ProfileActionsCard (Issue #3)

Settings is already in the AppBar. Remove it from the card grid to reduce cognitive load.

**File:** `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileComponents.kt`

- [ ] **Step 1: In ProfileActionsCard — remove the Settings action item**

Find the action item block that calls `onClick = onSettings` inside `ProfileActionsCard` (around line 466 per grep). Remove it entirely.

Also remove `onSettings: () -> Unit` from `ProfileActionsCard`'s parameter list.

- [ ] **Step 2: Update ProfileScreen.kt — remove onSettings from ProfileActionsCard call**

In `ProfileScreen.kt`, find:
```kotlin
ProfileActionsCard(
    ...
    onSettings = onSettings,
    ...
)
```

Remove `onSettings = onSettings,` from the call.

Also update the Preview call at line ~277 (remove `onSettings = {}` from the preview ProfileActionsCard call).

- [ ] **Step 3: Add section headers to ProfileActionsCard for clarity**

Currently all 7 items look equal. Add two section headers using simple `Text` composables:

Before health/challenges/aicoach/tbf group:
```kotlin
Text(
    text = stringResource(R.string.profile_section_explore),
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
)
```

Add strings:
```xml
<!-- values/strings.xml -->
<string name="profile_section_explore">Explore</string>
<!-- values-tr/strings.xml -->
<string name="profile_section_explore">Keşfet</string>
```

- [ ] **Step 4: Build verify + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "refactor: remove redundant Settings button from ProfileActionsCard — already in AppBar"
```

---

### Task 4.2: Schedule Screen — Add Tabs (Issue #4)

Add "Ders Bul" and "Rezervasyonlarım" tabs to ScheduleScreen to unify lesson discovery and reservations.

**Files:**
- `app/src/main/java/com/horsegallop/feature/schedule/presentation/ScheduleScreen.kt`
- `app/src/main/java/com/horsegallop/feature/schedule/presentation/MyReservationsScreen.kt`
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

**Strategy:** Extract the content of `MyReservationsScreen` into a `MyReservationsContent()` composable. Use it as Tab 2 inside `ScheduleScreen`. Keep `MyReservationsScreen` as a standalone screen for backward-compatible deep links.

- [ ] **Step 1: Read MyReservationsScreen.kt to understand its content**

```bash
cat app/src/main/java/com/horsegallop/feature/schedule/presentation/MyReservationsScreen.kt
```

Note the composable structure. It likely has a Scaffold + LazyColumn with reservation cards.

- [ ] **Step 2: Extract MyReservationsContent from MyReservationsScreen.kt**

In `MyReservationsScreen.kt`, extract the LazyColumn (or main content area) into a separate composable:

```kotlin
@Composable
fun MyReservationsContent(
    viewModel: MyReservationsViewModel = hiltViewModel(),
    onWriteReview: (String, String) -> Unit = { _, _ -> }
) {
    // move the existing content here (LazyColumn with reservation cards)
}
```

Update `MyReservationsScreen` to call `MyReservationsContent(...)` inside its Scaffold.

- [ ] **Step 3: Add tab state to ScheduleScreen**

In `ScheduleScreen.kt`, add tab state to `ScheduleRoute`:

```kotlin
enum class ScheduleTab { BROWSE, MY_RESERVATIONS }

@Composable
fun ScheduleRoute(
    viewModel: ScheduleViewModel = hiltViewModel(),
    onMyReservations: () -> Unit = {},
    onWriteReview: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(ScheduleTab.BROWSE) }
    val uiState by viewModel.uiState.collectAsState()

    ScheduleScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onRetry = { viewModel.refresh() },
        onBookLesson = { lessonId -> viewModel.bookLesson(lessonId) },
        onClearBookingState = { viewModel.clearBookingState() },
        onWriteReview = onWriteReview
    )
}
```

- [ ] **Step 4: Add TabRow to ScheduleScreen Composable**

In `ScheduleScreen.kt`, add to the function signature:
```kotlin
selectedTab: ScheduleTab = ScheduleTab.BROWSE,
onTabSelected: (ScheduleTab) -> Unit = {},
onWriteReview: (String, String) -> Unit = { _, _ -> }
```

Inside the Scaffold, add `TabRow` below the TopAppBar (or use `topBar` to include it):

```kotlin
Column {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == ScheduleTab.BROWSE,
            onClick = { onTabSelected(ScheduleTab.BROWSE) },
            text = { Text(stringResource(R.string.schedule_tab_browse)) }
        )
        Tab(
            selected = selectedTab == ScheduleTab.MY_RESERVATIONS,
            onClick = { onTabSelected(ScheduleTab.MY_RESERVATIONS) },
            text = { Text(stringResource(R.string.schedule_tab_reservations)) }
        )
    }

    when (selectedTab) {
        ScheduleTab.BROWSE -> { /* existing ScheduleScreen content */ }
        ScheduleTab.MY_RESERVATIONS -> {
            MyReservationsContent(onWriteReview = onWriteReview)
        }
    }
}
```

Remove the existing "Rezervasyonlarım" button from the BROWSE tab content (the button that previously called `onMyReservations`).

- [ ] **Step 5: Add strings**

`res/values/strings.xml`:
```xml
<string name="schedule_tab_browse">Find Lessons</string>
<string name="schedule_tab_reservations">My Reservations</string>
```

`res/values-tr/strings.xml`:
```xml
<string name="schedule_tab_browse">Ders Bul</string>
<string name="schedule_tab_reservations">Rezervasyonlarım</string>
```

- [ ] **Step 6: Update AppNav.kt — add onWriteReview to ScheduleRoute**

```kotlin
composable(Dest.Schedule.route) {
    ScheduleRoute(
        onMyReservations = { navController.navigate(Dest.MyReservations.route) },
        onWriteReview = { lessonId, lessonTitle ->
            navController.navigate(Dest.WriteReview.route(lessonId, "lesson", lessonTitle))
        }
    )
}
```

- [ ] **Step 7: Build + @Preview update + commit**

Add `@Preview` for `ScheduleScreen` with `selectedTab = ScheduleTab.BROWSE` and another with `MY_RESERVATIONS`.

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: add tabs to Schedule screen — Ders Bul | Rezervasyonlarım"
```

---

### Task 4.3: Barn List/Map Toggle (Issue #5)

Replace full-screen BarnsMapViewScreen navigation with an inline AppBar toggle in BarnListScreen.

**Files:**
- `app/src/main/java/com/horsegallop/feature/barn/presentation/BarnListScreen.kt`
- `app/src/main/java/com/horsegallop/feature/barn/presentation/BarnsMapViewScreen.kt`
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

- [ ] **Step 1: Read BarnListScreen.kt to understand its current structure**

```bash
head -120 app/src/main/java/com/horsegallop/feature/barn/presentation/BarnListScreen.kt
```

Note how the "View Map" button works and where it calls navigation.

- [ ] **Step 2: Add ViewMode enum and state to BarnListScreen**

At the top of the file (inside the composable or as a file-level enum):

```kotlin
enum class BarnViewMode { LIST, MAP }
```

In `BarnListScreen` composable, add:
```kotlin
var viewMode by remember { mutableStateOf(BarnViewMode.LIST) }
```

- [ ] **Step 3: Replace "View Map" button with AppBar icon toggle**

In the TopAppBar `actions` block, add a toggle icon:

```kotlin
IconButton(onClick = { viewMode = if (viewMode == BarnViewMode.LIST) BarnViewMode.MAP else BarnViewMode.LIST }) {
    Icon(
        imageVector = if (viewMode == BarnViewMode.LIST) Icons.Filled.Map else Icons.AutoMirrored.Filled.List,
        contentDescription = stringResource(
            if (viewMode == BarnViewMode.LIST) R.string.barn_view_map_cd else R.string.barn_view_list_cd
        )
    )
}
```

- [ ] **Step 4: Inline the map content**

In the main content area, replace the list with a `when (viewMode)` block:

```kotlin
when (viewMode) {
    BarnViewMode.LIST -> { /* existing LazyColumn barn list */ }
    BarnViewMode.MAP -> {
        BarnMapContent(
            barns = uiState.barns,
            onBarnClick = onBarnClick
        )
    }
}
```

Extract `BarnMapContent` — it wraps the Google Map composable. You can take the map code from `BarnsMapViewScreen.kt` and put it in a `BarnMapContent` composable in `BarnListScreen.kt` (or a new file `BarnMapContent.kt` in the same package).

- [ ] **Step 5: Remove "View Map" floating/inline button from existing BarnListScreen**

Find the current Button/FAB that calls `navController.navigate(Dest.BarnsMapView.route)` and remove it.

- [ ] **Step 6: Keep BarnsMapViewScreen route for now (backward compat) but mark deprecated**

In `AppNav.kt`, keep the `Dest.BarnsMapView` composable but redirect it to `Dest.Barns.route`:
```kotlin
composable(Dest.BarnsMapView.route) {
    // Redirect to BarnList — map is now a toggle there
    LaunchedEffect(Unit) {
        navController.navigate(Dest.Barns.route) {
            popUpTo(Dest.BarnsMapView.route) { inclusive = true }
        }
    }
}
```

- [ ] **Step 7: Add strings**

```xml
<!-- values/strings.xml -->
<string name="barn_view_map_cd">Switch to map view</string>
<string name="barn_view_list_cd">Switch to list view</string>
<!-- values-tr/strings.xml -->
<string name="barn_view_map_cd">Harita görünümüne geç</string>
<string name="barn_view_list_cd">Liste görünümüne geç</string>
```

- [ ] **Step 8: Build + @Preview + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: replace BarnsMapView screen with inline list/map toggle in BarnListScreen AppBar"
```

---

### Task 4.4: Add "Ahırlarım" to Profile (Issue #6)

Barn owners need a direct path from Profile to their barn dashboard.

**Files:**
- `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileScreen.kt`
- `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileComponents.kt`
- `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileViewModel.kt`
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

**Strategy:** Load the user's owned barnId in `ProfileViewModel`. If not null, show "Ahırım" button in ProfileActionsCard. Tapping navigates to `BarnDashboardScreen`.

- [ ] **Step 1: Check ProfileViewModel for barnId state**

```bash
grep -n "barnId\|isOwner\|ownedBarn\|managedBarn" \
  app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileViewModel.kt
```

If not present, add it. Read `ProfileViewModel.kt` first:
```bash
cat app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileViewModel.kt
```

- [ ] **Step 2: Add ownedBarnId to ProfileUiState**

In `ProfileViewModel.kt`, find the `ProfileUiState` data class. Add:
```kotlin
val ownedBarnId: String? = null
```

- [ ] **Step 3: Load ownedBarnId in ProfileViewModel**

`BarnRepository.getBarns()` returns `Flow<List<BarnWithLocation>>` — not a `Result`.
`BarnUi` has `ownerUserId: String?` field (not `isOwner` boolean).
Ownership is determined by comparing `barn.ownerUserId` with the current user's ID.

Inject only `BarnRepository` in `ProfileViewModel` — `GetCurrentUserIdUseCase` is **already injected** in the existing constructor (do NOT add it again):

```kotlin
// Only add this new injection:
private val barnRepository: BarnRepository
```

In `init`:
```kotlin
// Find if user owns any barn
val currentUserId = getCurrentUserIdUseCase()
if (currentUserId != null) {
    barnRepository.getBarns()
        .onEach { barns ->
            val ownedBarnId = barns
                .firstOrNull { it.barn.ownerUserId == currentUserId }
                ?.barn?.id
            _uiState.update { it.copy(ownedBarnId = ownedBarnId) }
        }
        .launchIn(viewModelScope)
}
```

> Note: `GetCurrentUserIdUseCase` already exists in the project. Check exact return type — likely `String?` from `FirebaseAuth.currentUser?.uid`.

- [ ] **Step 4: Add "Ahırım" callback to ProfileScreen and ProfileActionsCard**

In `ProfileScreen.kt`:
```kotlin
fun ProfileScreen(
    ...
    onMyBarn: (barnId: String) -> Unit = {},
    ...
)
```

Pass `ownedBarnId` and `onMyBarn` to `ProfileActionsCard`:
```kotlin
ProfileActionsCard(
    ...
    ownedBarnId = state.ownedBarnId,
    onMyBarn = onMyBarn,
    ...
)
```

- [ ] **Step 5: Add "Ahırım" button in ProfileActionsCard**

In `ProfileComponents.kt`, in `ProfileActionsCard`, add conditionally:
```kotlin
if (ownedBarnId != null) {
    ProfileActionItem(
        icon = Icons.Filled.House,
        label = stringResource(R.string.profile_action_my_barn),
        onClick = { onMyBarn(ownedBarnId) }
    )
}
```

Add strings:
```xml
<!-- values/strings.xml -->
<string name="profile_action_my_barn">My Barn</string>
<!-- values-tr/strings.xml -->
<string name="profile_action_my_barn">Ahırım</string>
```

- [ ] **Step 6: Wire in AppNav.kt**

```kotlin
composable(Dest.Profile.route) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    ProfileScreen(
        ...
        onMyBarn = { barnId ->
            navController.navigate(Dest.BarnDashboard.route(barnId))
        },
        viewModel = profileViewModel
    )
}
```

- [ ] **Step 7: Build + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: add My Barn shortcut to Profile for barn owners"
```

---

## Chunk 5: Feature Integration (#8, #9, #11)

### Task 5.1: AI Coach — Ride Context (Issue #8)

Pass the user's most recent ride summary as context to the AI Coach, so the initial prompt is meaningful.

**Files:**
- `app/src/main/java/com/horsegallop/feature/aicoach/presentation/AiCoachViewModel.kt`
- `app/src/main/java/com/horsegallop/feature/aicoach/presentation/AiCoachScreen.kt`

- [ ] **Step 1: Read AiCoachViewModel.kt**

```bash
cat app/src/main/java/com/horsegallop/feature/aicoach/presentation/AiCoachViewModel.kt
```

Understand how messages are sent to the backend (`askAiCoach` Cloud Function).

- [ ] **Step 2: Inject RideHistoryRepository in AiCoachViewModel**

`RideHistoryRepository.getRideHistory()` returns `Flow<List<RideSession>>`. The domain type is `RideSession` (id, dateMillis, durationSec, distanceKm, calories, avgSpeedKmh, maxSpeedKmh, rideType, barnName).

In `AiCoachViewModel`, inject:
```kotlin
private val rideHistoryRepository: RideHistoryRepository
```

In `init`:
```kotlin
rideHistoryRepository.getRideHistory()
    .onEach { sessions ->
        val lastRide = sessions.firstOrNull()
        if (lastRide != null) {
            _uiState.update { it.copy(rideContext = buildRideContext(lastRide)) }
        }
    }
    .launchIn(viewModelScope)
```

Add to `AiCoachUiState`:
```kotlin
val rideContext: String? = null
```

Helper function (uses correct `RideSession` fields — `durationSec`, not `durationMin`):
```kotlin
private fun buildRideContext(ride: RideSession): String {
    val durationMin = ride.durationSec / 60
    return "Son sürüş: ${ride.distanceKm} km, $durationMin dk, " +
           "ortalama ${ride.avgSpeedKmh} km/s. " +
           "Tur tipi: ${ride.rideType ?: "bilinmiyor"}."
}
```

- [ ] **Step 3: Include rideContext in first message sent to AI**

When building the message payload to send to the AI, prepend the rideContext:
```kotlin
val fullMessage = if (rideContext != null && messages.isEmpty()) {
    "Bağlam: $rideContext\n\nKullanıcı sorusu: $userInput"
} else {
    userInput
}
```

- [ ] **Step 4: Show context hint in AiCoachScreen**

In `AiCoachScreen.kt`, when `rideContext != null`, show a subtle banner below the AppBar:
```kotlin
if (uiState.rideContext != null) {
    Text(
        text = stringResource(R.string.ai_coach_context_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
```

Add strings:
```xml
<!-- values/strings.xml -->
<string name="ai_coach_context_hint">Using your last ride data as context</string>
<!-- values-tr/strings.xml -->
<string name="ai_coach_context_hint">Son sürüş verilerin baz alınıyor</string>
```

- [ ] **Step 5: Build + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: send last ride context to AI Coach for personalized responses"
```

---

### Task 5.2: Challenges — Show Active Count Badge in Ride (Issue #9)

When a ride is active, show a small "X aktif meydan okuma" chip/badge on RideTrackingScreen so users know their progress matters.

**Files:**
- `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingViewModel.kt`
- `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingUiModels.kt`
- `app/src/main/java/com/horsegallop/feature/ride/presentation/RideTrackingScreen.kt`

- [ ] **Step 1: Inject GetActiveChallengesUseCase in RideTrackingViewModel**

`GetActiveChallengesUseCase` already exists at `domain/challenge/usecase/GetActiveChallengesUseCase.kt` and handles the userId internally. It returns `Flow<List<Challenge>>`. Inject it instead of `ChallengeRepository` directly:

Add to constructor:
```kotlin
private val getActiveChallengesUseCase: GetActiveChallengesUseCase
```

Add to `init` (collect as Flow):
```kotlin
getActiveChallengesUseCase()
    .onEach { challenges ->
        _uiState.update { it.copy(activeChallengeCount = challenges.size) }
    }
    .launchIn(viewModelScope)
```

- [ ] **Step 2: Add activeChallengeCount to RideUiState**

In `RideTrackingUiModels.kt`:
```kotlin
val activeChallengeCount: Int = 0
```

- [ ] **Step 3: Show challenge badge in RideTrackingScreen**

When ride is active and `activeChallengeCount > 0`, show a small chip (e.g., in the metrics area or below the map):

```kotlin
if (state.isRiding && state.activeChallengeCount > 0) {
    SuggestionChip(
        onClick = { /* no-op or navigate to challenges */ },
        label = {
            Text(
                text = stringResource(R.string.ride_active_challenges, state.activeChallengeCount),
                style = MaterialTheme.typography.labelSmall
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
```

Add string:
```xml
<!-- values/strings.xml -->
<string name="ride_active_challenges">%1$d active challenge(s)</string>
<!-- values-tr/strings.xml -->
<string name="ride_active_challenges">%1$d aktif meydan okuma</string>
```

- [ ] **Step 4: Build + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: show active challenge count badge during ride tracking"
```

---

### Task 5.3: My Reviews Screen (Issue #11)

Create a new screen showing the user's submitted reviews, accessible from Profile.

**Files to CREATE:**
- `app/src/main/java/com/horsegallop/feature/review/presentation/MyReviewsScreen.kt`

**Files to MODIFY:**
- `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileComponents.kt`
- `app/src/main/java/com/horsegallop/feature/auth/presentation/ProfileScreen.kt`
- `app/src/main/java/com/horsegallop/navigation/AppNav.kt`

- [ ] **Step 1: Verify that GetMyReviewsUseCase and getMyReviews() already exist**

Both already exist:
- `ReviewRepository.getMyReviews(): Flow<List<Review>>` — returns a `Flow`, not a `Result`
- `GetMyReviewsUseCase` at `domain/review/usecase/GetMyReviewsUseCase.kt`

No changes needed to domain or data layers.

- [ ] **Step 2: Create MyReviewsViewModel**

`getMyReviews()` is a **Flow** — collect with `.onEach`/`.launchIn`, not `.onSuccess`:

```kotlin
// app/.../feature/review/presentation/MyReviewsViewModel.kt
@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val getMyReviewsUseCase: GetMyReviewsUseCase
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val reviews: List<Review> = emptyList(),
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        getMyReviewsUseCase()
            .onEach { reviews ->
                _uiState.update { it.copy(isLoading = false, reviews = reviews) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }
}
```

- [ ] **Step 3: Create MyReviewsScreen.kt**

```kotlin
// app/.../feature/review/presentation/MyReviewsScreen.kt
// NOTE: @OptIn(ExperimentalMaterial3Api::class) required for CenterAlignedTopAppBar
@file:OptIn(ExperimentalMaterial3Api::class)
// OR add @OptIn at the function level:

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReviewsScreen(
    onBack: () -> Unit,
    viewModel: MyReviewsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val semantic = LocalSemanticColors.current

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.my_reviews_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> HorseLoadingOverlay(visible = true)
            state.reviews.isEmpty() -> EmptyReviewsPlaceholder(modifier = Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.reviews,
                    key = { it.id },
                    contentType = { "review" }
                ) { review ->
                    ReviewCard(review = review)
                }
            }
        }
    }
}

@Composable
private fun EmptyReviewsPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.my_reviews_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewCard(review: Review) {
    val semantic = LocalSemanticColors.current
    Card(
        colors = CardDefaults.cardColors(containerColor = semantic.cardElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(review.targetName, style = MaterialTheme.typography.titleSmall)
            // No RatingBar composable exists in core/components — use star icons directly:
            Row {
                repeat(5) { i ->
                    Icon(
                        imageVector = if (i < review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = semantic.ratingStar,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(review.comment, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyReviewsScreenPreview() {
    AppTheme {
        MyReviewsScreen(onBack = {})
    }
}
```

Add strings:
```xml
<!-- values/strings.xml -->
<string name="my_reviews_title">My Reviews</string>
<string name="my_reviews_empty">You haven\'t written any reviews yet.</string>
<!-- values-tr/strings.xml -->
<string name="my_reviews_title">Değerlendirmelerim</string>
<string name="my_reviews_empty">Henüz değerlendirme yazmadınız.</string>
```

- [ ] **Step 4: Add Dest.MyReviews to AppNav**

In `AppNav.kt`, add to sealed class:
```kotlin
object MyReviews : Dest("my_reviews")
```

Add composable:
```kotlin
composable(Dest.MyReviews.route) {
    BackHandler { navController.popBackStack() }
    com.horsegallop.feature.review.presentation.MyReviewsScreen(
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 5: Add to ProfileActionsCard + ProfileScreen**

In `ProfileComponents.kt`, add action:
```kotlin
ProfileActionItem(
    icon = Icons.Filled.Star,
    label = stringResource(R.string.profile_action_my_reviews),
    onClick = onMyReviews
)
```

Add `onMyReviews: () -> Unit = {}` parameter to `ProfileActionsCard`.

In `ProfileScreen.kt`, add callback and wire it.

In `AppNav.kt` Profile composable:
```kotlin
onMyReviews = { navController.navigate(Dest.MyReviews.route) }
```

Add string:
```xml
<!-- values/strings.xml -->
<string name="profile_action_my_reviews">My Reviews</string>
<!-- values-tr/strings.xml -->
<string name="profile_action_my_reviews">Değerlendirmelerim</string>
```

- [ ] **Step 6: Build + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: add My Reviews screen accessible from Profile"
```

---

## Chunk 6: UX Polish (#14, #15)

### Task 6.1: Subscription — Feature Preview (Issue #14)

Add a feature preview section to `SubscriptionScreen` before showing the price, so users understand the value before seeing the paywall.

**File:** `app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionScreen.kt`

- [ ] **Step 1: Read SubscriptionScreen.kt**

```bash
cat app/src/main/java/com/horsegallop/feature/subscription/presentation/SubscriptionScreen.kt
```

Understand current layout structure.

- [ ] **Step 2: Add ProFeatureRow composable**

Before the price/buy section, add a feature list:

```kotlin
@Composable
private fun ProFeaturesList() {
    val semantic = LocalSemanticColors.current
    val features = listOf(
        R.string.pro_feature_gait to Icons.Filled.Timeline,
        R.string.pro_feature_elevation to Icons.Filled.Landscape,
        R.string.pro_feature_training to Icons.Filled.FitnessCenter,
        R.string.pro_feature_calories to Icons.Filled.LocalFireDepartment,
        R.string.pro_feature_challenges to Icons.Filled.EmojiEvents
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = semantic.cardSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.pro_features_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            features.forEach { (textRes, icon) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(textRes), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
```

Add strings:
```xml
<!-- values/strings.xml -->
<string name="pro_features_title">What\'s included in Pro</string>
<string name="pro_feature_gait">Color-coded gait analysis (walk/trot/canter)</string>
<string name="pro_feature_elevation">Elevation profile charts</string>
<string name="pro_feature_training">Unlimited training plans</string>
<string name="pro_feature_calories">Horse calorie tracking</string>
<string name="pro_feature_challenges">Advanced challenges &amp; badges</string>
<!-- values-tr/strings.xml -->
<string name="pro_features_title">Pro\'da neler var</string>
<string name="pro_feature_gait">Renk kodlu yürüyüş analizi (yürüyüş/trot/galopp)</string>
<string name="pro_feature_elevation">Yükselti profili grafikleri</string>
<string name="pro_feature_training">Sınırsız antrenman planı</string>
<string name="pro_feature_calories">At kalori takibi</string>
<string name="pro_feature_challenges">Gelişmiş meydan okumalar ve rozetler</string>
```

- [ ] **Step 3: Place ProFeaturesList above the price/CTA in SubscriptionScreen**

Find where the price card / CTA button starts and insert `ProFeaturesList()` above it.

- [ ] **Step 4: Add @Preview update + build + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: add Pro feature preview list to SubscriptionScreen before paywall"
```

---

### Task 6.2: AddHorse — Quick Add Mode + Back Confirmation (Issue #15)

**Files:**
- `app/src/main/java/com/horsegallop/feature/horse/presentation/AddHorseScreen.kt`
- `app/src/main/java/com/horsegallop/feature/horse/presentation/HorseViewModel.kt` (or `AddHorseViewModel.kt` if separate)

- [ ] **Step 1: Read AddHorseScreen.kt to understand current form fields**

```bash
head -100 app/src/main/java/com/horsegallop/feature/horse/presentation/AddHorseScreen.kt
```

Note which fields are required vs optional.

- [ ] **Step 2: Add quickMode toggle state**

At the top of `AddHorseScreen`:
```kotlin
var quickMode by remember { mutableStateOf(true) }
```

- [ ] **Step 3: Add "Quick Add / Detailed" toggle switch**

Near the top of the form (below AppBar), add:
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        text = stringResource(R.string.add_horse_quick_mode_label),
        style = MaterialTheme.typography.bodyMedium
    )
    Switch(
        checked = quickMode,
        onCheckedChange = { quickMode = it }
    )
}
```

In Quick Mode (`quickMode == true`), only show: Name + Gender fields.
In Full Mode (`quickMode == false`), show all fields.

```kotlin
// Show name + gender always
HorseNameField(...)
HorseGenderField(...)

// Show rest only in full mode
if (!quickMode) {
    BreedDropdown(...)
    BirthYearField(...)
    ColorField(...)
    WeightSlider(...)
}
```

- [ ] **Step 4: Add back-press confirmation dialog if form has data**

Check if the ViewModel has any non-empty state before navigating back:

```kotlin
var showDiscardDialog by remember { mutableStateOf(false) }
val hasUnsavedData = uiState.name.isNotBlank()  // adjust field name to match ViewModel

BackHandler(enabled = hasUnsavedData) {
    showDiscardDialog = true
}

if (showDiscardDialog) {
    AlertDialog(
        onDismissRequest = { showDiscardDialog = false },
        title = { Text(stringResource(R.string.add_horse_discard_title)) },
        text = { Text(stringResource(R.string.add_horse_discard_message)) },
        confirmButton = {
            TextButton(onClick = { showDiscardDialog = false; onBack() }) {
                Text(stringResource(R.string.discard))
            }
        },
        dismissButton = {
            TextButton(onClick = { showDiscardDialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
```

Add strings:
```xml
<!-- values/strings.xml -->
<string name="add_horse_quick_mode_label">Quick add (name only)</string>
<string name="add_horse_discard_title">Discard changes?</string>
<string name="add_horse_discard_message">Your horse details will not be saved.</string>
<string name="discard">Discard</string>
<!-- values-tr/strings.xml -->
<string name="add_horse_quick_mode_label">Hızlı ekle (sadece isim)</string>
<string name="add_horse_discard_title">Değişiklikler iptal edilsin mi?</string>
<string name="add_horse_discard_message">At bilgileri kaydedilmeyecek.</string>
<string name="discard">İptal Et</string>
```

> Note: `cancel` string likely already exists in strings.xml. If it does, reuse it.

- [ ] **Step 5: Build + @Preview + commit**

```bash
./gradlew assembleDebug 2>&1 | grep -E "error:|BUILD"
git commit -m "feat: add quick mode and back-press confirmation to AddHorse form"
```

---

## Final Verification

- [ ] **Full build + lint + tests**

```bash
./gradlew clean assembleDebug lintDebug testDebugUnitTest 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Manual smoke test on emulator**
  - [ ] Profile → Settings icon works (AppBar)
  - [ ] Profile → Ahırlarım visible only for barn owners
  - [ ] Schedule → Both tabs work, reservations tab shows list
  - [ ] Barns → Map/List toggle in AppBar works
  - [ ] Ride finishes → "Sürüş Geçmişi" button appears
  - [ ] Notifications → invalid route does not crash
  - [ ] WriteReview → title shows correct type
  - [ ] AddHorse → Quick mode toggle works, back shows dialog
  - [ ] SubscriptionScreen → feature list visible before price
  - [ ] Safety → not accessible anywhere in app

- [ ] **Final commit**

```bash
git commit -m "chore: final cleanup and smoke test verification — product logic fixes complete"
```
