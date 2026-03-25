#!/usr/bin/env python3
"""
HorseGallop Inbox Watcher
Telefondan gelen brief'leri okur, task context oluşturur, pipeline'ı başlatır.

Kullanım:
  python3 scripts/inbox_watcher.py               # inbox.md'yi işle
  python3 scripts/inbox_watcher.py --dry-run     # Sadece göster, işleme
"""

import argparse
import json
import pathlib
import re
import subprocess
import sys
from datetime import datetime


REPO_ROOT = pathlib.Path(__file__).resolve().parents[1]
INBOX_PATH = REPO_ROOT / "inbox.md"
INBOX_ARCHIVE_DIR = REPO_ROOT / "docs" / "inbox-archive"
TASKS_DIR = REPO_ROOT / ".claude" / "context" / "tasks"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="HorseGallop inbox watcher")
    parser.add_argument("--dry-run", action="store_true", help="İşlem yapma, sadece göster")
    return parser.parse_args()


def read_inbox() -> str | None:
    """inbox.md'yi oku. Boşsa veya yoksa None döndür."""
    if not INBOX_PATH.exists():
        return None
    content = INBOX_PATH.read_text(encoding="utf-8").strip()
    # Sadece şablon satırlarından oluşuyorsa boş say
    if not content or content.startswith("<!-- Telefon"):
        return None
    # <!-- işlenmiş --> işareti varsa atla
    if "<!-- işlenmiş -->" in content:
        return None
    return content


def extract_brief_fields(content: str) -> dict:
    """
    inbox.md içeriğinden alanları çıkar.

    Beklenen format (basit):
    # T-XXXX: Başlık
    ## Goal
    ...
    ## Constraints (opsiyonel)
    ...
    ## Notes (opsiyonel)
    ...
    """
    lines = content.strip().splitlines()
    task_id = None
    title = None
    goal = []
    constraints = []
    notes = []
    current_section = None

    for line in lines:
        # Başlık satırı: # T-XXX: Başlık
        if line.startswith("# "):
            header = line[2:].strip()
            match = re.match(r"(T-[A-Z0-9]+):\s*(.+)", header)
            if match:
                task_id = match.group(1)
                title = match.group(2).strip()
            else:
                # Task ID yok, otomatik oluştur
                title = header
                ts = datetime.now().strftime("%m%d%H%M")
                task_id = f"T-{ts}"
            continue

        if line.startswith("## Goal"):
            current_section = "goal"
            continue
        elif line.startswith("## Constraints"):
            current_section = "constraints"
            continue
        elif line.startswith("## Notes"):
            current_section = "notes"
            continue
        elif line.startswith("##"):
            current_section = None
            continue

        if current_section == "goal":
            goal.append(line)
        elif current_section == "constraints":
            constraints.append(line)
        elif current_section == "notes":
            notes.append(line)

    # Task ID yoksa içerikten çıkaramadık — basit format
    if not task_id:
        ts = datetime.now().strftime("%m%d%H%M")
        task_id = f"T-{ts}"
        title = lines[0].lstrip("#").strip() if lines else "Untitled Task"

    if not goal:
        # Goal bölümü yoksa tüm içeriği goal say (en basit format)
        goal = [l for l in lines if not l.startswith("#")]

    return {
        "task_id": task_id,
        "title": title or "Untitled",
        "goal": "\n".join(goal).strip(),
        "constraints": "\n".join(constraints).strip(),
        "notes": "\n".join(notes).strip(),
    }


def create_task_context(fields: dict, dry_run: bool = False) -> pathlib.Path:
    """init_claude_task_context.py çalıştır ve brief.md'yi doldur."""
    task_id = fields["task_id"]
    title = fields["title"]

    if dry_run:
        print(f"[DRY RUN] Task oluşturulacak: {task_id} — {title}")
        print(f"[DRY RUN] Goal: {fields['goal'][:100]}...")
        return TASKS_DIR / task_id

    # Task context dizinlerini oluştur
    result = subprocess.run(
        ["python3", "scripts/init_claude_task_context.py",
         "--task-id", task_id, "--title", title],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        print(f"HATA: init_claude_task_context.py başarısız\n{result.stderr}")
        sys.exit(1)

    # brief.md'yi doldur
    task_dir = TASKS_DIR / task_id
    brief_path = task_dir / "brief.md"

    brief_content = f"""# {title}

## Goal

{fields['goal']}

## Constraints

{fields['constraints'] or '- Yok'}

## Relevant Paths

- (researcher agent belirleyecek)

## Acceptance Notes

{fields['notes'] or '- Telefon brief\'i — acceptance criteria researcher/tech-lead ile belirlenir'}

---
_Brief kaynağı: inbox.md — {datetime.now().strftime('%Y-%m-%d %H:%M')}_
"""
    brief_path.write_text(brief_content, encoding="utf-8")
    print(f"✅ Task oluşturuldu: {task_id} — {title}")
    print(f"   Brief: {brief_path}")
    return task_dir


def archive_inbox(content: str, task_id: str, dry_run: bool = False):
    """İşlenen inbox içeriğini arşivle, inbox'ı temizle."""
    if dry_run:
        print(f"[DRY RUN] Inbox arşivlenecek → docs/inbox-archive/{task_id}.md")
        return

    INBOX_ARCHIVE_DIR.mkdir(parents=True, exist_ok=True)
    archive_path = INBOX_ARCHIVE_DIR / f"{task_id}_{datetime.now().strftime('%Y%m%d_%H%M')}.md"
    archive_path.write_text(content, encoding="utf-8")

    # Inbox'ı şablona sıfırla
    INBOX_PATH.write_text(
        "<!-- Telefon brief'i buraya yaz. Kaydet. Pipeline otomatik başlar. -->\n\n"
        "# T-XXX: Görev Başlığı\n\n"
        "## Goal\n\nNe yapılacak?\n\n"
        "## Constraints\n\nKısıtlamalar varsa\n\n"
        "## Notes\n\nEk notlar\n",
        encoding="utf-8",
    )
    print(f"✅ Inbox arşivlendi: {archive_path.name}")
    print("✅ Inbox sıfırlandı")


def print_next_steps(task_id: str, title: str):
    """Kullanıcıya sonraki adımları göster."""
    print(f"""
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 Task hazır: {task_id} — {title}

Sonraki adım (Claude Code'da):
  claude  # Claude Code'u başlat
  > tech-lead agentını çalıştır: {task_id} brief'ini al ve pipeline başlat

Ya da doğrudan:
  > .claude/context/tasks/{task_id}/brief.md'yi oku ve implement et
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
""")


def main():
    args = parse_args()

    content = read_inbox()
    if not content:
        print("📭 Inbox boş veya zaten işlenmiş. Atlanıyor.")
        return 0

    print(f"📬 Inbox içeriği bulundu ({len(content)} karakter)")

    fields = extract_brief_fields(content)
    print(f"🔍 Task tespit edildi: {fields['task_id']} — {fields['title']}")

    task_dir = create_task_context(fields, dry_run=args.dry_run)
    archive_inbox(content, fields["task_id"], dry_run=args.dry_run)

    if not args.dry_run:
        print_next_steps(fields["task_id"], fields["title"])

    return 0


if __name__ == "__main__":
    sys.exit(main())
