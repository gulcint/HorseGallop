#!/usr/bin/env python3
import argparse
import pathlib
import re
import subprocess
import sys
import xml.etree.ElementTree as ET


STRING_CALL_PATTERNS = [
    re.compile(r"""\bText\s*\(\s*"[^"]*[A-Za-zÇĞİÖŞÜçğıöşü][^"]*"\s*[,)]"""),
    re.compile(r"""\b(label|placeholder|title|text)\s*=\s*\{\s*Text\s*\(\s*"[^"]*[A-Za-zÇĞİÖŞÜçğıöşü][^"]*"\s*[,)]"""),
]
ICON_BUTTON_PATTERN = re.compile(r"\bIconButton\s*\(")
FLOATING_ACTION_BUTTON_PATTERN = re.compile(r"\bFloatingActionButton\s*\(")
PREVIEW_PATTERN = re.compile(r"@Preview\b")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--project-dir", required=True)
    parser.add_argument("--path", action="append", dest="paths", default=[])
    return parser.parse_args()


def git_changed_paths(project_dir: pathlib.Path) -> list[pathlib.Path]:
    result = subprocess.run(
        ["git", "status", "--short"],
        cwd=project_dir,
        capture_output=True,
        text=True,
        check=False,
    )
    paths: list[pathlib.Path] = []
    for line in result.stdout.splitlines():
        if not line.strip():
            continue
        candidate = line[3:].strip()
        if " -> " in candidate:
            candidate = candidate.split(" -> ", 1)[1]
        paths.append(project_dir / candidate)
    return paths


def read_string_names(path: pathlib.Path) -> set[str]:
    tree = ET.parse(path)
    return {
        element.attrib["name"]
        for element in tree.getroot()
        if element.tag == "string" and "name" in element.attrib and element.attrib.get("translatable", "true") != "false"
    }


def window_has_content_description(lines: list[str], start: int, window: int = 20) -> bool:
    for line in lines[start : start + window]:
        if "contentDescription = stringResource(" in line or "contentDescription = stringResource(id =" in line:
            return True
    return False


def check_previews(path: pathlib.Path, lines: list[str], violations: list[str]) -> None:
    if path.name.endswith("Screen.kt") and "@Composable" in "\n".join(lines) and not PREVIEW_PATTERN.search("\n".join(lines)):
        violations.append(f"{path}: missing @Preview in screen file")


def check_hardcoded_strings(path: pathlib.Path, lines: list[str], violations: list[str]) -> None:
    for index, line in enumerate(lines, start=1):
        if "testTag" in line or "route =" in line:
            continue
        if any(pattern.search(line) for pattern in STRING_CALL_PATTERNS):
            violations.append(f"{path}:{index}: hardcoded UI string detected")


def check_content_descriptions(path: pathlib.Path, lines: list[str], violations: list[str]) -> None:
    for index, line in enumerate(lines):
        if ICON_BUTTON_PATTERN.search(line) or FLOATING_ACTION_BUTTON_PATTERN.search(line):
            if not window_has_content_description(lines, index):
                violations.append(f"{path}:{index + 1}: interactive icon control missing stringResource contentDescription")


def run_kotlin_checks(path: pathlib.Path, violations: list[str]) -> None:
    lines = path.read_text(encoding="utf-8").splitlines()
    check_previews(path, lines, violations)
    check_hardcoded_strings(path, lines, violations)
    check_content_descriptions(path, lines, violations)


def check_localized_resources(project_dir: pathlib.Path, violations: list[str]) -> None:
    default_strings = read_string_names(project_dir / "app/src/main/res/values/strings_core.xml")
    tr_strings = read_string_names(project_dir / "app/src/main/res/values-tr/strings.xml")
    en_strings = read_string_names(project_dir / "app/src/main/res/values-en/strings.xml")
    missing_tr = sorted(default_strings - tr_strings)
    missing_en = sorted(default_strings - en_strings)
    if missing_tr:
        preview = ", ".join(missing_tr[:10])
        violations.append(f"values-tr/strings.xml missing keys from strings_core.xml: {preview}")
    if missing_en:
        preview = ", ".join(missing_en[:10])
        violations.append(f"values-en/strings.xml missing keys from strings_core.xml: {preview}")


def main() -> int:
    args = parse_args()
    project_dir = pathlib.Path(args.project_dir).resolve()
    raw_paths = [pathlib.Path(path).resolve() for path in args.paths] if args.paths else git_changed_paths(project_dir)
    candidate_paths = [path for path in raw_paths if path.exists()]
    violations: list[str] = []

    run_resource_sync = False
    for path in candidate_paths:
        relative = path.relative_to(project_dir)
        if relative.suffix in {".kt", ".kts"} and "app/src/main/java/com/horsegallop" in str(relative):
            run_kotlin_checks(path, violations)
        if relative.name in {"strings_core.xml", "strings.xml"} and "res/values" in str(relative.parent):
            run_resource_sync = True

    if run_resource_sync:
        check_localized_resources(project_dir, violations)

    if violations:
        sys.stderr.write("Android quality convention violations found:\n")
        for violation in violations:
            sys.stderr.write(f"- {violation}\n")
        return 1

    sys.stdout.write("Android quality conventions passed.\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
