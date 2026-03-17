#!/usr/bin/env python3
import sys

from common import audit, emit_json, load_payload


SUBAGENT_CONTEXT = """You are operating under the repo's canonical handoff contract.

Before doing work:
- Read `.claude/context/shared/agent-contracts.md`
- Read the exact task-specific context paths given in the dispatch message
- Treat those file paths as the primary source of truth over chat history

While working:
- Only write outputs to the artifact or report path assigned in the dispatch
- Do not edit another agent's handoff file directly
- If a required context file is missing, report that explicitly instead of inventing state
"""


def main() -> int:
    payload = load_payload()
    audit("subagent_start", agent_type=payload.get("agent_type", ""), agent_id=payload.get("agent_id", ""))
    emit_json(
        {
            "hookSpecificOutput": {
                "hookEventName": "SubagentStart",
                "additionalContext": SUBAGENT_CONTEXT.strip(),
            }
        }
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
