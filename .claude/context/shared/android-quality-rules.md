# Android Quality Rules

Bu repo icin otomatik Android kalite kontrolleri su sinyallere odaklanir:

- `compileDebugKotlin`: Kotlin degisikliklerinde compile sanity
- `testDebugUnitTest`: uretim Kotlin kodu degistikten sonra unit test gate
- `lintDebug`: UI, manifest, resource veya Gradle degisikliklerinde lint gate
- `androidQualityConventions`: custom repo kurallari

## Custom Convention Checks

- `enforceSemanticSurfaceTokens`: feature/core/navigation/MainActivity katmanlarinda dogrudan surface/background renk kullanimini engeller
- `androidQualityConventions`: degisen dosyalarda su kontrolleri uygular:
  - hardcoded UI string
  - `@Preview` eksikligi
  - `IconButton` / `FloatingActionButton` icin `stringResource` tabanli `contentDescription`
  - `strings_core.xml` ile `values-tr/strings.xml` ve `values-en/strings.xml` senkronizasyonu

## Tasarim Ilkesi

Hook bu task'lari tetikler. Asil kalite mantigi repo icindeki Gradle task veya script'te yasar.
