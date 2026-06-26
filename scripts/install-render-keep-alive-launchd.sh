#!/usr/bin/env bash
set -uo pipefail

SOURCE_DIR="$(cd "$(dirname "$0")" && pwd)"
INSTALL_DIR="${HOME}/.sust-preli/keep-alive"
PLIST_SRC="${SOURCE_DIR}/com.sustpreli.render-keep-alive.plist"
PLIST_DST="${HOME}/Library/LaunchAgents/com.sustpreli.render-keep-alive.plist"
LABEL="com.sustpreli.render-keep-alive"

mkdir -p "$INSTALL_DIR"
cp "${SOURCE_DIR}/render-keep-alive.sh" "${INSTALL_DIR}/render-keep-alive.sh"
cp "${SOURCE_DIR}/stop-render-keep-alive.sh" "${INSTALL_DIR}/stop-render-keep-alive.sh"
chmod +x "${INSTALL_DIR}/render-keep-alive.sh" "${INSTALL_DIR}/stop-render-keep-alive.sh"

# Point LaunchAgent at the home-directory copy (launchd cannot read Documents on macOS).
sed "s|__INSTALL_DIR__|${INSTALL_DIR}|g" "${SOURCE_DIR}/com.sustpreli.render-keep-alive.plist.template" > "$PLIST_DST"

launchctl bootout "gui/$(id -u)" "$PLIST_DST" 2>/dev/null || true
launchctl bootstrap "gui/$(id -u)" "$PLIST_DST"
launchctl enable "gui/$(id -u)/${LABEL}"
launchctl kickstart -k "gui/$(id -u)/${LABEL}"

echo "Keep-alive installed to: ${INSTALL_DIR}"
echo "LaunchAgent: ${PLIST_DST}"
echo "Log: ${INSTALL_DIR}/render-keep-alive.log"
echo "Stop: ${SOURCE_DIR}/stop-render-keep-alive-launchd.sh"
