# HorseGallop Backend (Firebase Cloud Functions)

This folder contains backend code for profile operations used by the Android app.

## Functions
- `getUserProfile` (callable): returns authenticated user's profile from `users/{uid}`.
- `updateUserProfile` (callable): updates authenticated user's profile fields.

## Local setup
1. Install dependencies:
   - `cd backend`
   - `npm install`
2. Build:
   - `npm run build`
3. (Optional) Run emulator:
   - `npm run serve`

## Deploy
- `cd backend`
- `npm run deploy -- --project <YOUR_FIREBASE_PROJECT_ID>`

## Android integration
Android app calls these callable functions via Firebase Functions SDK:
- `httpsCallable("getUserProfile")`
- `httpsCallable("updateUserProfile")`
