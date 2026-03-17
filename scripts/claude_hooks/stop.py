#!/usr/bin/env python3
import sys

from common import audit, load_payload, notify, play_sound


def summarize(text: str, limit: int = 180) -> str:
    compact = " ".join(text.split())
    if len(compact) <= limit:
        return compact
    return compact[: limit - 3] + "..."


def main() -> int:
    payload = load_payload()
    if payload.get("stop_hook_active"):
        return 0
    message = summarize(payload.get("last_assistant_message", "Claude stopped."))
    audit("stop", message=message)
    notify("Claude task finished", message or "Claude is waiting for you.")
    play_sound()
    return 0


if __name__ == "__main__":
    sys.exit(main())
