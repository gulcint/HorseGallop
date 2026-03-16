#!/usr/bin/env bash
# robotest.sh — Firebase Test Lab Test Runner
#
# Modlar:
#   --mode login    (varsayılan) Robo: checkbox → email giriş → login
#   --mode signup   Robo: login ekranı → hesap oluştur → kayıt formu → doğrulama bekleme
#   --mode espresso Instrumentation: AuthSmokeTest smoke test paketi
#
# Kullanım:
#   bash scripts/robotest.sh [--build] [--mode login|signup|espresso] [--timeout 5m] [--device Pixel8]
#   bash scripts/robotest.sh --build --mode signup   # Gerçek Firebase signup testi
#   bash scripts/robotest.sh --build --mode espresso # UI smoke testleri

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
TEST_APK_PATH="$PROJECT_DIR/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
FIREBASE_PROJECT="com-horse-gallop"

# Varsayılan parametreler
BUILD=false
TIMEOUT="5m"
DEVICE_MODEL="caiman"   # Pixel 9 Pro (physical) — API 34/35
DEVICE_API="35"
LOCALE="tr"
MODE="login"

# Argüman parse
while [[ $# -gt 0 ]]; do
  case $1 in
    --build)   BUILD=true; shift ;;
    --timeout) TIMEOUT="$2"; shift 2 ;;
    --device)  DEVICE_MODEL="$2"; shift 2 ;;
    --api)     DEVICE_API="$2"; shift 2 ;;
    --locale)  LOCALE="$2"; shift 2 ;;
    --mode)    MODE="$2"; shift 2 ;;
    *) echo "Bilinmeyen parametre: $1"; exit 1 ;;
  esac
done

echo "🤖 HorseGallop Firebase Test Lab"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎯 Mod: $MODE"

# gcloud kontrolü
if ! command -v gcloud &>/dev/null; then
  echo "❌ gcloud bulunamadı. Kur: brew install --cask google-cloud-sdk"
  exit 1
fi

# APK build (istenirse)
if [ "$BUILD" = true ]; then
  echo "📦 APK build ediliyor..."
  cd "$PROJECT_DIR"
  if [ "$MODE" = "espresso" ]; then
    ./gradlew assembleDebug assembleDebugAndroidTest
    echo "✅ App APK + Test APK hazır"
  else
    ./gradlew assembleDebug
    echo "✅ APK hazır"
  fi
fi

# APK varlık kontrolü
if [ ! -f "$APK_PATH" ]; then
  echo "❌ APK bulunamadı: $APK_PATH"
  echo "   Önce build et: bash scripts/robotest.sh --build --mode $MODE"
  exit 1
fi

if [ "$MODE" = "espresso" ] && [ ! -f "$TEST_APK_PATH" ]; then
  echo "❌ Test APK bulunamadı: $TEST_APK_PATH"
  echo "   Önce build et: bash scripts/robotest.sh --build --mode espresso"
  exit 1
fi

APK_SIZE=$(du -sh "$APK_PATH" | cut -f1)
echo "📱 APK: $APK_SIZE — $APK_PATH"
echo "🎯 Cihaz: $DEVICE_MODEL (API $DEVICE_API, $LOCALE)"
echo "⏱️  Timeout: $TIMEOUT"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🚀 Test Lab'a gönderiliyor..."

case "$MODE" in

  # ── Login akışı ─────────────────────────────────────────────────────────
  # text direktifleri: email + şifre form alanlarını doldur
  # click direktifi: tek click = login_button (Robo AI checkpoint'e kadar kendi geziyor)
  login)
    gcloud firebase test android run \
      --project "$FIREBASE_PROJECT" \
      --type robo \
      --app "$APK_PATH" \
      --device "model=$DEVICE_MODEL,version=$DEVICE_API,locale=$LOCALE,orientation=portrait" \
      --timeout "$TIMEOUT" \
      --robo-directives "text:email_input=test@horsegallop.com,text:password_input=Test1234!,click:login_button=" \
      2>&1
    ;;

  # ── Signup akışı (gerçek Firebase — mail gönderilir) ────────────────────
  # text direktifleri: enrollment form alanları
  # click direktifi: tek click = signup_button
  # Robo AI agreement_checkbox + email_login_button + create_account_link'i keşfeder
  # UYARI: Bu mod production Firebase'e gerçek kullanıcı kaydeder.
  # Kullanılan email: gulcint41@gmail.com — doğrulama maili bu adrese gider.
  signup)
    echo "⚠️  UYARI: Bu test gerçek Firebase'e bağlanır."
    echo "   gulcint41@gmail.com adresine doğrulama maili gönderilecek."
    echo "   Devam etmek için 5 saniye bekliyorum... (Ctrl+C ile iptal)"
    sleep 5
    gcloud firebase test android run \
      --project "$FIREBASE_PROJECT" \
      --type robo \
      --app "$APK_PATH" \
      --device "model=$DEVICE_MODEL,version=$DEVICE_API,locale=$LOCALE,orientation=portrait" \
      --timeout "$TIMEOUT" \
      --robo-directives "text:enrollment_first_name=Test,text:enrollment_last_name=Binici,text:enrollment_email=gulcint41@gmail.com,text:enrollment_password=HorseGallop2024!,click:signup_button=" \
      2>&1
    ;;

  # ── Espresso smoke testleri (fake state, Firebase bağlantısı yok) ───────
  espresso)
    TEST_APK_SIZE=$(du -sh "$TEST_APK_PATH" | cut -f1)
    echo "🧪 Test APK: $TEST_APK_SIZE — $TEST_APK_PATH"
    gcloud firebase test android run \
      --project "$FIREBASE_PROJECT" \
      --type instrumentation \
      --app "$APK_PATH" \
      --test "$TEST_APK_PATH" \
      --device "model=$DEVICE_MODEL,version=$DEVICE_API,locale=$LOCALE,orientation=portrait" \
      --timeout "$TIMEOUT" \
      --test-targets "class com.horsegallop.feature.auth.presentation.AuthSmokeTest" \
      2>&1
    ;;

  *)
    echo "❌ Bilinmeyen mod: $MODE"
    echo "   Geçerli modlar: login | signup | espresso"
    exit 1
    ;;
esac

echo ""
echo "📊 Sonuçlar: https://console.firebase.google.com/project/$FIREBASE_PROJECT/testlab"
