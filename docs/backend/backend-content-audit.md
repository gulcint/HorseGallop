# Backend Content Audit (Final Cleanup Pass)

## Completed (backend-driven)

### Home
- Hero title/subtitle now from `getAppContent.home`.

### Common
- No-internet helper text now from `getAppContent.common.offlineHelp`.

### Auth
- Login hero title/subtitle now from `getAppContent.auth.loginTitle/loginSubtitle`.
- Email login title/subtitle now from `getAppContent.auth.emailLoginTitle/emailLoginSubtitle`.
- Enrollment header title/subtitle now from `getAppContent.auth.enrollTitle/enrollSubtitle`.
- Forgot password subtitle now from `getAppContent.auth.forgotPasswordSubtitle`.

### Onboarding
- Hero title/subtitle now from `getAppContent.onboarding.heroTitle/heroSubtitle`.
- Extra helper copy now from `getAppContent.onboarding.helpText`.

### Ride
- Live header title/subtitles now from `getAppContent.ride`.
- Permission card title/hint/CTA now from `getAppContent.ride`.

### Settings
- Section subtitles/help copy now from `getAppContent.settings`.

## Intentionally remaining local/fallback

These remain in Android resources by design:
- UI control labels and button actions (`OK`, `Back`, `Cancel`, field labels).
- Validation and error key mapping fallbacks.
- Units/short stat labels (`km`, `kcal`, speed unit), i18n-safe.
- Fallback copy when backend content is null/unavailable.

## Remaining non-backend candidates (optional next pass)

If full remote-copy is desired for **all** visible strings, next candidates:
- `feature/ride/presentation/RideTrackingScreen.kt`:
  - stop confirmation dialog title/message
  - sync status labels
  - metric labels/units
  - ride type and barn selector helper labels
- `feature/auth/presentation/ForgotPasswordScreen.kt`:
  - top title and form labels
- `feature/settings/presentation/SettingsScreen.kt`:
  - action row labels (`export`, `delete`, dialog actions)

## Notes
- `strings.xml` is preserved as fallback + locale safety.
- This pass targets backend-first content blocks while keeping UX resilient offline.
