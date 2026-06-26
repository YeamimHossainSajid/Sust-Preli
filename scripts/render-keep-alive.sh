#!/usr/bin/env bash
# Render free-tier keep-alive: ping /health every 10 minutes for 12 days.
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_URL="${RENDER_KEEP_ALIVE_URL:-https://sust-preli-b8l9.onrender.com}"
HEALTH_PATH="${RENDER_KEEP_ALIVE_PATH:-/health}"
INTERVAL_SECONDS="${RENDER_KEEP_ALIVE_INTERVAL:-600}"
DURATION_DAYS="${RENDER_KEEP_ALIVE_DAYS:-12}"
CURL_TIMEOUT_SECONDS="${RENDER_KEEP_ALIVE_TIMEOUT:-90}"
LOG_FILE="${RENDER_KEEP_ALIVE_LOG:-${SCRIPT_DIR}/render-keep-alive.log}"
PID_FILE="${RENDER_KEEP_ALIVE_PID:-${SCRIPT_DIR}/render-keep-alive.pid}"

URL="${BASE_URL%/}${HEALTH_PATH}"
END_EPOCH=$(( $(date +%s) + DURATION_DAYS * 86400 ))

timestamp() {
  date '+%Y-%m-%d %H:%M:%S'
}

log() {
  printf '[%s] %s\n' "$(timestamp)" "$*" >> "$LOG_FILE"
}

format_epoch() {
  local epoch="$1"
  if date -r "$epoch" '+%Y-%m-%d %H:%M:%S' >/dev/null 2>&1; then
    date -r "$epoch" '+%Y-%m-%d %H:%M:%S'
  else
    date -u -d "@${epoch}" '+%Y-%m-%d %H:%M:%S' 2>/dev/null || echo "epoch ${epoch}"
  fi
}

ping_health() {
  local http_code
  http_code="$(curl -sS -m "${CURL_TIMEOUT_SECONDS}" -o /dev/null -w '%{http_code}' "$URL" 2>/dev/null || true)"
  if [[ "$http_code" == "200" ]]; then
    log "OK ping ${URL} (HTTP ${http_code})"
    return 0
  fi

  if [[ -z "$http_code" || "$http_code" == "000" ]]; then
    log "FAIL ping ${URL} (timeout or connection error; Render may be cold-starting)"
  else
    log "FAIL ping ${URL} (HTTP ${http_code})"
  fi
  return 1
}

cleanup() {
  log "Keep-alive received stop signal (pid $$)"
  rm -f "$PID_FILE"
  exit 0
}

trap cleanup INT TERM

echo $$ > "$PID_FILE"
log "Keep-alive started (pid $$)"
log "Target: ${URL}"
log "Interval: ${INTERVAL_SECONDS}s | Timeout: ${CURL_TIMEOUT_SECONDS}s | Duration: ${DURATION_DAYS} days | Ends: $(format_epoch "$END_EPOCH")"

while [[ $(date +%s) -lt $END_EPOCH ]]; do
  ping_health || true
  sleep "$INTERVAL_SECONDS" || sleep "$INTERVAL_SECONDS" || true
done

log "Keep-alive finished after ${DURATION_DAYS} days"
rm -f "$PID_FILE"
