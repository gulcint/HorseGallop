# Ride Detail Polyline Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Improve the route rendering quality in `RideDetailScreen` by filtering noisy points, drawing gait-colored segments, and clarifying the start/end map presentation.

**Architecture:** Keep the ride data model intact, add a small presentation-side route processing utility for the detail screen, and render segmented polylines plus a compact legend. Avoid touching the live tracking pipeline in this iteration.

**Tech Stack:** Kotlin, Jetpack Compose, Google Maps Compose

---

### Task 1: Inspect Current Ride Detail Rendering

**Files:**
- Modify: `app/src/main/java/com/horsegallop/feature/ride/presentation/RideDetailScreen.kt`

**Step 1: Identify current polyline rendering**

Find:
- how map bounds are calculated
- how path points are passed to `Polyline`
- whether start/end markers already exist

**Step 2: Keep current behavior as fallback**

Do not delete working behavior before new pipeline is ready.

### Task 2: Add Route Processing Utility

**Files:**
- Create: `app/src/main/java/com/horsegallop/feature/ride/presentation/RideRouteSegments.kt`

**Step 1: Define data structures**

Add small UI-only models such as:
- filtered point
- route segment

**Step 2: Add filtering**

Implement:
- minimum-distance filter
- impossible-jump guard
- same-point collapse

**Step 3: Add gait segmentation**

Build ordered segments by gait.

**Step 4: Compile verification**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

### Task 3: Render Segmented Polylines In Ride Detail

**Files:**
- Modify: `app/src/main/java/com/horsegallop/feature/ride/presentation/RideDetailScreen.kt`

**Step 1: Swap single polyline rendering**

Use processed segments instead of one raw path.

**Step 2: Improve visual quality**

Adjust:
- stroke width
- color by gait
- cap/join settings

**Step 3: Preserve fallback**

If no valid segments exist, render minimal safe state.

### Task 4: Improve Start / End Marker Presentation

**Files:**
- Modify: `app/src/main/java/com/horsegallop/feature/ride/presentation/RideDetailScreen.kt`

**Step 1: Add explicit markers**

Show:
- start
- finish

**Step 2: Handle short rides**

If route has one point:
- show only one marker

### Task 5: Improve Camera Fit

**Files:**
- Modify: `app/src/main/java/com/horsegallop/feature/ride/presentation/RideDetailScreen.kt`

**Step 1: Compute bounds from filtered route**

Use processed points, not raw list.

**Step 2: Add stable padding**

Ensure camera fit works inside the card layout.

### Task 6: Add Compact Gait Legend

**Files:**
- Modify: `app/src/main/java/com/horsegallop/feature/ride/presentation/RideDetailScreen.kt`
- Modify: `app/src/main/res/values/strings_core.xml`
- Modify: `app/src/main/res/values-tr/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Step 1: Add resource strings**

Legend labels for:
- walk
- trot
- canter
- gallop

**Step 2: Render compact legend**

Keep it visually small and close to the map.

### Task 7: Verify

**Files:**
- Verify only

**Step 1: Kotlin compile**

Run:

```bash
./gradlew --no-daemon --console=plain :app:compileDebugKotlin
```

Expected: success

**Step 2: Full Android gate**

Run:

```bash
./gradlew --no-daemon --console=plain lintDebug testDebugUnitTest
```

Expected: success

**Step 3: Emulator smoke**

Run:

```bash
bash scripts/install-apk-emulator.sh
adb -s emulator-5554 shell am start -n com.horsegallop/.MainActivity
```

Expected: app opens and ride detail remains accessible

### Task 8: Update Memory

**Files:**
- Modify: `memory.md`

**Step 1: Update section 2**

Move `Harita & Polyline` from `Bekliyor` to `Kismen tamamlandi` if this ships.

**Step 2: Record next follow-up**

Add note that live tracking still needs the shared pipeline in a later pass.

Plan complete and saved to `docs/plans/2026-03-14-ride-detail-polyline.md`. Two execution options:

1. Subagent-Driven (this session) - I dispatch fresh subagent per task, review between tasks, fast iteration

2. Parallel Session (separate) - Open new session with executing-plans, batch execution with checkpoints

Which approach?
