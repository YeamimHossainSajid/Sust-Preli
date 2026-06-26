#!/usr/bin/env bash
set -uo pipefail

PLIST_DST="${HOME}/Library/LaunchAgents/com.sustpreli.render-keep-alive.plist"
LABEL="com.sustpreli.render-keep-alive"
INSTALL_DIR="${HOME}/.sust-preli/keep-alive"

launchctl bootout "gui/$(id -u)" "$PLIST_DST" 2>/dev/null || true
rm -f "$PLIST_DST"

if [[ -f "${INSTALL_DIR}/render-keep-alive.pid" ]]; then
  PID="$(cat "${INSTALL_DIR}/render-keep-alive.pid" 2>/dev/null || true)"
  if [[ -n "$PID" ]] && kill -0 "$PID" 2>/dev/null; then
    kill "$PID" 2>/dev/null || true
  fi
  rm -f "${INSTALL_DIR}/render-keep-alive.pid"
fi

echo "LaunchAgent stopped and removed."
