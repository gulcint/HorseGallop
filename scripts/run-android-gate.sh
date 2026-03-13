#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ensure_android_sdk() {
  if [[ -n "${ANDROID_HOME:-}" && -d "${ANDROID_HOME}" ]]; then
    return 0
  fi

  if [[ -f local.properties ]]; then
    local sdk_dir
    sdk_dir="$(grep '^sdk.dir=' local.properties | sed 's#^sdk.dir=##' | sed 's#\\:#:#g' | tail -n 1 || true)"
    if [[ -n "$sdk_dir" && -d "$sdk_dir" ]]; then
      export ANDROID_HOME="$sdk_dir"
      return 0
    fi
  fi

  if [[ -d "$HOME/Library/Android/sdk" ]]; then
    export ANDROID_HOME="$HOME/Library/Android/sdk"
    return 0
  fi

  echo "Android SDK bulunamadi. ANDROID_HOME ayarlayin veya local.properties icine sdk.dir ekleyin." >&2
  exit 1
}

ensure_google_maps_key() {
  if [[ -n "${GOOGLE_MAPS_API_KEY:-}" ]]; then
    return 0
  fi

  if [[ -f local.properties ]] && grep -q '^GOOGLE_MAPS_API_KEY=' local.properties; then
    return 0
  fi

  echo "GOOGLE_MAPS_API_KEY bulunamadi. local.properties veya environment icinde tanimlayin." >&2
  exit 1
}

ensure_google_services() {
  if [[ -s app/google-services.json ]]; then
    return 0
  fi

  echo "app/google-services.json eksik. Firebase build adimlari icin bu dosya gereklidir." >&2
  exit 1
}

ensure_android_sdk
ensure_google_maps_key
ensure_google_services

chmod +x ./gradlew

echo "Android gate calisiyor: lintDebug + testDebugUnitTest"
./gradlew lintDebug testDebugUnitTest --no-daemon --stacktrace
