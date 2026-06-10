#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASS=0; FAIL=0

check() {
  local desc="$1"; local url="$2"; local expected="${3:-200}"
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$url")
  if [ "$STATUS" = "$expected" ]; then
    echo "✅ PASS: $desc ($STATUS)"
    ((PASS++))
  else
    echo "❌ FAIL: $desc (expected $expected, got $STATUS)"
    ((FAIL++))
  fi
}

echo "Running smoke tests against $BASE_URL"
check "Health check"         "$BASE_URL/actuator/health"
check "App info"             "$BASE_URL/api/"
check "List tasks"           "$BASE_URL/api/tasks"
check "Task stats"           "$BASE_URL/api/tasks/stats"
check "Non-existent task"    "$BASE_URL/api/tasks/999999" "404"
check "Prometheus metrics"   "$BASE_URL/actuator/prometheus"

echo ""
echo "Results: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ] || exit 1
