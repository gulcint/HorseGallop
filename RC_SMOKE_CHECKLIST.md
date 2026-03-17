# HorseGallop RC Smoke Checklist

This checklist is for release-candidate validation after merge to `main`.

## 1) Build and Quality Gate

Run locally (or validate in CI):

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
./gradlew :app:lintDebug --no-daemon
```

## 2) Manual Smoke Scenarios

Validate on a test device/emulator:

1. Login flow works.
2. Profile edit/save works.
3. Start ride works with location permission.
4. Stop ride saves local summary.
5. Ride detail screen opens and renders metrics/map.
6. Pending sync -> retry -> synced status flow works.

## 3) RC Note Template

Copy this block into release notes / PR comment:

```md
## RC Validation

- Build/Test: PASS/FAIL
- Lint: PASS/FAIL
- APK Artifact URL: <paste>

### Smoke
- Login: PASS/FAIL
- Profile Save: PASS/FAIL
- Start/Stop Ride: PASS/FAIL
- Ride Detail: PASS/FAIL
- Pending Sync Retry: PASS/FAIL
```
