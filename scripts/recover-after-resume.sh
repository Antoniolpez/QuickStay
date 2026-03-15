#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

NETWORKS=(
    quickstay_app_net
    quickstay_dmz_net
    quickstay_mgmt_net
    quickstay_iot_net
    quickstay_vpn_net
)

has_bridge_ipv4() {
    local net="$1"
    local net_id bridge
    net_id="$(docker network inspect -f '{{.Id}}' "$net" 2>/dev/null | cut -c1-12 || true)"
    if [[ -z "$net_id" ]]; then
        return 1
    fi
    bridge="br-$net_id"
    ip -4 addr show "$bridge" 2>/dev/null | grep -q 'inet '
}

broken=0
for net in "${NETWORKS[@]}"; do
    if ! has_bridge_ipv4 "$net"; then
        echo "[WARN] Red sin gateway IPv4 operativo: $net"
        broken=1
    fi
done

if [[ "$broken" -eq 0 ]]; then
    echo "[OK] Redes Docker QuickStay sanas. No se requiere recuperacion."
    exit 0
fi

echo "[FIX] Recuperando redes QuickStay (compose down/up)..."
docker compose down
docker compose up -d
echo "[DONE] Recuperacion completada."
