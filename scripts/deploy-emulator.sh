#!/bin/bash
# deploy-emulator.sh — Emülatör başlat (gerekirse) + Build + Install + Launch
# Usage: bash scripts/deploy-emulator.sh [AVD_NAME]
# Optional: bash scripts/deploy-emulator.sh --skip-build (just reinstall last APK)
# Default AVD: Pixel_9

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APK_PATH="$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
APP_ID="com.horsegallop"
DEFAULT_AVD="Pixel_9"
EMULATOR_BIN="$HOME/Library/Android/sdk/emulator/emulator"
ADB_BIN="$HOME/Library/Android/sdk/platform-tools/adb"

# PATH'e ekle
export PATH="$HOME/Library/Android/sdk/platform-tools:$HOME/Library/Android/sdk/emulator:$PATH"

cd "$PROJECT_ROOT"

# Renk kodları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}HorseGallop — Deploy${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 1. ADB kontrol
if ! command -v adb &> /dev/null && [ ! -f "$ADB_BIN" ]; then
  echo -e "${RED}adb bulunamadı. Android SDK Platform Tools yüklü mü?${NC}"
  exit 1
fi
ADB="${ADB_BIN}"

# 2. Emülatör çalışıyor mu?
DEVICES=$($ADB devices | grep -v "List of devices" | grep -v "^$" | grep "device$" | awk '{print $1}')
DEVICE_COUNT=$(echo "$DEVICES" | grep -c "." 2>/dev/null || echo "0")

if [ "$DEVICE_COUNT" -eq 0 ]; then
  # AVD adını argümandan al ya da default kullan
  AVD_NAME="${1:-$DEFAULT_AVD}"
  if [ "$AVD_NAME" = "--skip-build" ]; then AVD_NAME="$DEFAULT_AVD"; fi

  echo -e "${YELLOW}Emülatör bulunamadı. Başlatılıyor: $AVD_NAME${NC}"

  if [ ! -f "$EMULATOR_BIN" ]; then
    echo -e "${RED}Emülatör binary bulunamadı: $EMULATOR_BIN${NC}"
    exit 1
  fi

  nohup "$EMULATOR_BIN" -avd "$AVD_NAME" -no-audio > /tmp/emulator.log 2>&1 &
  echo "   Emülatör başlatıldı (PID: $!), boot bekleniyor..."

  # Boot tamamlanana kadar bekle (max 120s)
  TIMEOUT=120
  ELAPSED=0
  while true; do
    BOOT_STATUS=$($ADB shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
    if [ "$BOOT_STATUS" = "1" ]; then
      echo -e "${GREEN}Emülatör hazır!${NC}"
      break
    fi
    if [ "$ELAPSED" -ge "$TIMEOUT" ]; then
      echo -e "${RED}Emülatör $TIMEOUT saniyede boot olmadı.${NC}"
      exit 1
    fi
    sleep 3
    ELAPSED=$((ELAPSED + 3))
    echo "   Bekleniyor... (${ELAPSED}s)"
  done

  DEVICES=$($ADB devices | grep -v "List of devices" | grep -v "^$" | grep "device$" | awk '{print $1}')
fi

# Birden fazla cihaz varsa emülatörü tercih et
DEVICE_COUNT=$(echo "$DEVICES" | grep -c "." 2>/dev/null || echo "0")
if [ "$DEVICE_COUNT" -gt 1 ]; then
  EMULATOR_DEVICE=$(echo "$DEVICES" | grep "emulator" | head -1)
  TARGET_DEVICE="${EMULATOR_DEVICE:-$(echo "$DEVICES" | head -1)}"
  echo -e "${YELLOW}Birden fazla cihaz — seçilen: $TARGET_DEVICE${NC}"
else
  TARGET_DEVICE="$DEVICES"
fi

echo -e "🎯 Hedef: ${YELLOW}$TARGET_DEVICE${NC}"

# 3. Build (--skip-build flag yoksa)
if [ "$1" != "--skip-build" ]; then
  echo ""
  echo -e "${BLUE}🔨 Build alınıyor...${NC}"
  ./gradlew assembleDebug --quiet
  echo -e "${GREEN}✅ Build başarılı${NC}"
else
  echo -e "${YELLOW}⏭️  Build atlandı (--skip-build)${NC}"
  if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK bulunamadı: $APK_PATH${NC}"
    exit 1
  fi
fi

# 4. APK boyutu
APK_SIZE=$(du -sh "$APK_PATH" | awk '{print $1}')
echo ""
echo -e "${BLUE}📦 APK kurulumu: $APK_SIZE${NC}"

# 5. Uygulama varsa kaldır (temiz kurulum için)
adb -s "$TARGET_DEVICE" shell pm list packages | grep -q "$APP_ID" && {
  echo "   Eski sürüm kaldırılıyor..."
  adb -s "$TARGET_DEVICE" uninstall "$APP_ID" > /dev/null 2>&1 || true
}

# 6. APK kur
adb -s "$TARGET_DEVICE" install -r "$APK_PATH"

# 7. Uygulamayı başlat
echo ""
echo -e "${BLUE}🚀 Uygulama başlatılıyor...${NC}"
adb -s "$TARGET_DEVICE" shell am start -n "$APP_ID/.MainActivity" > /dev/null 2>&1 || {
  adb -s "$TARGET_DEVICE" shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1
}

echo ""
echo -e "${GREEN}✅ Deploy tamamlandı!${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "   Smoke test için: bash scripts/smoke-test.sh"
