# Realtime Notifications Implementation Plan

> **Status: COMPLETED** — Merged to main. Bu doküman referans amaçlıdır.

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace placeholder notifications with realtime Firestore notifications produced by backend reservation, horse health, and general announcement flows.

**Architecture:** Keep the existing `users/{uid}/notifications` Firestore listener in the Android app, and add a centralized backend notification writer plus targeted producers. Extend the app model only enough to support new notification types and future navigation metadata.

**Tech Stack:** Kotlin, Jetpack Compose, Firebase Firestore, Firebase Cloud Functions, TypeScript

---

### Task 1: Define Notification Contract

**Files:**
- Modify: `app/src/main/java/com/horsegallop/domain/notification/model/AppNotification.kt`
- Modify: `app/src/main/java/com/horsegallop/data/notification/repository/NotificationRepositoryImpl.kt`

**Step 1: Extend the domain model**

Add:
- `NotificationType.HORSE_HEALTH`
- `targetId: String?`
- `targetRoute: String?`

**Step 2: Update Firestore mapping**

Map new fields safely:
- preserve backward compatibility for old documents
- support `horse_health`

**Step 3: Verify compile**

Run:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

### Task 2: Add Backend Notification Writer Helper

**Files:**
- Modify: `backend/src/index.ts`

**Step 1: Create a single helper**

Add a helper that writes notification docs under:

```text
users/{uid}/notifications/{autoId}
```

Payload should include:
- `type`
- `title`
- `body`
- `timestamp`
- `isRead`
- optional `targetId`
- optional `targetRoute`

**Step 2: Reuse helper from all producers**

Do not duplicate notification write logic across functions.

**Step 3: Verify backend build**

Run:

```bash
cd backend && npm run build
```

Expected: `tsc` succeeds

### Task 3: Emit Reservation Notifications

**Files:**
- Modify: `backend/src/index.ts`
- Check related reservation callable in `backend/src/index.ts`

**Step 1: Find lesson booking / reservation creation flow**

Hook notification write immediately after successful reservation persistence.

**Step 2: Write user-facing copy**

Example shape:
- title: reservation created
- body: lesson/date summary

**Step 3: Verify no duplicate writes**

Ensure one reservation action writes one notification.

### Task 4: Emit Horse Health Reminder Notifications

**Files:**
- Modify: `backend/src/index.ts`

**Step 1: Add reminder scan helper**

Find horse health events due within the next 24 hours.

**Step 2: Add duplicate protection**

Use a stable key strategy such as:
- event id + reminder window
- or a marker field/document

**Step 3: Schedule or callable trigger**

For first implementation, use a lightweight scheduled function or controlled callable to generate reminders.

**Step 4: Build verification**

Run:

```bash
cd backend && npm run build
```

Expected: success

### Task 5: Add General Announcement Notification Entry Point

**Files:**
- Modify: `backend/src/index.ts`

**Step 1: Add controlled entry point**

Create a callable or helper for general notifications.

Constraints:
- authenticated
- ideally admin-guarded or limited for now

**Step 2: Reuse centralized writer**

Do not create a second notification schema.

### Task 6: Polish Notifications Screen For New Types

**Files:**
- Modify: `app/src/main/java/com/horsegallop/feature/notifications/presentation/NotificationsScreen.kt`
- Modify: `app/src/main/res/values/strings_core.xml`
- Modify: `app/src/main/res/values-tr/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

**Step 1: Add icon/render handling**

Support `HORSE_HEALTH` visually.

**Step 2: Improve copy**

Remove hardcoded strings where practical and add resource-backed text for:
- screen title
- loading
- empty state
- mark all read CTA

**Step 3: Keep UX stable**

Do not redesign the whole screen in this task.

### Task 7: Verify End-to-End

**Files:**
- Verify only

**Step 1: Android checks**

Run:

```bash
./gradlew --no-daemon --console=plain lintDebug testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`

**Step 2: Backend build**

Run:

```bash
cd backend && npm run build
```

Expected: success

**Step 3: Manual emulator smoke**

Run:

```bash
bash scripts/install-apk-emulator.sh
adb -s emulator-5554 shell am start -n com.horsegallop/.MainActivity
```

Expected: app launches and notifications screen remains accessible

### Task 8: Update Memory

**Files:**
- Modify: `memory.md`

**Step 1: Update backlog status**

Adjust section `4. Gercek Bildirimler`:
- move from `Bekliyor` to `Kismen tamamlandi` or `Tamamlandi` depending on result
- capture remaining follow-up items

**Step 2: Add audit note if needed**

If general notification entry point remains limited/admin-only, record that explicitly.

Plan complete and saved to `docs/plans/2026-03-14-realtime-notifications.md`. Two execution options:

1. Subagent-Driven (this session) - I dispatch fresh subagent per task, review between tasks, fast iteration

2. Parallel Session (separate) - Open new session with executing-plans, batch execution with checkpoints

Which approach?
