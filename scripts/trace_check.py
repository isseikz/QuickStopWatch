#!/usr/bin/env python3
"""Harness: traceability check (REQ/NFR -> TC -> test code).

Rules (see docs/AGENTS.md):
  * An ID is *defined* where it appears in bold, e.g. **REQ-001**.
  * Every defined REQ/NFR must be referenced by at least one TC definition line
    in docs/05-test-design/test-design.md.
  * Every defined TC must appear in test code (app/src/test, app/src/androidTest)
    or in a journey (journeys/*.xml) -- enforced only once phase 6 has started
    (i.e. when --require-impl is given or PROGRESS.md marks phase 6 IN_PROGRESS/DONE).
  * Referenced IDs must be defined somewhere (no dangling references).

Exits 0 with a notice when the documents do not exist yet.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
REQ_DOC = DOCS / "01-requirements" / "requirements.md"
TEST_DOC = DOCS / "05-test-design" / "test-design.md"
PROGRESS = DOCS / "PROGRESS.md"
CODE_DIRS = [ROOT / "app" / "src" / "test", ROOT / "app" / "src" / "androidTest", ROOT / "journeys"]

ID_RE = re.compile(r"\b(REQ|NFR|SCR|DD|TC|JR)-\d{3}\b")
DEF_RE = re.compile(r"\*\*((?:REQ|NFR|SCR|DD|TC|JR)-\d{3})\*\*")
STRUCK_RE = re.compile(r"~~((?:REQ|NFR|SCR|DD|TC|JR)-\d{3})~~")
TEMPLATE_MARK = "<!-- TEMPLATE -->"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8") if path.exists() else ""


def strip_code(text: str) -> str:
    """Remove fenced and inline code so that examples are not treated as IDs."""
    text = re.sub(r"```.*?```", "", text, flags=re.S)
    return re.sub(r"`[^`\n]*`", "", text)


def deliverable(path: Path) -> str:
    text = read(path)
    return "" if TEMPLATE_MARK in text else strip_code(text)


def definitions(text: str) -> set[str]:
    return set(DEF_RE.findall(text)) - set(STRUCK_RE.findall(text))


def phase6_started() -> bool:
    for line in read(PROGRESS).splitlines():
        if re.match(r"\|\s*6\s*\|", line) and re.search(r"IN_PROGRESS|IN_REVIEW|APPROVED|DONE", line):
            return True
    return False


def main() -> int:
    require_impl = "--require-impl" in sys.argv or phase6_started()
    errors: list[str] = []

    req_text = deliverable(REQ_DOC)
    test_text = deliverable(TEST_DOC)
    if not req_text:
        print("trace-check: requirements not written yet -> skipped")
        return 0

    reqs = {i for i in definitions(req_text) if i.startswith(("REQ-", "NFR-"))}

    phase_docs = [p for d in sorted(DOCS.glob("0[1-5]-*")) + [DOCS / "adr"] for p in d.rglob("*.md")]
    all_docs = "\n".join(deliverable(p) for p in phase_docs)
    defined_anywhere = definitions(all_docs) | set(STRUCK_RE.findall(all_docs))
    for ref in sorted(set(m.group(0) for m in ID_RE.finditer(all_docs)) - defined_anywhere):
        if not ref.startswith("JR-"):
            errors.append(f"dangling reference: {ref} is referenced but never defined (**{ref}**)")

    if test_text:
        tc_lines = [ln for ln in test_text.splitlines() if "**TC-" in ln]
        covered = {i for ln in tc_lines for i in re.findall(r"\b(?:REQ|NFR)-\d{3}\b", ln)}
        for req in sorted(reqs - covered):
            errors.append(f"{req} has no test case (no **TC-xxx** line references it)")

        if require_impl:
            tcs = {i for i in definitions(test_text) if i.startswith("TC-")}
            code = "\n".join(
                p.read_text(encoding="utf-8", errors="ignore")
                for d in CODE_DIRS
                if d.exists()
                for p in d.rglob("*")
                if p.is_file() and p.suffix in {".kt", ".xml"}
            )
            for tc in sorted(tcs):
                if tc not in code:
                    errors.append(f"{tc} is not implemented in test code or journeys")
    else:
        print("trace-check: test design not written yet -> REQ->TC coverage skipped")

    if errors:
        print("trace-check: FAILED")
        for e in errors:
            print(f"  - {e}")
        return 1
    print(f"trace-check: OK ({len(reqs)} requirements)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
