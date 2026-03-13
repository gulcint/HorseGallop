#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
HOOKS_DIR="$ROOT_DIR/.githooks"

mkdir -p "$HOOKS_DIR"
chmod +x "$HOOKS_DIR"/* 2>/dev/null || true

git -C "$ROOT_DIR" config core.hooksPath .githooks

echo "Git hooks kuruldu."
echo "hooksPath => .githooks"
echo "Aktif hooklar:"
find "$HOOKS_DIR" -maxdepth 1 -type f -perm +111 -print | sed "s#^$ROOT_DIR/##"
