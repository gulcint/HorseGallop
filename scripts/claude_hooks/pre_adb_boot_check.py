#!/usr/bin/env python3
"""
PreToolUse hook: adb install komutundan önce emülatörün hazır olup olmadığını kontrol eder.
Hazır değilse boot tamamlanana kadar bekler (max 120s).
"""
import json
import subprocess
import sys
import time

ADB = "/Users/gulcintas/Library/Android/sdk/platform-tools/adb"
MAX_WAIT_SEC = 120
POLL_INTERVAL = 3


def is_device_ready() -> bool:
    try:
        result = subprocess.run([ADB, "devices"], capture_output=True, text=True, timeout=10)
        lines = result.stdout.strip().splitlines()
        # "emulator-5554\tdevice" gibi bir satır olmalı, "offline" olmamalı
        for line in lines[1:]:
            if "\tdevice" in line and "offline" not in line:
                return True
    except Exception:
        pass
    return False


def is_boot_completed() -> bool:
    try:
        result = subprocess.run(
            [ADB, "shell", "getprop", "sys.boot_completed"],
            capture_output=True,
            text=True,
            timeout=10,
        )
        return result.stdout.strip() == "1"
    except Exception:
        return False


def main():
    try:
        data = json.load(sys.stdin)
    except Exception:
        sys.exit(0)

    command = data.get("tool_input", {}).get("command", "")

    if "adb install" not in command:
        sys.exit(0)

    # adb install komutu tespit edildi — emülatör hazır mı kontrol et
    if is_device_ready() and is_boot_completed():
        sys.exit(0)

    print(json.dumps({"systemMessage": "Emülatör henüz hazır değil, boot bekleniyor..."}), flush=True)

    elapsed = 0
    while elapsed < MAX_WAIT_SEC:
        time.sleep(POLL_INTERVAL)
        elapsed += POLL_INTERVAL
        if is_device_ready() and is_boot_completed():
            print(json.dumps({"systemMessage": f"Emülatör hazır! ({elapsed}s)"}), flush=True)
            sys.exit(0)

    # Timeout — kullanıcıyı uyar ama bloklama
    print(
        json.dumps(
            {
                "systemMessage": f"Uyarı: {MAX_WAIT_SEC}s içinde emülatör hazır olmadı. Install devam edecek."
            }
        ),
        flush=True,
    )
    sys.exit(0)


if __name__ == "__main__":
    main()
