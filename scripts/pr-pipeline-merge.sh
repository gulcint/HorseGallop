#!/bin/bash
# pr-pipeline-merge.sh
# PostToolUse hook — git push sonrası otomatik çağrılır.
# 1. Unit testleri çalıştırır
# 2. Testler geçerse PR açar (yoksa) veya mevcut PR'ı raporlar
# 3. PR pipeline geçerse otomatik merge eder (background)

set -uo pipefail

# stdin'den JSON hook girdisini oku
INPUT=$(cat)
COMMAND=$(python3 -c "
import sys, json
try:
    data = json.loads(sys.stdin.read())
    print(data.get('tool_input', {}).get('command', ''))
except Exception:
    print('')
" <<< "$INPUT" 2>/dev/null || echo "")

# Sadece git push komutları için tetikle
if [[ "$COMMAND" != *"git push"* ]]; then
    exit 0
fi

# Mevcut branch'i al
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [[ -z "$BRANCH" || "$BRANCH" == "HEAD" || "$BRANCH" == "main" || "$BRANCH" == "develop" ]]; then
    exit 0
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🧪 Unit testler çalıştırılıyor..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Unit testleri çalıştır
if ! ./gradlew testDebugUnitTest --no-daemon -q 2>&1; then
    echo ""
    echo "❌ Unit testler başarısız — PR açılmadı."
    echo "   Hataları düzeltip tekrar push edin."
    exit 1
fi

echo "✅ Unit testler geçti!"
echo ""

# Açık PR var mı kontrol et
PR_NUMBER=$(gh pr view "$BRANCH" --json number --jq '.number' 2>/dev/null || echo "")

if [[ -n "$PR_NUMBER" ]]; then
    PR_URL=$(gh pr view "$BRANCH" --json url --jq '.url' 2>/dev/null || echo "")
    echo "🔗 Mevcut PR: #${PR_NUMBER} — ${PR_URL}"
else
    # PR yok — oluştur
    echo "📬 PR oluşturuluyor..."

    # Son commit mesajından başlık ve açıklama al
    LAST_COMMIT=$(git log -1 --format="%s" 2>/dev/null || echo "Yeni değişiklikler")
    COMMIT_COUNT=$(git rev-list --count HEAD ^main 2>/dev/null || echo "")

    PR_URL=$(gh pr create \
        --title "$LAST_COMMIT" \
        --body "$(cat <<'PREOF'
## Özet

Bu PR aşağıdaki değişiklikleri içermektedir.

## Test planı

- [x] Unit testler yerel ortamda geçti
- [ ] CI pipeline kontrol edildi

🤖 Generated with [Claude Code](https://claude.ai/claude-code)
PREOF
)" \
        --base main 2>/dev/null || echo "")

    if [[ -n "$PR_URL" ]]; then
        PR_NUMBER=$(gh pr view "$BRANCH" --json number --jq '.number' 2>/dev/null || echo "")
        echo "✅ PR oluşturuldu: ${PR_URL}"
    else
        echo "⚠️  PR oluşturulamadı. Manuel olarak açabilirsiniz."
        exit 0
    fi
fi

# PR numarası yoksa çık
[[ -z "$PR_NUMBER" ]] && exit 0

echo ""
echo "⏳ Pipeline izleniyor (PR #${PR_NUMBER})..."

# Önce auto-merge dene (branch protection varsa)
if gh pr merge "$PR_NUMBER" --squash --auto --delete-branch 2>/dev/null; then
    echo "✅ Auto-merge etkinleştirildi. Pipeline geçince otomatik merge edilecek."
    exit 0
fi

# Fallback: arka planda polling + merge
LOG_FILE="/tmp/horsegallop-pr-${PR_NUMBER}-merge.log"
(
    echo "[$(date '+%H:%M:%S')] PR #${PR_NUMBER} izleme başladı"
    MAX_ITERATIONS=20   # 20 x 30s = 10 dakika
    ITERATION=0
    while [ "$ITERATION" -lt "$MAX_ITERATIONS" ]; do
        sleep 30
        ITERATION=$((ITERATION + 1))

        STATUS_JSON=$(gh pr view "$PR_NUMBER" \
            --json statusCheckRollup \
            --jq '.statusCheckRollup // []' 2>/dev/null || echo "[]")

        RESULT=$(python3 - <<PYEOF 2>/dev/null || echo "pending"
import json, sys
checks = json.loads('''${STATUS_JSON}''')
if not checks:
    print("pending")
    sys.exit(0)
conclusions = [c.get("conclusion") or "" for c in checks]
statuses    = [c.get("status") or "" for c in checks]
if any(c in ("FAILURE","CANCELLED","TIMED_OUT") for c in conclusions):
    print("failure")
elif all(s == "COMPLETED" for s in statuses) and all(c == "SUCCESS" for c in conclusions):
    print("success")
else:
    print("pending")
PYEOF
        )

        echo "[$(date '+%H:%M:%S')] Kontrol #${ITERATION}: ${RESULT}"

        if [[ "$RESULT" == "success" ]]; then
            echo "[$(date '+%H:%M:%S')] ✅ Tüm check'ler geçti! Merge ediliyor..."
            gh pr merge "$PR_NUMBER" --squash --delete-branch 2>&1 && \
                echo "[$(date '+%H:%M:%S')] ✅ PR #${PR_NUMBER} merge edildi." || \
                echo "[$(date '+%H:%M:%S')] ❌ Merge başarısız."
            exit 0
        elif [[ "$RESULT" == "failure" ]]; then
            echo "[$(date '+%H:%M:%S')] ❌ CI başarısız. Merge iptal."
            exit 1
        fi
    done
    echo "[$(date '+%H:%M:%S')] ⏰ 10 dakika timeout."
) > "$LOG_FILE" 2>&1 &

echo "   Arka planda izleniyor → tail -f $LOG_FILE"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
exit 0
