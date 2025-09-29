# Build Instructions

## Prerequisites
- Android Studio (latest stable version)
- JDK 17
- Android SDK 34

## Setup Gradle Wrapper
If you don't have gradlew, generate it:

```bash
gradle wrapper --gradle-version=8.5 --distribution-type=all
```

Or use Android Studio:
1. Open project in Android Studio
2. File → Sync Project with Gradle Files
3. Gradle wrapper will be auto-generated

## Build Commands

### Using Android Studio
1. Open project
2. Build → Make Project (Ctrl+F9 / Cmd+F9)
3. Run → Run 'app' (Shift+F10 / Ctrl+R)

### Using Gradle (after wrapper is generated)
```bash
# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease

# Run tests
./gradlew testDebugUnitTest

# Clean build
./gradlew clean build
```

## Fixed Issues
- ✅ Gradle bundle naming (compose-base → compose_base)
- ✅ Localization using Accept-Language header
- ✅ Removed titleTr field in favor of server-side localization
