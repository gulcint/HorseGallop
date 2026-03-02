# HorseGallop Backend (Firebase Cloud Functions)

This folder contains backend code used by the Android app.

## Callable Functions
- `getUserProfile`: returns authenticated user profile from `users/{uid}`
- `updateUserProfile`: updates authenticated user profile fields
- `getHomeDashboard`: returns home stats + recent activities
- `getBarns`: returns barn list
- `getBarnDetail`: returns single barn detail
- `getLessons`: returns lesson list (optional date range)
- `getAppContent`: returns locale-based dynamic content blocks
  - `home`: hero title/subtitle
  - `auth`: login, email-login, enroll, forgot-password copy
  - `onboarding`: hero + help copy
  - `ride`: live header + permission copy
  - `settings`: section subtitle/help copy
  - `common`: offline help text

## Firestore Collections (contract)
- `users/{uid}`
- `rides/{rideId}`
- `barns/{barnId}`
- `lessons/{lessonId}`
- `app_content/{locale}`

## Local setup
1. Install dependencies:
   - `cd backend`
   - `npm install`
2. Build:
   - `npm run build`
3. Tests:
   - `npm test`
4. (Optional) Run emulator:
   - `npm run serve`

## Seed data (dev/test)
- `npm run seed`

This command seeds minimum sample data for `barns`, `lessons`, and `app_content`.

## Deploy
- `cd backend`
- `npm run deploy -- --project <YOUR_FIREBASE_PROJECT_ID>`

## Android integration
Android app calls these functions via Firebase Functions SDK (region: `us-central1`).
