#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BRANCH="$(git rev-parse --abbrev-ref HEAD)"

if [[ "$BRANCH" == "HEAD" || "$BRANCH" == "main" ]]; then
  echo "Bu script feature branch uzerinde calistirilmalidir." >&2
  exit 1
fi

bash scripts/run-android-gate.sh

if git rev-parse --abbrev-ref --symbolic-full-name '@{u}' >/dev/null 2>&1; then
  git push
else
  git push -u origin "$BRANCH"
fi

PR_NUMBER="$(gh pr view "$BRANCH" --json number --jq '.number' 2>/dev/null || true)"

if [[ -z "$PR_NUMBER" ]]; then
  LAST_COMMIT="$(git log -1 --format=%s)"
  gh pr create \
    --base main \
    --title "$LAST_COMMIT" \
    --body "$(cat <<'EOF'
## Ozet

Yerel zorunlu gate gecti:
- lintDebug
- testDebugUnitTest

CI basarili oldugunda auto-merge etkinlesecektir.
EOF
)"
  PR_NUMBER="$(gh pr view "$BRANCH" --json number --jq '.number')"
fi

gh pr merge "$PR_NUMBER" --auto --squash --delete-branch
echo "PR #$PR_NUMBER hazir. GitHub Actions basarili oldugunda otomatik merge edilecek."

echo ""
echo "Emülatöre deploy ediliyor..."
bash scripts/deploy-emulator.sh || echo "Deploy basarisiz — PR zaten acildi, devam et."

echo ""
echo "Smoke test calistiriliyor..."
bash scripts/smoke-test.sh || echo "Smoke test basarisiz — manuel kontrol et."
