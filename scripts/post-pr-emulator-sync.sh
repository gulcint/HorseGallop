#!/usr/bin/env bash
set -euo pipefail

APP_ID="com.horsegallop"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
ADB="${ADB:-$(command -v adb || true)}"
if [[ -z "${ADB}" ]]; then
  ADB="/Users/gulcintas/Library/Android/sdk/platform-tools/adb"
fi
EMULATOR_BIN="${EMULATOR_BIN:-/Users/gulcintas/Library/Android/sdk/emulator/emulator}"
AVD_NAME="${AVD_NAME:-Pixel_9}"

is_emulator_running() {
  "$ADB" devices | awk 'NR>1 {print $1}' | grep -q '^emulator-'
}

ensure_emulator() {
  if is_emulator_running; then
    echo "Emulator already running"
    return
  fi

  echo "Starting emulator: $AVD_NAME"
  nohup "$EMULATOR_BIN" -avd "$AVD_NAME" >/tmp/horsegallop-emulator.log 2>&1 &

  echo "Waiting for boot completion..."
  "$ADB" wait-for-device
  until "$ADB" shell getprop sys.boot_completed 2>/dev/null | grep -m 1 '1' >/dev/null; do
    sleep 2
  done
  "$ADB" shell input keyevent 82 >/dev/null 2>&1 || true
}

build_apk() {
  ./gradlew :app:assembleDebug --no-daemon
}

install_fresh() {
  if "$ADB" shell pm list packages | grep -q "$APP_ID"; then
    echo "Removing old package: $APP_ID"
    "$ADB" uninstall "$APP_ID" || true
  fi

  echo "Installing fresh APK: $APK_PATH"
  "$ADB" install -r "$APK_PATH"
}

main() {
  ensure_emulator
  build_apk
  install_fresh
  echo "Done: fresh debug APK installed on emulator"
}

main "$@"
