#!/usr/bin/env bash
# robotest.sh — Firebase Test Lab Robo Test
# Kullanım: bash scripts/robotest.sh [--build] [--timeout 5m] [--device Pixel8]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
FIREBASE_PROJECT="com-horse-gallop"

# Varsayılan parametreler
BUILD=false
TIMEOUT="5m"
DEVICE_MODEL="caiman"   # Pixel 9 Pro (physical) — API 34/35
DEVICE_API="35"
LOCALE="tr"

# Argüman parse
while [[ $# -gt 0 ]]; do
  case $1 in
    --build)   BUILD=true; shift ;;
    --timeout) TIMEOUT="$2"; shift 2 ;;
    --device)  DEVICE_MODEL="$2"; shift 2 ;;
    --api)     DEVICE_API="$2"; shift 2 ;;
    --locale)  LOCALE="$2"; shift 2 ;;
    *) echo "Bilinmeyen parametre: $1"; exit 1 ;;
  esac
done

echo "🤖 HorseGallop Robo Test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# gcloud kontrolü
if ! command -v gcloud &>/dev/null; then
  echo "❌ gcloud bulunamadı. Kur: brew install --cask google-cloud-sdk"
  exit 1
fi

# APK build (istenirse)
if [ "$BUILD" = true ]; then
  echo "📦 APK build ediliyor..."
  cd "$PROJECT_DIR"
  ./gradlew assembleDebug
  echo "✅ APK hazır"
fi

# APK varlık kontrolü
if [ ! -f "$APK_PATH" ]; then
  echo "❌ APK bulunamadı: $APK_PATH"
  echo "   Önce build et: bash scripts/robotest.sh --build"
  exit 1
fi

APK_SIZE=$(du -sh "$APK_PATH" | cut -f1)
echo "📱 APK: $APK_SIZE — $APK_PATH"
echo "🎯 Cihaz: $DEVICE_MODEL (API $DEVICE_API, $LOCALE)"
echo "⏱️  Timeout: $TIMEOUT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Test Lab'a gönderiliyor..."

gcloud firebase test android run \
  --project "$FIREBASE_PROJECT" \
  --type robo \
  --app "$APK_PATH" \
  --device "model=$DEVICE_MODEL,version=$DEVICE_API,locale=$LOCALE,orientation=portrait" \
  --timeout "$TIMEOUT" \
  --robo-directives \
    "click:agreement_checkbox,click:email_login_button,text:email_input=test@horsegallop.com,text:password_input=Test1234!,click:login_button" \
  2>&1

echo ""
echo "📊 Sonuçlar: https://console.firebase.google.com/project/$FIREBASE_PROJECT/testlab"
