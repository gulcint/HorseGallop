---
description: Runs the local pre-PR verification gate for the Android app
---

1. Run Android quality conventions
// turbo
```bash
./gradlew androidQualityConventions --no-daemon --console=plain
```

2. Run lint and unit tests
// turbo
```bash
./gradlew lintDebug testDebugUnitTest --no-daemon --console=plain
```

3. Run the existing PR pipeline helper
// turbo
```bash
bash scripts/pr-pipeline-merge.sh
```
