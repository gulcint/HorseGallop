#!/usr/bin/env python3
import pathlib
import re
import sys

from common import audit, emit_json, load_payload, relpath


DANGEROUS_BASH_RULES = [
    ("deny", re.compile(r"\brm\s+-rf\b"), "rm -rf blocked by project hook"),
    ("deny", re.compile(r"\bgit\s+reset\s+--hard\b"), "git reset --hard blocked by project hook"),
    ("deny", re.compile(r"\bgit\s+checkout\s+--\b"), "git checkout -- blocked by project hook"),
    ("deny", re.compile(r"\bgit\s+clean\s+-fd\b"), "git clean -fd blocked by project hook"),
    ("ask", re.compile(r"(^|\s)sudo\s"), "sudo requires explicit approval"),
    ("ask", re.compile(r"curl\b[^|>]*\|\s*(sh|bash)\b"), "Remote shell execution requires approval"),
    ("ask", re.compile(r"wget\b[^|>]*\|\s*(sh|bash)\b"), "Remote shell execution requires approval"),
    ("ask", re.compile(r"\badb\s+uninstall\b"), "adb uninstall requires approval"),
    ("ask", re.compile(r"\badb\s+shell\s+pm\s+clear\b"), "adb app data reset requires approval"),
    ("ask", re.compile(r"\./gradlew\s+(assembleRelease|bundleRelease)\b"), "Release build commands require approval"),
]

SENSITIVE_EDIT_PATTERNS = [
    "AndroidManifest.xml",
    "build.gradle",
    "build.gradle.kts",
    "settings.gradle",
    "settings.gradle.kts",
    "gradle.properties",
    "google-services.json",
    ".jks",
    ".keystore",
]


def bash_decision(command: str) -> dict | None:
    for behavior, pattern, reason in DANGEROUS_BASH_RULES:
        if pattern.search(command):
            audit("pre_tool_use", tool_name="Bash", behavior=behavior, reason=reason, command=command)
            return {
                "hookSpecificOutput": {
                    "hookEventName": "PreToolUse",
                    "permissionDecision": behavior,
                    "permissionDecisionReason": reason,
                }
            }
    return None


def edit_decision(file_path: str) -> dict | None:
    candidate = pathlib.Path(file_path)
    filename = candidate.name
    matched = filename in SENSITIVE_EDIT_PATTERNS or any(str(candidate).endswith(suffix) for suffix in SENSITIVE_EDIT_PATTERNS)
    if not matched:
        return None
    reason = f"Sensitive Android project file edit requires approval: {relpath(file_path)}"
    audit("pre_tool_use", tool_name="EditLike", behavior="ask", reason=reason, file_path=relpath(file_path))
    return {
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "ask",
            "permissionDecisionReason": reason,
        }
    }


def main() -> int:
    payload = load_payload()
    tool_name = payload.get("tool_name", "")
    tool_input = payload.get("tool_input", {})

    decision = None
    if tool_name == "Bash":
        decision = bash_decision(tool_input.get("command", ""))
    elif tool_name in {"Edit", "Write", "MultiEdit"}:
        decision = edit_decision(tool_input.get("file_path", ""))

    if decision:
        emit_json(decision)
    return 0


if __name__ == "__main__":
    sys.exit(main())
