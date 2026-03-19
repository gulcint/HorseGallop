#!/bin/bash
# retrospective.sh — PR sonrası otomatik retrospektif notu oluştur
# Usage: bash scripts/retrospective.sh [pr-number]
# Örnek: bash scripts/retrospective.sh 92

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RETRO_DIR="$PROJECT_ROOT/docs/retrospectives"
DATE=$(date +"%Y-%m-%d")
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Renk kodları
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}📝 HorseGallop — Retrospektif Notu${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mkdir -p "$RETRO_DIR"

cd "$PROJECT_ROOT"

# PR numarası
PR_NUMBER="${1:-}"

# Git bilgileri
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
LAST_COMMIT=$(git log -1 --pretty=format:"%s" 2>/dev/null || echo "")
AUTHOR=$(git log -1 --pretty=format:"%an" 2>/dev/null || echo "")
COMMIT_COUNT=$(git log --oneline origin/main..HEAD 2>/dev/null | wc -l | tr -d ' ' || echo "0")
CHANGED_FILES=$(git diff --name-only origin/main...HEAD 2>/dev/null | wc -l | tr -d ' ' || echo "0")
CHANGED_LIST=$(git diff --name-only origin/main...HEAD 2>/dev/null | head -20 || echo "")

# PR bilgisi (gh CLI varsa)
PR_INFO=""
PR_TITLE=""
if command -v gh &> /dev/null && [ -n "$PR_NUMBER" ]; then
  PR_TITLE=$(gh pr view "$PR_NUMBER" --json title -q .title 2>/dev/null || echo "")
  PR_BODY=$(gh pr view "$PR_NUMBER" --json body -q .body 2>/dev/null || echo "")
  PR_INFO="PR #$PR_NUMBER: $PR_TITLE"
elif [ -n "$PR_NUMBER" ]; then
  PR_INFO="PR #$PR_NUMBER"
else
  PR_INFO="Branch: $BRANCH"
fi

# Dosya adı
SAFE_BRANCH=$(echo "$BRANCH" | sed 's/[^a-zA-Z0-9-]/-/g' | cut -c1-40)
RETRO_FILE="$RETRO_DIR/${DATE}-${SAFE_BRANCH}.md"

# Yazılım kalite metrikleri
TEST_COUNT=$(find "$PROJECT_ROOT/app/src/test" -name "*Test.kt" 2>/dev/null | wc -l | tr -d ' ' || echo "0")
VIEWMODEL_COUNT=$(find "$PROJECT_ROOT/app/src/main" -name "*ViewModel.kt" 2>/dev/null | wc -l | tr -d ' ' || echo "0")
SCREEN_COUNT=$(find "$PROJECT_ROOT/app/src/main" -name "*Screen.kt" 2>/dev/null | wc -l | tr -d ' ' || echo "0")

# TODO/FIXME sayısı
TODO_COUNT=$(grep -r "TODO\|FIXME" "$PROJECT_ROOT/app/src/main" --include="*.kt" 2>/dev/null | wc -l | tr -d ' ' || echo "0")

# SemanticColors ihlali kontrol
COLOR_VIOLATIONS=$(grep -r "Color\.White\|Color\.Black\|Color\.Red\|Color\.Gray\|Color(0x" \
  "$PROJECT_ROOT/app/src/main/java/com/horsegallop/feature" \
  "$PROJECT_ROOT/app/src/main/java/com/horsegallop/core" \
  --include="*.kt" 2>/dev/null | wc -l | tr -d ' ' || echo "0")

cat > "$RETRO_FILE" << EOF
# Retrospektif — $DATE

**$PR_INFO**
**Branch:** \`$BRANCH\`
**Yazar:** $AUTHOR
**Commit sayısı:** $COMMIT_COUNT
**Değişen dosya:** $CHANGED_FILES

---

## 📊 Kod Metrikleri

| Metrik | Değer |
|--------|-------|
| Unit test dosyası | $TEST_COUNT |
| ViewModel sayısı | $VIEWMODEL_COUNT |
| Screen sayısı | $SCREEN_COUNT |
| TODO/FIXME | $TODO_COUNT |
| SemanticColors ihlali | $COLOR_VIOLATIONS |

---

## ✅ Bu PR'da Yapılanlar

<!-- Otomatik: son commit'ler -->
$(git log --oneline origin/main..HEAD 2>/dev/null | head -15 | sed 's/^/- /' || echo "- (git log alınamadı)")

---

## 📂 Değişen Dosyalar

\`\`\`
$CHANGED_LIST
\`\`\`

---

## 🔍 İyileştirme Önerileri

> Bu bölümü review sonrası doldur — ekip bir sonraki iterasyonda neyi daha iyi yapabilir?

### Teknik Borç
- [ ] <!-- Tespit edilen teknik borç -->

### Eksik Testler
- [ ] <!-- Test yazılması gereken ViewModel/useCase -->

### UX İyileştirme
- [ ] <!-- Kullanıcı deneyiminde gözlemlenen sorunlar -->

### Performans
- [ ] <!-- Recomposition, network, memory sorunları -->

---

## 📋 Bir Sonraki İterasyon

> Bir sonraki sprint'e taşınan maddeler:

- [ ] <!-- -->

---

## 🚀 Production Checklist

- [ ] Build SUCCESSFUL (assembleDebug + assembleRelease)
- [ ] Unit testler geçiyor (testDebugUnitTest)
- [ ] Lint hatasız (lintDebug)
- [ ] SemanticColors ihlali yok
- [ ] @Preview tüm ekranlarda mevcut
- [ ] strings.xml TR + EN + default tamam
- [ ] Smoke test PASS (scripts/smoke-test.sh)

---

*Oluşturulma: $TIMESTAMP | scripts/retrospective.sh*
EOF

echo ""
echo -e "${GREEN}✅ Retrospektif notu oluşturuldu:${NC}"
echo "   $RETRO_FILE"
echo ""
echo -e "${YELLOW}📌 Yapman gerekenler:${NC}"
echo "   1. Dosyayı aç ve 'İyileştirme Önerileri' bölümünü doldur"
echo "   2. Production Checklist'i kontrol et"
echo "   3. Commit et: git add docs/retrospectives/ && git commit -m 'docs: retrospektif notu $DATE'"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Dosyayı terminalde göster
if command -v cat &> /dev/null; then
  echo ""
  cat "$RETRO_FILE"
fi
