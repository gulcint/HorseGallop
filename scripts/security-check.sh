#!/bin/bash
# security-check.sh
# PreToolUse[Bash] hook — tehlikeli Bash komutlarını engeller.

INPUT=$(cat)
COMMAND=$(python3 -c "
import sys, json
try:
    data = json.loads(sys.stdin.read())
    print(data.get('tool_input', {}).get('command', ''))
except Exception:
    print('')
" <<< "$INPUT" 2>/dev/null || echo "")

# ── Tehlikeli pattern kontrolü ───────────────────────────────────────────────
check_danger() {
    local cmd="$1"

    # Force push to main/master engelle
    if echo "$cmd" | grep -qE "git push.*--force.*origin.*(main|master)|git push.*-f.*origin.*(main|master)"; then
        echo "⛔ GÜVENLİK: main/master'a force push engellendi."
        exit 2
    fi

    # rm -rf root veya home engelle
    if echo "$cmd" | grep -qE "rm -rf /|rm -rf ~|rm -rf \$HOME"; then
        echo "⛔ GÜVENLİK: Tehlikeli rm -rf komutu engellendi."
        exit 2
    fi

    # Kritik sistem dosyalarına yazma engelle
    if echo "$cmd" | grep -qE ">(.*)(\/etc\/|\/usr\/|\/bin\/|\/sbin\/)"; then
        echo "⛔ GÜVENLİK: Sistem dizinine yazma engellendi."
        exit 2
    fi

    # google-services.json veya .env dosyasını git add ile stagele engelle
    if echo "$cmd" | grep -qE "git add.*(google-services\.json|\.env|credentials\.json|keystore\.jks)"; then
        echo "⚠️  UYARI: Hassas dosya git stage'e ekleniyor. Devam etmek güvenli değil."
        exit 2
    fi
}

[[ -n "$COMMAND" ]] && check_danger "$COMMAND"
exit 0
