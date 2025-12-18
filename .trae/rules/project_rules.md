# Android Debug Auto-Deploy

- Trigger: After every assistant prompt (and after code changes)
- Goal: Always run the latest app on the connected device/emulator

## Steps

1) Uninstall existing debug app (ignore errors):

```
~/Library/Android/sdk/platform-tools/adb uninstall com.horsegallop || true
```

2) Build debug APK:

```
./gradlew :app:assembleDebug -x test
```

3) Install on all connected devices/emulators:

```
./gradlew :app:assembleDebug -x test
APK=app/build/outputs/apk/debug/app-debug.apk
~/Library/Android/sdk/platform-tools/adb devices
for d in $(~/Library/Android/sdk/platform-tools/adb devices | awk 'NR>1 && $2=="device"{print $1}'); do
  ~/Library/Android/sdk/platform-tools/adb -s $d uninstall com.horsegallop || true
  ~/Library/Android/sdk/platform-tools/adb -s $d install -r $APK || true
  ~/Library/Android/sdk/platform-tools/adb -s $d shell am start -n com.horsegallop/.MainActivity || true
done
```

Notes:
- Requires one or more running emulators or connected devices.
- Emulators recommended: `Pixel_9` (çentikli) ve `Pixel_AOSP_API_33` (çentiksiz).
