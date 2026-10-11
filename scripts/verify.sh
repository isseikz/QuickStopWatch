#!/usr/bin/env bash
# Harness: the single source of truth for "is the code good?".
# Used by agents (L1/L2/L3 loops), the Stop hook, and CI.
#
#   ./scripts/verify.sh          full gate (local): format, lint, tests, screenshot verify, R8 build, traceability
#   ./scripts/verify.sh --fast   inner TDD loop: format + JVM unit tests only
#   ./scripts/verify.sh --ci     like full, but screenshots are compared (report only) instead of
#                                verified, because Robolectric rendering differs between macOS and Linux
set -euo pipefail

cd "$(dirname "$0")/.."
ROOT="$(pwd)"

MODE="full"
for arg in "$@"; do
  case "$arg" in
    --fast) MODE="fast" ;;
    --ci) MODE="ci" ;;
    -h | --help)
      sed -n '2,9p' "$0"
      exit 0
      ;;
    *)
      echo "unknown option: $arg" >&2
      exit 2
      ;;
  esac
done

# Prefer JDK 17 locally when JAVA_HOME is not set (CI sets it via setup-java).
if [[ -z "${JAVA_HOME:-}" && -x /usr/libexec/java_home ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
  export JAVA_HOME
fi

GRADLE=("./gradlew" "--console=plain")

step() { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }

case "$MODE" in
  fast)
    step "spotlessCheck + unit tests (fast)"
    "${GRADLE[@]}" spotlessCheck :app:testDebugUnitTest
    ;;
  full)
    step "spotlessCheck + lint + unit/UI tests + screenshot verify + debug/release build"
    "${GRADLE[@]}" spotlessCheck :app:lintDebug :app:verifyRoborazziDebug :app:assembleDebug :app:assembleRelease
    step "traceability"
    "$ROOT/scripts/trace-check.sh"
    ;;
  ci)
    step "spotlessCheck + lint + unit/UI tests + screenshot compare + debug/release build"
    "${GRADLE[@]}" spotlessCheck :app:lintDebug :app:compareRoborazziDebug :app:assembleDebug :app:assembleRelease
    step "traceability"
    "$ROOT/scripts/trace-check.sh"
    ;;
esac

# Marker consumed by the Stop hook (.agents/hooks.json -> scripts/hooks/stop_gate.py).
if [[ "$MODE" != "fast" ]]; then
  mkdir -p "$ROOT/.harness"
  date -u +%Y-%m-%dT%H:%M:%SZ >"$ROOT/.harness/last-verify-ok"
fi

printf '\n\033[1;32mverify (%s): OK\033[0m\n' "$MODE"
