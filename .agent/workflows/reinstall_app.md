---
description: Reinstalls the application on the connected emulator
---

1. Uninstall the existing application
// turbo
```bash
adb uninstall com.horsegallop
```

2. Install the debug APK
// turbo
```bash
./gradlew installDebug
```

3. Launch the application
// turbo
```bash
adb shell am start -n com.horsegallop/.MainActivity
```
