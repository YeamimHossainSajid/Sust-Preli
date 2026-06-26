#!/usr/bin/env bash
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="${SCRIPT_DIR}/render-keep-alive.pid"
LOG_FILE="${SCRIPT_DIR}/render-keep-alive.log"

if [[ -f "$PID_FILE" ]]; then
  OLD_PID="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [[ -n "$OLD_PID" ]] && kill -0 "$OLD_PID" 2>/dev/null; then
    echo "Keep-alive already running (pid ${OLD_PID})"
    echo "Log: ${LOG_FILE}"
    exit 0
  fi
  rm -f "$PID_FILE"
fi

nohup "${SCRIPT_DIR}/render-keep-alive.sh" >> "$LOG_FILE" 2>&1 &
START_PID=$!
disown "$START_PID" 2>/dev/null || true

sleep 1
if [[ -f "$PID_FILE" ]]; then
  RUNNING_PID="$(cat "$PID_FILE")"
  echo "Keep-alive started (pid ${RUNNING_PID})"
else
  echo "Keep-alive launcher pid ${START_PID} (waiting for pid file...)"
fi

echo "Log: ${LOG_FILE}"
echo "Stop: ${SCRIPT_DIR}/stop-render-keep-alive.sh"
