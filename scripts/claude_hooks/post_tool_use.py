#!/usr/bin/env python3
import pathlib
import sys

from common import REPO_ROOT, audit, emit_json, load_payload, relpath, run_command, should_debounce


FILE_EDIT_TOOLS = {"Edit", "Write", "MultiEdit"}
COMPILE_DEBOUNCE_SECONDS = 45
TEST_DEBOUNCE_SECONDS = 60
LINT_DEBOUNCE_SECONDS = 90
QUALITY_DEBOUNCE_SECONDS = 60


def extract_path(payload: dict) -> pathlib.Path | None:
    tool_input = payload.get("tool_input", {})
    tool_response = payload.get("tool_response", {})
    candidate = tool_input.get("file_path") or tool_response.get("filePath")
    if not candidate:
        return None
    return pathlib.Path(candidate)


def classify(path: pathlib.Path) -> dict[str, bool]:
    path_text = relpath(path)
    is_kotlin = path.suffix in {".kt", ".kts"}
    is_manifest = path.name == "AndroidManifest.xml"
    is_gradle = path.name in {"build.gradle", "build.gradle.kts", "settings.gradle", "settings.gradle.kts", "gradle.properties"}
    is_resource = "src/main/res/" in path_text or path.suffix == ".xml"
    is_ui = any(segment in path_text for segment in ["feature/", "core/", "navigation/", "MainActivity.kt"])
    is_prod_code = is_kotlin and "/src/main/" in path_text
    return {
        "compile": is_kotlin,
        "test": is_prod_code,
        "lint": is_prod_code or is_manifest or is_gradle or is_resource,
        "quality": is_ui or is_manifest or is_gradle or is_resource,
    }


def run_gradle_task(task: str, file_path: pathlib.Path) -> tuple[bool, str]:
    result = run_command(["./gradlew", task, "--no-daemon", "--console=plain"], cwd=REPO_ROOT, timeout=1200)
    tail = "\n".join((result.stdout + "\n" + result.stderr).strip().splitlines()[-30:])
    audit(
        "post_tool_use_task",
        task=task,
        file_path=relpath(file_path),
        exit_code=result.returncode,
        output_tail=tail,
    )
    ok = result.returncode == 0
    icon = "PASS" if ok else "FAIL"
    return ok, f"{icon} {task} after {relpath(file_path)}\n{tail}".strip()


def run_quality_check(file_path: pathlib.Path) -> tuple[bool, str]:
    result = run_command(
        [
            "python3",
            str(REPO_ROOT / "scripts" / "android_quality_checks.py"),
            "--project-dir",
            str(REPO_ROOT),
            "--path",
            str(file_path),
        ],
        cwd=REPO_ROOT,
        timeout=300,
    )
    tail = "\n".join((result.stdout + "\n" + result.stderr).strip().splitlines()[-30:])
    audit(
        "post_tool_use_task",
        task="android_quality_checks",
        file_path=relpath(file_path),
        exit_code=result.returncode,
        output_tail=tail,
    )
    ok = result.returncode == 0
    icon = "PASS" if ok else "FAIL"
    return ok, f"{icon} android quality checks after {relpath(file_path)}\n{tail}".strip()


def main() -> int:
    payload = load_payload()
    if payload.get("tool_name") not in FILE_EDIT_TOOLS:
        return 0

    file_path = extract_path(payload)
    if not file_path or not file_path.exists():
        return 0

    checks = classify(file_path)
    messages: list[str] = []

    if checks["compile"] and not should_debounce("compileDebugKotlin", COMPILE_DEBOUNCE_SECONDS):
        _, message = run_gradle_task("compileDebugKotlin", file_path)
        messages.append(message)
    if checks["test"] and not should_debounce("testDebugUnitTest", TEST_DEBOUNCE_SECONDS):
        _, message = run_gradle_task("testDebugUnitTest", file_path)
        messages.append(message)
    if checks["lint"] and not should_debounce("lintDebug", LINT_DEBOUNCE_SECONDS):
        _, message = run_gradle_task("lintDebug", file_path)
        messages.append(message)
    if checks["quality"] and not should_debounce("androidQualityConventions", QUALITY_DEBOUNCE_SECONDS):
        _, message = run_quality_check(file_path)
        messages.append(message)

    if messages:
        emit_json(
            {
                "hookSpecificOutput": {
                    "hookEventName": "PostToolUse",
                    "additionalContext": "\n\n".join(messages),
                }
            }
        )
    return 0


if __name__ == "__main__":
    sys.exit(main())
