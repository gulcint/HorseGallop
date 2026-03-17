# Gradle Check Matrix

## Hook Trigger Matrix

- Kotlin dosyasi (`.kt`, `.kts`) degisirse:
  - `./gradlew compileDebugKotlin --no-daemon --console=plain`

- Uretim Kotlin kodu (`app/src/main/...`) degisirse:
  - `./gradlew testDebugUnitTest --no-daemon --console=plain`

- UI, manifest, resource veya Gradle dosyasi degisirse:
  - `./gradlew lintDebug --no-daemon --console=plain`
  - `./gradlew androidQualityConventions --no-daemon --console=plain`

## Debounce

- compile: 45s
- test: 60s
- lint: 90s
- quality: 60s

Amaç aynı edit serisinde gereksiz Gradle firtınası oluşturmamak.
