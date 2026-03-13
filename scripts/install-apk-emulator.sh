#!/usr/bin/env bash
# install-apk-emulator.sh
# Her task tamamlandığında (Stop hook) emülatöre güncel APK kurar.
# Emülatör çalışmıyorsa sessizce çıkar.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"

# Emülatör açık mı kontrol et
DEVICE=$(adb devices 2>/dev/null | grep -E "emulator-[0-9]+" | awk '{print $1}' | head -1)

if [ -z "$DEVICE" ]; then
  echo "ℹ️  Emülatör bulunamadı, APK kurulumu atlanıyor."
  exit 0
fi

echo "📱 Emülatör bulundu: $DEVICE"
echo "🔨 Debug APK derleniyor..."

cd "$PROJECT_ROOT"
./gradlew assembleDebug --quiet 2>&1 | tail -5

if [ ! -f "$APK_PATH" ]; then
  echo "❌ APK bulunamadı: $APK_PATH"
  exit 1
fi

echo "📦 APK emülatöre kuruluyor..."
adb -s "$DEVICE" install -r "$APK_PATH" 2>&1

echo "✅ APK başarıyla kuruldu → $DEVICE"
