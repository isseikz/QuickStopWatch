#!/usr/bin/env python3
"""Harness PreToolUse hook (matcher: run_command).

Blocks commands that would leak release secrets or rewrite shared history, and
asks for confirmation before pushing directly to main. Anything else is passed
through unchanged (empty JSON object -> default permission flow).
"""

from __future__ import annotations

import json
import re
import sys

DENY = [
    (
        re.compile(r"\.jks\b|\.keystore\b|keystore\.properties|service[-_]?account[^\s]*\.json", re.I),
        "Release signing material / Play credentials must not be accessed by the agent. "
        "Ask the user to run scripts/setup-release-secrets.sh instead.",
    ),
    (
        re.compile(r"\bgit\s+push\b[^\n]*(\s--force(-with-lease)?\b|\s-f\b|\s\+\S)"),
        "Force push is forbidden (AGENTS.md section 4).",
    ),
    (
        re.compile(r"\brm\s+-[a-z]*r[a-z]*f?[a-z]*\s+(/|~|\$HOME|\.|\*)(\s|$)"),
        "Recursive deletion of the repository root, home or filesystem root is forbidden.",
    ),
    (
        re.compile(r"\bgit\s+(tag\s+-d|push\b[^\n]*--delete|push\b[^\n]*\s:refs/tags/)"),
        "Deleting release tags is forbidden; create a new patch version instead.",
    ),
]

ASK = [
    (
        re.compile(r"\bgit\s+push\b[^\n]*\b(origin\s+)?(HEAD:)?main\b"),
        "Direct push to main. Normal changes go through a pull request (AGENTS.md section 4).",
    ),
    (
        re.compile(r"\bgit\s+push\b[^\n]*\b(--tags|v\d+\.\d+\.\d+)\b"),
        "Pushing a release tag triggers a Google Play release. Confirm with the user.",
    ),
    (
        re.compile(r"\bgh\s+secret\s+(set|delete)\b"),
        "Changing GitHub secrets. Confirm with the user.",
    ),
]


PASS_THROUGH = {"decision": "ask"}  # default permission flow (respects "Always Allow")


def decide(payload: dict) -> dict:
    args = (payload.get("toolCall") or {}).get("args") or {}
    command = str(args.get("CommandLine", ""))

    for pattern, reason in DENY:
        if pattern.search(command):
            return {"decision": "deny", "reason": f"[harness/guard] {reason}"}
    for pattern, reason in ASK:
        if pattern.search(command):
            return {"decision": "force_ask", "reason": f"[harness/guard] {reason}"}
    return PASS_THROUGH


def main() -> None:
    # NOTE: the hook contract requires an explicit "decision". An empty "{}" is
    # treated as a deny without reason, which blocks every command.
    try:
        payload = json.load(sys.stdin)
    except json.JSONDecodeError:
        print(json.dumps(PASS_THROUGH))
        return
    try:
        result = decide(payload)
    except Exception as exc:  # noqa: BLE001 - surface hook bugs instead of silently blocking
        result = {"decision": "deny", "reason": f"[harness/guard] hook error: {exc!r}"}
    print(json.dumps(result))


if __name__ == "__main__":
    main()
