#!/usr/bin/env python3
import argparse
import json
import pathlib
import sys


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--task-id", required=True)
    parser.add_argument("--title", required=True)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    repo_root = pathlib.Path(__file__).resolve().parents[1]
    task_root = repo_root / ".claude" / "context" / "tasks" / args.task_id
    (task_root / "artifacts").mkdir(parents=True, exist_ok=True)
    (task_root / "handoffs").mkdir(parents=True, exist_ok=True)
    (task_root / "reports").mkdir(parents=True, exist_ok=True)

    brief_path = task_root / "brief.md"
    state_path = task_root / "state.json"
    decision_log_path = task_root / "decision-log.md"
    open_questions_path = task_root / "open-questions.md"
    artifact_index_path = task_root / "artifacts" / "index.md"

    if not brief_path.exists():
        brief_path.write_text(
            f"# {args.title}\n\n## Goal\n\n## Constraints\n\n## Relevant Paths\n\n## Acceptance Notes\n",
            encoding="utf-8",
        )
    if not state_path.exists():
        state_path.write_text(
            json.dumps(
                {
                    "taskId": args.task_id,
                    "title": args.title,
                    "status": "active",
                    "summary": "",
                    "owner": "conductor",
                    "currentStage": "planning",
                    "decisions": [],
                    "openQuestions": [],
                    "nextActions": [],
                    "handoffs": {},
                    "artifacts": {},
                    "reports": {},
                },
                indent=2,
            )
            + "\n",
            encoding="utf-8",
        )
    if not decision_log_path.exists():
        decision_log_path.write_text(
            "# Decision Log\n\n## Entries\n\n- [timestamp] Decision:\n  - Context:\n  - Reason:\n  - Impact:\n",
            encoding="utf-8",
        )
    if not open_questions_path.exists():
        open_questions_path.write_text(
            "# Open Questions\n\n## Questions\n\n- Question:\n  - Why it matters:\n  - Owner:\n  - Status:\n",
            encoding="utf-8",
        )
    if not artifact_index_path.exists():
        artifact_index_path.write_text(
            "# Artifact Index\n\n## Artifacts\n\n- `artifacts/researcher.md`:\n- `artifacts/android-feature.md`:\n- `artifacts/firebase-backend.md`:\n- `artifacts/ui-craft.md`:\n- `artifacts/operator.md`:\n- `reports/qa.md`:\n",
            encoding="utf-8",
        )

    print(task_root)
    return 0


if __name__ == "__main__":
    sys.exit(main())
