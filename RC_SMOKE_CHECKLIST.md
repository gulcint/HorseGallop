# HorseGallop RC Smoke Checklist

This checklist is for release-candidate validation after merge to `main`.

## 1) Build and Quality Gate

Run locally (or validate in CI):

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
./gradlew :app:lintDebug --no-daemon
```

## 2) Firebase Test Lab (Required for RC)

Trigger the workflow manually with `run_ftl=true`:

1. GitHub Actions -> `Android CI + Firebase Test Lab`
2. `Run workflow`
3. Set `run_ftl` to `true`

Expected:

- `android-build-test`: success
- `android-lint`: success
- `firebase-test-lab`: success (not skipped)

## 3) Manual Smoke Scenarios

Validate on a test device/emulator:

1. Login flow works.
2. Profile edit/save works.
3. Start ride works with location permission.
4. Stop ride saves local summary.
5. Ride detail screen opens and renders metrics/map.
6. Pending sync -> retry -> synced status flow works.

## 4) RC Note Template

Copy this block into release notes / PR comment:

```md
## RC Validation

- Build/Test: PASS/FAIL
- Lint: PASS/FAIL
- FTL Run URL: <paste>
- APK Artifact URL: <paste>

### Smoke
- Login: PASS/FAIL
- Profile Save: PASS/FAIL
- Start/Stop Ride: PASS/FAIL
- Ride Detail: PASS/FAIL
- Pending Sync Retry: PASS/FAIL
```
