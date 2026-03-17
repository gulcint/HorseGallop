---
description: Runs the Android QA workflow using the repo-native checks and reports the result
---

1. Run quick Android quality checks
// turbo
```bash
python3 scripts/android_quality_checks.py --project-dir .
```

2. Run compile sanity
// turbo
```bash
./gradlew compileDebugKotlin --no-daemon --console=plain
```

3. Run lint and unit tests
// turbo
```bash
./gradlew lintDebug testDebugUnitTest --no-daemon --console=plain
```
