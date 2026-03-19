#!/bin/bash
# smoke-test.sh — Post-deploy temel sağlık kontrolü
# Emülatörde çalışan uygulamayı test eder (crash, ANR, temel ekranlar)
# Usage: bash scripts/smoke-test.sh [device-id]

set -e

APP_ID="com.horsegallop"
MONKEY_EVENTS=500          # rastgele UI eventi sayısı
MONKEY_THROTTLE=100        # event'ler arası ms
MONKEY_SEED=12345          # tekrarlanabilir test için sabit seed
LOG_DIR="build/smoke-test-logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="$LOG_DIR/smoke_$TIMESTAMP.log"

# Renk kodları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🧪 HorseGallop — Smoke Test${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mkdir -p "$LOG_DIR"

# 1. ADB + cihaz kontrol
if ! command -v adb &> /dev/null; then
  echo -e "${RED}❌ adb bulunamadı.${NC}"; exit 1
fi

if [ -n "$1" ]; then
  TARGET_DEVICE="$1"
else
  TARGET_DEVICE=$(adb devices | grep -v "List of devices" | grep "device$" | head -1 | awk '{print $1}')
fi

if [ -z "$TARGET_DEVICE" ]; then
  echo -e "${RED}❌ Bağlı cihaz bulunamadı.${NC}"; exit 1
fi

echo "🎯 Cihaz: $TARGET_DEVICE"

# 2. Uygulama yüklü mü?
if ! adb -s "$TARGET_DEVICE" shell pm list packages | grep -q "$APP_ID"; then
  echo -e "${RED}❌ $APP_ID cihazda yüklü değil.${NC}"
  echo "   Önce: bash scripts/deploy-emulator.sh"
  exit 1
fi

# 3. Uygulamayı başlat ve başlangıç kontrolü
echo ""
echo -e "${BLUE}1/4 — Uygulama başlatma testi${NC}"
adb -s "$TARGET_DEVICE" shell am force-stop "$APP_ID" 2>/dev/null || true
sleep 1
adb -s "$TARGET_DEVICE" shell am start -n "$APP_ID/.MainActivity" > /dev/null 2>&1
sleep 3

# Uygulama hala çalışıyor mu?
RUNNING=$(adb -s "$TARGET_DEVICE" shell pidof "$APP_ID" 2>/dev/null || echo "")
if [ -z "$RUNNING" ]; then
  echo -e "${RED}❌ FAIL: Uygulama başlatılamadı veya hemen kapandı (crash).${NC}"
  exit 1
fi
echo -e "${GREEN}   ✅ Uygulama başladı (PID: $RUNNING)${NC}"

# 4. Logcat başlat (arka planda)
echo ""
echo -e "${BLUE}2/4 — Logcat izleme başlatılıyor${NC}"
adb -s "$TARGET_DEVICE" logcat -c  # logcat buffer temizle
adb -s "$TARGET_DEVICE" logcat "$APP_ID:E" AndroidRuntime:E *:S > "$LOG_FILE" 2>&1 &
LOGCAT_PID=$!

# 5. Monkey test — rastgele UI eventi
echo ""
echo -e "${BLUE}3/4 — Monkey test ($MONKEY_EVENTS event)${NC}"
MONKEY_OUTPUT=$(adb -s "$TARGET_DEVICE" shell monkey \
  -p "$APP_ID" \
  --seed "$MONKEY_SEED" \
  --throttle "$MONKEY_THROTTLE" \
  --ignore-crashes \
  --ignore-timeouts \
  --ignore-native-crashes \
  -v \
  "$MONKEY_EVENTS" 2>&1)

# Crash kontrol
CRASH_COUNT=$(echo "$MONKEY_OUTPUT" | grep -c "// CRASH" || echo "0")
ANR_COUNT=$(echo "$MONKEY_OUTPUT" | grep -c "// ANR" || echo "0")
EVENTS_DONE=$(echo "$MONKEY_OUTPUT" | grep "Events injected" | awk '{print $NF}' || echo "0")

echo "   Events: $EVENTS_DONE / $MONKEY_EVENTS"
echo "   Crash: $CRASH_COUNT | ANR: $ANR_COUNT"

# 6. Logcat durdur ve analiz et
kill $LOGCAT_PID 2>/dev/null || true
sleep 1

EXCEPTION_COUNT=$(grep -c "FATAL EXCEPTION\|E AndroidRuntime" "$LOG_FILE" 2>/dev/null || echo "0")

# 7. Sonuç
echo ""
echo -e "${BLUE}4/4 — Sonuç${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

PASS=true

if [ "$CRASH_COUNT" -gt 0 ]; then
  echo -e "${RED}   ❌ $CRASH_COUNT crash tespit edildi${NC}"
  PASS=false
fi

if [ "$ANR_COUNT" -gt 0 ]; then
  echo -e "${YELLOW}   ⚠️  $ANR_COUNT ANR tespit edildi${NC}"
  PASS=false
fi

if [ "$EXCEPTION_COUNT" -gt 0 ]; then
  echo -e "${RED}   ❌ $EXCEPTION_COUNT exception logcatta${NC}"
  PASS=false
fi

if [ "$PASS" = true ]; then
  echo -e "${GREEN}   ✅ SMOKE TEST PASS${NC}"
  echo "   Log: $LOG_FILE"
  echo ""
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  exit 0
else
  echo ""
  echo -e "${RED}   ❌ SMOKE TEST FAIL${NC}"
  echo "   Detaylı log: $LOG_FILE"
  echo "   Son exception'lar:"
  grep -A 5 "FATAL EXCEPTION\|E AndroidRuntime" "$LOG_FILE" 2>/dev/null | head -30 || true
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  exit 1
fi
