#!/usr/bin/env python3
"""Harness Stop hook: do not let the agent finish with unverified changes.

If build-relevant files changed after the last successful `./scripts/verify.sh`
(marker: .harness/last-verify-ok), the agent is sent back into the loop with an
instruction to run the verifier. To avoid infinite loops it re-prompts at most
MAX_RETRIES times per conversation.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MARKER = ROOT / ".harness" / "last-verify-ok"
STATE_DIR = ROOT / ".harness" / "stop-gate"
MAX_RETRIES = 3

WATCHED = [
    ROOT / "app" / "src",
    ROOT / "app" / "build.gradle.kts",
    ROOT / "build.gradle.kts",
    ROOT / "settings.gradle.kts",
    ROOT / "gradle" / "libs.versions.toml",
]
IGNORED_PARTS = {"build", ".gradle"}


def latest_mtime() -> float:
    latest = 0.0
    for path in WATCHED:
        if path.is_file():
            latest = max(latest, path.stat().st_mtime)
        elif path.is_dir():
            for p in path.rglob("*"):
                if p.is_file() and not IGNORED_PARTS.intersection(p.parts):
                    latest = max(latest, p.stat().st_mtime)
    return latest


def allow() -> None:
    print(json.dumps({"decision": "allow"}))


def main() -> None:
    try:
        payload = json.load(sys.stdin)
    except json.JSONDecodeError:
        payload = {}

    if payload.get("terminationReason", "model_stop") != "model_stop":
        return allow()

    marker_time = MARKER.stat().st_mtime if MARKER.exists() else 0.0
    if latest_mtime() <= marker_time:
        return allow()

    STATE_DIR.mkdir(parents=True, exist_ok=True)
    counter = STATE_DIR / f"{payload.get('conversationId', 'unknown')}.count"
    count = int(counter.read_text()) if counter.exists() else 0
    if count >= MAX_RETRIES:
        return allow()
    counter.write_text(str(count + 1))

    print(
        json.dumps(
            {
                "decision": "continue",
                "reason": (
                    "[harness/stop-gate] Source files changed after the last successful verification. "
                    "Run `./scripts/verify.sh` and fix any failure before finishing. If the change is "
                    "intentionally unverifiable right now, say so explicitly to the user and update "
                    f"docs/PROGRESS.md. (retry {count + 1}/{MAX_RETRIES})"
                ),
            }
        )
    )


if __name__ == "__main__":
    main()
