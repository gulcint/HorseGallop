#!/usr/bin/env python3
import sys

from common import audit, load_payload, notify


def main() -> int:
    payload = load_payload()
    title = payload.get("title", "Claude Code")
    message = payload.get("message", "Attention required")
    notification_type = payload.get("notification_type", "unknown")
    audit("notification", notification_type=notification_type, title=title, message=message)
    notify(title, message)
    return 0


if __name__ == "__main__":
    sys.exit(main())
