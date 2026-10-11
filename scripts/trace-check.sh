#!/usr/bin/env bash
# Harness: traceability check wrapper. See scripts/trace_check.py for the rules.
set -euo pipefail
exec python3 "$(dirname "$0")/trace_check.py" "$@"
