#!/usr/bin/env python3
import json
import os
import pathlib
import subprocess
import sys
import time
from typing import Any


REPO_ROOT = pathlib.Path(__file__).resolve().parents[2]
CLAUDE_DIR = REPO_ROOT / ".claude"
LOG_DIR = CLAUDE_DIR / "logs"
STATE_DIR = CLAUDE_DIR / "state"
HOOK_LOG = LOG_DIR / "hooks.jsonl"
HOOK_STATE = STATE_DIR / "hook_state.json"


def ensure_runtime_dirs() -> None:
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    STATE_DIR.mkdir(parents=True, exist_ok=True)


def load_payload() -> dict[str, Any]:
    raw = sys.stdin.read().strip()
    if not raw:
        return {}
    return json.loads(raw)


def emit_json(payload: dict[str, Any]) -> None:
    sys.stdout.write(json.dumps(payload))


def audit(event: str, **fields: Any) -> None:
    ensure_runtime_dirs()
    record = {
        "ts": int(time.time()),
        "event": event,
        **fields,
    }
    with HOOK_LOG.open("a", encoding="utf-8") as handle:
        handle.write(json.dumps(record, ensure_ascii=True) + "\n")


def load_state() -> dict[str, Any]:
    ensure_runtime_dirs()
    if not HOOK_STATE.exists():
        return {}
    try:
        return json.loads(HOOK_STATE.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}


def save_state(state: dict[str, Any]) -> None:
    ensure_runtime_dirs()
    HOOK_STATE.write_text(json.dumps(state, indent=2, ensure_ascii=True), encoding="utf-8")


def should_debounce(key: str, interval_seconds: int) -> bool:
    state = load_state()
    now = time.time()
    last_run = float(state.get("debounce", {}).get(key, 0))
    if now - last_run < interval_seconds:
        return True
    state.setdefault("debounce", {})[key] = now
    save_state(state)
    return False


def relpath(path: str | pathlib.Path | None) -> str:
    if not path:
        return ""
    candidate = pathlib.Path(path)
    try:
        return str(candidate.resolve().relative_to(REPO_ROOT))
    except Exception:
        return str(candidate)


def run_command(command: list[str], cwd: pathlib.Path | None = None, timeout: int = 600) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        command,
        cwd=str(cwd or REPO_ROOT),
        capture_output=True,
        text=True,
        timeout=timeout,
        check=False,
    )


def notify(title: str, message: str) -> None:
    sanitized_title = title.replace('"', "'")
    sanitized_message = message.replace('"', "'")
    if sys.platform == "darwin":
        subprocess.run(
            [
                "osascript",
                "-e",
                f'display notification "{sanitized_message}" with title "{sanitized_title}"',
            ],
            capture_output=True,
            text=True,
            check=False,
        )


def play_sound() -> None:
    if sys.platform == "darwin":
        sound_candidates = [
            "/System/Library/Sounds/Glass.aiff",
            "/System/Library/Sounds/Funk.aiff",
        ]
        for candidate in sound_candidates:
            if os.path.exists(candidate):
                subprocess.Popen(
                    ["afplay", candidate],
                    stdout=subprocess.DEVNULL,
                    stderr=subprocess.DEVNULL,
                )
                return
    sys.stdout.write("\a")
