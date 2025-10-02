# HorseGallop

Modular Android app (Kotlin, Compose, Hilt, Coroutines, Retrofit, Room, Firebase).

## Modules
- app, core, domain, data, feature_* (auth, home, schedule, reservation, orders, reviews, admin)

## Requirements
- JDK 17, Android Studio Ladybug+, Android SDK 34

## Setup
1. Copy `google-services.json` to `app/`.
2. Set API base URL in `NetworkModule`.
3. Run `./gradlew :app:assembleDebug`.

## Dev workflow
- GitHub Flow: feature branches, PRs → CI runs.
- Mock API: run `json-server --watch db.json --port 3000` and set baseUrl to `http://10.0.2.2:3000/`.
- Localization: put `strings_en.json` in your config bucket; app loads on start.
- Feature rule: each `:feature_*` depends only on `:core` and `:domain`.

## Testing
- Unit tests: `./gradlew testDebugUnitTest`
- UI tests: `./gradlew connectedDebugAndroidTest`

## Security
- HTTPS only, tokens stored encrypted, short-lived access tokens, refresh rotation.

## Mock API
```bash
cd mock-api
npm install
npm start
```

## Architecture
- Clean Architecture with MVVM
- Hilt for DI
- Flow for reactive programming
- Retrofit for networking
- Room for local storage
- Firebase for auth and messaging
