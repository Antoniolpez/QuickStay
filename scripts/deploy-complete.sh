#!/bin/bash
# ============================================================================
# QUICKSTAY INFRASTRUCTURE - MASTER DEPLOYMENT SCRIPT (v2 - ENHANCED)
# ============================================================================
# Script de despliegue completo con validaciones extensivas
# Autor: Antonio López Montes
# Fecha: Enero 2026
# ============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Contadores
WARNINGS=0
ERRORS=0
CHECKS_PASSED=0

# Funciones de utilidad
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
    CHECKS_PASSED=$((CHECKS_PASSED + 1))
}

log_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
    WARNINGS=$((WARNINGS + 1))
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
    ERRORS=$((ERRORS + 1))
}

log_debug() {
    echo -e "${MAGENTA}[DEBUG]${NC} $1"
}

COMPOSE_CMD=()

init_compose_cmd() {
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD=(docker compose)
        return 0
    fi

    if command -v docker-compose >/dev/null 2>&1; then
        COMPOSE_CMD=(docker-compose)
        return 0
    fi

    log_error "Docker Compose no encontrado (ni plugin v2 ni binario docker-compose)"
    return 1
}

compose() {
    if [ "${#COMPOSE_CMD[@]}" -eq 0 ]; then
        init_compose_cmd || return 1
    fi

    "${COMPOSE_CMD[@]}" "$@"
}

load_secrets_env() {
    if [ ! -f "secrets.env" ]; then
        return 0
    fi

    set -a
    # shellcheck disable=SC1091
    source secrets.env
    set +a
}

set_secret_default_if_empty() {
    local key="$1"
    local default_value="$2"
    local file="secrets.env"

    [ -f "$file" ] || return 0

    local line
    line="$(grep -E "^${key}=" "$file" 2>/dev/null || true)"

    if [ -z "$line" ]; then
        echo "${key}=${default_value}" >> "$file"
        return 0
    fi

    local current_value="${line#*=}"
    if [ -z "$current_value" ]; then
        sed -i "s|^${key}=.*|${key}=${default_value}|" "$file"
    fi
}

ensure_lab_zabbix_agent_defaults() {
    if [ ! -f "secrets.env" ]; then
        return 0
    fi

    local agent_server
    local server_host
    agent_server="$(grep -E '^ZABBIX_AGENT_SERVER=' secrets.env 2>/dev/null | head -n1 | cut -d= -f2-)"
    server_host="$(grep -E '^ZABBIX_SERVER_HOST=' secrets.env 2>/dev/null | head -n1 | cut -d= -f2-)"

    if [ -n "$agent_server" ] || [ -n "$server_host" ]; then
        return 0
    fi

    log_warning "ZABBIX_AGENT_SERVER/ZABBIX_SERVER_HOST vacíos: aplicando defaults de laboratorio (172.16.30.22)"
    set_secret_default_if_empty "ZABBIX_SERVER_HOST" "172.16.30.22"
    set_secret_default_if_empty "ZABBIX_AGENT_SERVER" "172.16.30.22"
    set_secret_default_if_empty "ZABBIX_AGENT_ACTIVE_SERVER" "172.16.30.22"

    # Refrescar variables en el shell actual para fases posteriores del deploy.
    set -a
    # shellcheck disable=SC1091
    source secrets.env
    set +a

    log_success "Defaults Zabbix de laboratorio aplicados para agentes simulados"
}

run_factory_reset_cleanup() {
    log_warning "Iniciando limpieza tipo 'estado base' antes del despliegue..."

    if [ -f "cleanup-all.sh" ]; then
        log_info "Ejecutando cleanup-all.sh..."
        bash ./cleanup-all.sh || log_warning "cleanup-all.sh devolvió advertencias"
    else
        log_warning "cleanup-all.sh no encontrado; se aplica limpieza mínima"
        compose --env-file secrets.env down -v --remove-orphans >/dev/null 2>&1 || true
    fi

    log_info "Aplicando limpieza global de Docker (imágenes/cachés/volúmenes no usados)..."
    docker system prune -af --volumes >/dev/null 2>&1 || true
    docker builder prune -af >/dev/null 2>&1 || true

    log_success "Limpieza previa completada"
}

maybe_offer_factory_reset() {
    local auto_reset="${QUICKSTAY_FACTORY_RESET:-ask}"

    if [ "$auto_reset" = "1" ] || [ "$auto_reset" = "true" ]; then
        run_factory_reset_cleanup
        return 0
    fi

    if [ "$auto_reset" = "0" ] || [ "$auto_reset" = "false" ]; then
        log_info "Limpieza previa forzada desactivada por QUICKSTAY_FACTORY_RESET=${auto_reset}"
        return 0
    fi

    if [ ! -t 0 ]; then
        log_info "Modo no interactivo detectado; se omite pregunta de limpieza previa"
        return 0
    fi

    echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
    log_info "FASE 0: LIMPIEZA PREVIA OPCIONAL"
    echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
    echo ""
    log_warning "Esto eliminará contenedores, volúmenes, cachés Docker y artefactos generados de QuickStay."
    log_warning "El código del proyecto se conserva."
    echo ""

    local confirm
    read -r -p "Escribe LIMPIAR para resetear antes de desplegar (Enter para omitir): " confirm
    if [ "$confirm" = "LIMPIAR" ]; then
        run_factory_reset_cleanup
    else
        log_info "Limpieza previa omitida por el usuario"
    fi
}

docker_cleanup_for_build() {
    local cleanup_log="logs/deploy/prebuild-cleanup.log"
    mkdir -p logs/deploy

    log_warning "Ejecutando limpieza Docker para recuperar espacio antes del build..."
    {
        echo "===== $(date '+%F %T') :: docker builder prune -af ====="
        docker builder prune -af
        echo ""
        echo "===== $(date '+%F %T') :: docker image prune -af ====="
        docker image prune -af
        echo ""
        echo "===== $(date '+%F %T') :: docker container prune -f ====="
        docker container prune -f
        echo ""
        echo "===== $(date '+%F %T') :: docker volume prune -f ====="
        docker volume prune -f
        echo ""
        echo "===== $(date '+%F %T') :: docker system df ====="
        docker system df
        echo ""
    } >> "$cleanup_log" 2>&1 || true

    log_info "Limpieza registrada en: $cleanup_log"
}

ensure_build_disk_headroom() {
    local min_free_gb="${DEPLOY_MIN_FREE_GB:-12}"
    local min_free_kb=$((min_free_gb * 1024 * 1024))
    local avail_kb
    avail_kb="$(df -Pk / | awk 'NR==2{print $4}')"
    local avail_gb=$((avail_kb / 1024 / 1024))

    log_info "Verificando espacio libre para build: ${avail_gb}GB disponibles (mínimo ${min_free_gb}GB)"

    if [ "$avail_kb" -ge "$min_free_kb" ]; then
        log_success "Espacio suficiente para construir imágenes"
        return 0
    fi

    log_warning "Espacio insuficiente para build (disponible=${avail_gb}GB, mínimo=${min_free_gb}GB)"
    docker_cleanup_for_build

    avail_kb="$(df -Pk / | awk 'NR==2{print $4}')"
    avail_gb=$((avail_kb / 1024 / 1024))
    log_info "Espacio tras limpieza: ${avail_gb}GB"

    if [ "$avail_kb" -lt "$min_free_kb" ]; then
        log_error "No hay espacio suficiente tras limpieza automática. Libera disco o baja DEPLOY_MIN_FREE_GB."
        return 1
    fi

    log_success "Espacio recuperado; continuando con build"
}

run_compose_build() {
    local build_log="logs/deploy/build.log"
    set +e
    compose --env-file secrets.env build --parallel 2>&1 | tee "$build_log"
    local build_exit=${PIPESTATUS[0]}
    set -e
    return "$build_exit"
}

generate_default_vpn_peers() {
    local peers_count="${DEPLOY_DEFAULT_PEERS:-3}"
    local peers_root="wireguard/config/peers"
    local router_container="quickstay-router-fw"
    local retries="${DEPLOY_WG_PEER_RETRIES:-10}"
    local delay="${DEPLOY_WG_PEER_DELAY_SEC:-5}"
    local failed=0

    if ! docker ps --format '{{.Names}}' | grep -qx "$router_container"; then
        log_warning "Router no disponible; se omite generación automática de peers"
        return 0
    fi

    mkdir -p "$peers_root"
    log_info "Generando peers WireGuard por defecto (${peers_count})..."

    local ready=0
    local r_try
    for r_try in $(seq 1 "$retries"); do
        if docker exec -i "$router_container" python3 - <<'PY' >/dev/null 2>&1
import base64
import os
import urllib.request

u = os.getenv("ROUTER_ADMIN_USER", "admin")
p = os.getenv("ROUTER_ADMIN_PASSWORD", "QuickStay2026!Router")
token = base64.b64encode(f"{u}:{p}".encode()).decode()
req = urllib.request.Request("http://127.0.0.1:8443/api/vpn/peers", headers={"Authorization": f"Basic {token}"})
with urllib.request.urlopen(req, timeout=6):
    pass
PY
        then
            ready=1
            break
        fi
        sleep "$delay"
    done

    if [ "$ready" -ne 1 ]; then
        log_warning "API del router no lista para WireGuard; se omite generación automática de peers"
        return 0
    fi

    local i
    for i in $(seq 1 "$peers_count"); do
        local peer_name="peer${i}"
        local peer_conf="${peers_root}/${peer_name}.conf"
        local peer_qr_png="${peers_root}/${peer_name}.qr.png"
        local peer_qr_txt="${peers_root}/${peer_name}.qr.txt"

        local py_out=""
        local cmd_ok=0
        local attempt
        for attempt in $(seq 1 "$retries"); do
            local out_file="/tmp/quickstay-wg-peer-${peer_name}.out"
            rm -f "$out_file"

            if docker exec -i "$router_container" python3 - "$peer_name" <<'PY' >"$out_file" 2>&1
import base64
import json
import os
import sys
import urllib.request

name = sys.argv[1]
user = os.getenv("ROUTER_ADMIN_USER", "admin")
password = os.getenv("ROUTER_ADMIN_PASSWORD", "QuickStay2026!Router")
token = base64.b64encode(f"{user}:{password}".encode()).decode()

headers = {
    "Authorization": f"Basic {token}",
    "Content-Type": "application/json",
}

req = urllib.request.Request(
    "http://127.0.0.1:8443/api/vpn/add",
    data=json.dumps({"name": name}).encode(),
    headers=headers,
)

try:
    with urllib.request.urlopen(req, timeout=20) as r:
        payload = json.loads(r.read().decode())
    print(json.dumps({"ok": True, "payload": payload}))
except urllib.error.HTTPError as e:
    try:
        body = e.read().decode()
    except Exception:
        body = str(e)
    print(json.dumps({"ok": False, "status": e.code, "error": body}))
except Exception as e:
    print(json.dumps({"ok": False, "error": str(e)}))
PY
            then
                cmd_ok=1
            else
                cmd_ok=0
            fi

            py_out="$(cat "$out_file" 2>/dev/null || true)"

            if echo "$py_out" | grep -q '"ok": true'; then
                break
            fi

            # Si ya existe una conf válida del peer, la reaprovechamos y continuamos.
            if [ -f "wireguard/config/peer_${peer_name}.conf" ] || [ -f "wireguard/config/peers/${peer_name}.conf" ] || [ -f "$peer_conf" ]; then
                py_out="peer existente detectado en artefactos WireGuard"
                cmd_ok=1
                break
            fi

            if [ "$attempt" -lt "$retries" ]; then
                sleep "$delay"
            fi
        done

        if echo "$py_out" | grep -q '"ok": true' || [ "$cmd_ok" -eq 1 ]; then
            docker exec "$router_container" sh -lc "
                set -e
                d=/etc/wireguard/peers/${peer_name}
                mkdir -p \"\$d\"
                if [ -f /etc/wireguard/peer_${peer_name}.conf ]; then
                    cp -f /etc/wireguard/peer_${peer_name}.conf \"\$d/${peer_name}.conf\"
                elif [ -f /etc/wireguard/peers/${peer_name}.conf ]; then
                    cp -f /etc/wireguard/peers/${peer_name}.conf \"\$d/${peer_name}.conf\"
                fi
                [ -f /etc/wireguard/peers/${peer_name}.qr.png ] && cp -f /etc/wireguard/peers/${peer_name}.qr.png \"\$d/${peer_name}.qr.png\" || true
                [ -f /etc/wireguard/peers/${peer_name}.qr.txt ] && cp -f /etc/wireguard/peers/${peer_name}.qr.txt \"\$d/${peer_name}.qr.txt\" || true
            " >/dev/null 2>&1 || true

            [ -f "wireguard/config/peer_${peer_name}.conf" ] && cp -f "wireguard/config/peer_${peer_name}.conf" "$peer_conf" 2>/dev/null || true
            [ -f "wireguard/config/peers/${peer_name}.conf" ] && cp -f "wireguard/config/peers/${peer_name}.conf" "$peer_conf" 2>/dev/null || true
            [ -f "wireguard/config/peers/${peer_name}.qr.png" ] && cp -f "wireguard/config/peers/${peer_name}.qr.png" "$peer_qr_png" 2>/dev/null || true
            [ -f "wireguard/config/peers/${peer_name}.qr.txt" ] && cp -f "wireguard/config/peers/${peer_name}.qr.txt" "$peer_qr_txt" 2>/dev/null || true

            if [ ! -s "$peer_conf" ]; then
                log_error "Peer ${peer_name} no generado: falta ${peer_conf}"
                failed=$((failed + 1))
                continue
            fi

            log_success "Peer ${peer_name} generado en ${peers_root}/"
        else
            if [ -z "$py_out" ]; then
                py_out="sin salida del comando (router/API no disponible)"
            fi
            log_error "No se pudo generar ${peer_name}: ${py_out}"
            failed=$((failed + 1))
        fi
    done

    if [ "$failed" -gt 0 ]; then
        log_error "Falló la generación de ${failed}/${peers_count} peers WireGuard"
        return 1
    fi

    return 0
}

is_transient_network_build_failure() {
    local build_log="logs/deploy/build.log"
    grep -Eiq 'i/o timeout|DeadlineExceeded|TLS handshake timeout|context deadline exceeded|failed to resolve source metadata|failed to do request' "$build_log"
}

is_transient_network_up_failure() {
    local startup_log="logs/deploy/startup.log"
    grep -Eiq 'i/o timeout|DeadlineExceeded|TLS handshake timeout|context deadline exceeded|failed to do request|failed to copy: httpReadSeeker|temporary failure' "$startup_log"
}

pull_image_with_retries() {
    local image="$1"
    local pull_log="logs/deploy/prefetch-runtime-images.log"
    local retries="${DEPLOY_PULL_RETRIES:-3}"
    local delay="${DEPLOY_PULL_DELAY_SEC:-8}"
    local attempt=1
    local ok=0

    while [ "$attempt" -le "$retries" ]; do
        log_info "   Pull ${image} (intento ${attempt}/${retries})"
        if docker pull "$image" >> "$pull_log" 2>&1; then
            ok=1
            break
        fi

        # Fallback para imágenes oficiales de Docker Hub sin namespace (library/*).
        if [[ "$image" != */* ]]; then
            local mirror_image="mirror.gcr.io/library/${image}"
            log_info "   Fallback mirror ${mirror_image}"
            if docker pull "$mirror_image" >> "$pull_log" 2>&1; then
                docker tag "$mirror_image" "$image" >> "$pull_log" 2>&1 || true
                ok=1
                break
            fi
        fi

        if [ "$attempt" -lt "$retries" ]; then
            sleep "$delay"
        fi
        attempt=$((attempt + 1))
    done

    if [ "$ok" -eq 1 ]; then
        log_success "   Imagen runtime lista: ${image}"
        return 0
    fi

    log_warning "   No se pudo predescargar ${image}; compose intentará durante up"
    return 1
}

prefetch_runtime_images() {
    local pull_log="logs/deploy/prefetch-runtime-images.log"
    mkdir -p logs/deploy
    : > "$pull_log"

    log_info "Prefetch de imágenes runtime de docker-compose..."
    local images
    images="$(compose --env-file secrets.env config 2>/dev/null | awk '/^[[:space:]]*image:[[:space:]]/{print $2}' | sort -u)"
    if [ -z "$images" ]; then
        log_warning "No se pudieron listar imágenes runtime desde docker-compose"
        return 0
    fi

    local img
    while IFS= read -r img; do
        [ -z "$img" ] && continue
        pull_image_with_retries "$img" || true
    done <<< "$images"

    log_info "Detalle de prefetch runtime: $pull_log"
}

run_compose_up() {
    local startup_log="logs/deploy/startup.log"
    set +e
    compose --env-file secrets.env up -d 2>&1 | tee "$startup_log"
    local up_exit=${PIPESTATUS[0]}
    set -e
    return "$up_exit"
}

prefetch_base_images() {
    local pull_log="logs/deploy/prefetch-base-images.log"
    mkdir -p logs/deploy

    local retries="${DEPLOY_PULL_RETRIES:-3}"
    local delay="${DEPLOY_PULL_DELAY_SEC:-8}"
    local images=(
        "php:8.2-apache"
        "python:3.11-slim"
        "ubuntu:22.04"
        "node:20-alpine"
        "haproxy:2.8"
        "maven:3.9-eclipse-temurin-19"
        "eclipse-temurin:19-jre"
        "mysql:8.0-bookworm"
    )

    log_info "Prefetch de imágenes base para reducir fallos transitorios de red..."

    for image in "${images[@]}"; do
        if docker image inspect "$image" >/dev/null 2>&1; then
            log_success "   Imagen base ya local: ${image}"
            continue
        fi

        local mirror_image="mirror.gcr.io/library/${image}"
        local ok=0
        local attempt=1
        while [ "$attempt" -le "$retries" ]; do
            log_info "   Pull mirror ${mirror_image} (intento ${attempt}/${retries})"
            if docker pull "$mirror_image" >> "$pull_log" 2>&1 && docker tag "$mirror_image" "$image" >> "$pull_log" 2>&1; then
                ok=1
                break
            fi

            log_warning "   Mirror no disponible para ${image}; intentando Docker Hub"
            if docker pull "$image" >> "$pull_log" 2>&1; then
                ok=1
                break
            fi

            if [ "$attempt" -lt "$retries" ]; then
                sleep "$delay"
            fi
            attempt=$((attempt + 1))
        done

        if [ "$ok" -eq 1 ]; then
            log_success "   Imagen base lista: ${image}"
        else
            log_warning "   No se pudo predescargar ${image}; BuildKit intentará durante el build"
        fi
    done

    log_info "Detalle de prefetch: $pull_log"
}

auto_pin_latest_images() {
    local compose_file="docker-compose.yml"
    if [ ! -f "$compose_file" ]; then
        log_warning "No existe $compose_file; se omite autocorreccion de tags latest"
        return 0
    fi

    if ! grep -Eq '^\s*image:\s*[^#]+:latest\s*$' "$compose_file"; then
        log_success "No se detectaron imagenes con tag latest"
        return 0
    fi

    log_warning "Detectado tag latest en $compose_file; aplicando autocorreccion inmutable..."

    if ! command -v python3 >/dev/null 2>&1; then
        log_warning "python3 no disponible; no se puede autocorregir latest"
        return 0
    fi

    local py_out
    py_out="$(python3 - "$compose_file" <<'PY'
import re
import subprocess
import sys
from pathlib import Path

path = Path(sys.argv[1])
lines = path.read_text(encoding="utf-8").splitlines(True)
pat = re.compile(r'^(\s*image:\s*)(["\']?)([^"\']+)(["\']?)\s*$')

fallback_map = {
    "grafana/grafana:latest": "grafana/grafana:11.1.0",
}

changed = False
updated = []
unresolved = []

for idx, line in enumerate(lines):
    m = pat.match(line.rstrip("\n"))
    if not m:
        continue
    prefix, q1, image, q2 = m.groups()
    if not image.endswith(":latest"):
        continue

    replacement = None

    # Try local digest first.
    try:
        out = subprocess.check_output(
            ["docker", "image", "inspect", "--format", "{{index .RepoDigests 0}}", image],
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
        if out and "@sha256:" in out:
            replacement = out
    except Exception:
        pass

    # Pull and inspect when digest is not present locally.
    if replacement is None:
        try:
            subprocess.check_call(["docker", "pull", image], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            out = subprocess.check_output(
                ["docker", "image", "inspect", "--format", "{{index .RepoDigests 0}}", image],
                text=True,
                stderr=subprocess.DEVNULL,
            ).strip()
            if out and "@sha256:" in out:
                replacement = out
        except Exception:
            pass

    if replacement is None:
        replacement = fallback_map.get(image)

    if replacement is None:
        unresolved.append(image)
        continue

    quote = q1 if q1 else (q2 if q2 else "")
    lines[idx] = f"{prefix}{quote}{replacement}{quote}\n"
    changed = True
    updated.append((image, replacement))

if changed:
    path.write_text("".join(lines), encoding="utf-8")

for src, dst in updated:
    print(f"UPDATED:{src}->{dst}")
for img in unresolved:
    print(f"UNRESOLVED:{img}")
print(f"CHANGED:{1 if changed else 0}")
PY
)"

    if [ -n "$py_out" ]; then
        while IFS= read -r ln; do
            case "$ln" in
                UPDATED:*)
                    log_success "${ln#UPDATED:}"
                    ;;
                UNRESOLVED:*)
                    log_warning "No se pudo fijar tag para ${ln#UNRESOLVED:}; se mantiene temporalmente"
                    ;;
                CHANGED:1)
                    log_info "docker-compose.yml actualizado con tags inmutables"
                    ;;
            esac
        done <<< "$py_out"
    fi
}

heal_unhealthy_services() {
    local attempts=${1:-4}
    local sleep_between=${2:-20}

    log_info "Autorecuperacion de contenedores unhealthy (hasta ${attempts} intentos)..."

    for n in $(seq 1 "$attempts"); do
        local unhealthy
        unhealthy="$(docker ps -a --filter "name=quickstay-" --filter "health=unhealthy" --format '{{.Names}}' | tr '\n' ' ')"

        if [ -z "${unhealthy// }" ]; then
            log_success "No hay contenedores unhealthy"
            return 0
        fi

        log_warning "Intento ${n}/${attempts}: unhealthy detectados -> ${unhealthy}"

        for c in $unhealthy; do
            docker restart "$c" >/dev/null 2>&1 || true

            # Si sigue unhealthy, recrear por servicio compose.
            local service
            service="$(docker inspect -f '{{ index .Config.Labels "com.docker.compose.service" }}' "$c" 2>/dev/null || true)"
            if [ -n "$service" ]; then
                compose --env-file secrets.env up -d --force-recreate "$service" >/dev/null 2>&1 || true
            fi
        done

        sleep "$sleep_between"
    done

    local remaining
    remaining="$(docker ps -a --filter "name=quickstay-" --filter "health=unhealthy" --format '{{.Names}}' | tr '\n' ' ')"
    if [ -n "${remaining// }" ]; then
        log_warning "Persisten unhealthy tras autorecuperacion: $remaining"
    else
        log_success "Autorecuperacion completada: todos healthy/up"
    fi
}

setup_resume_recovery() {
        log_info "Configurando auto-recuperacion tras suspend/resume..."

        mkdir -p deploy/systemd

        cat > recover-after-resume.sh << 'EOF_RECOVER'
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
EOF_RECOVER

        chmod +x recover-after-resume.sh

        cat > deploy/systemd/quickstay-resume-recover.service << 'EOF_UNIT'
[Unit]
Description=QuickStay network recovery after suspend/resume
After=docker.service network-online.target suspend.target
Wants=docker.service network-online.target

[Service]
Type=oneshot
WorkingDirectory=/home/jefe/quickstay
ExecStart=/home/jefe/quickstay/recover-after-resume.sh

[Install]
WantedBy=suspend.target
WantedBy=hibernate.target
WantedBy=hybrid-sleep.target
EOF_UNIT

        if command -v systemctl >/dev/null 2>&1; then
                if sudo -n true >/dev/null 2>&1; then
                        sudo cp deploy/systemd/quickstay-resume-recover.service /etc/systemd/system/quickstay-resume-recover.service
                        sudo systemctl daemon-reload
                        sudo systemctl enable quickstay-resume-recover.service >/dev/null 2>&1 || true
                        log_success "Auto-recuperacion post-resume habilitada (systemd)"
                else
                        log_warning "No se pudo habilitar systemd sin password sudo."
                        log_info "   Ejecuta manualmente:"
                        log_info "   sudo cp deploy/systemd/quickstay-resume-recover.service /etc/systemd/system/"
                        log_info "   sudo systemctl daemon-reload"
                        log_info "   sudo systemctl enable quickstay-resume-recover.service"
                fi
        else
                log_warning "systemd no disponible. El script recover-after-resume.sh queda listo para ejecucion manual."
        fi
}

test_file() {
    local file=$1
    if [ -f "$file" ]; then
        return 0
    else
        log_error "Archivo no encontrado: $file"
        return 1
    fi
}

test_dir() {
    local dir=$1
    if [ -d "$dir" ]; then
        return 0
    else
        log_error "Directorio no encontrado: $dir"
        return 1
    fi
}

# Banner
clear
cat << "EOF"
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║   ██████╗ ██╗   ██╗██╗ ██████╗██╗  ██╗███████╗████████╗ █████╗██╗   ██╗
║  ██╔═══██╗██║   ██║██║██╔════╝██║ ██╔╝██╔════╝╚══██╔══╝██╔══██╗╚██╗ ██╔╝
║  ██║   ██║██║   ██║██║██║     █████╔╝ ███████╗   ██║   ███████║ ╚████╔╝ 
║  ██║▄▄ ██║██║   ██║██║██║     ██╔═██╗ ╚════██║   ██║   ██╔══██║  ╚██╔╝  
║  ╚██████╔╝╚██████╔╝██║╚██████╗██║  ██╗███████║   ██║   ██║  ██║   ██║   
║   ╚══▀▀═╝  ╚═════╝ ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝   ╚═╝   
║                                                               ║
║           DESPLIEGUE MAESTRO DE INFRAESTRUCTURA (v2)          ║
║              Validaciones Extensivas Incluidas                ║
║                    Proyecto ASIR 2026                         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF

log_info "Iniciando despliegue de QuickStay con validaciones..."
echo ""

maybe_offer_factory_reset
echo ""

# ============================================================================
# FASE 1: PRE-VALIDACIÓN DE ESTRUCTURA
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 1: PRE-VALIDACIÓN DE ESTRUCTURA"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

set +e

log_info "Validando directorios críticos..."
if test_dir "Documentación/app_repo"; then
    log_success "Directorio Java"
fi
if test_dir "Documentación/dashboard-v2"; then
    log_success "Directorio Dashboard"
fi
if test_dir "Documentación/iot"; then
    log_success "Directorio IoT"
fi
if test_dir "infrastructure"; then
    log_success "Directorio Infrastructure"
fi

echo ""
log_info "Validando archivos críticos..."
test_file "docker-compose.yml" && log_success "docker-compose.yml" || exit 1
test_file "secrets.env" && log_success "secrets.env" || log_warning "secrets.env (valores por defecto)"
test_file "deploy-all.sh" && log_success "deploy-all.sh" || log_error "Script maestro"

echo ""

set -e

# ============================================================================
# FASE 2: VALIDACIÓN DE CÓDIGO FUENTE
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 2: VALIDACIÓN DE CÓDIGO FUENTE"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

# Java
log_info "Analizando código Java..."
if test_file "Documentación/app_repo/pom.xml"; then
    log_info "   Verificando sintaxis XML..."
    if command -v xmllint &> /dev/null; then
        if xmllint --noout "Documentación/app_repo/pom.xml" 2>/dev/null; then
            log_success "   pom.xml sintaxis válida"
        else
            log_warning "   pom.xml tiene advertencias XML"
        fi
    else
        log_debug "   xmllint no instalado, saltando validación"
    fi
    
    if test_file "Documentación/app_repo/src/main/java/ProyectoFinal/Servidor/Servidor.java"; then
        log_success "   Servidor.java encontrado"
        
        # Verificar clase principal
        if grep -q "public static void main" "Documentación/app_repo/src/main/java/ProyectoFinal/Servidor/Servidor.java"; then
            log_success "   Método main encontrado"
        else
            log_warning "   Método main no encontrado"
        fi
    else
        log_error "   Servidor.java no encontrado"
    fi
    
    # Verificar target/classes (compilado)
    if [ -d "Documentación/app_repo/target" ]; then
        log_success "   Carpeta target existe (código compilado)"
    else
        log_warning "   Carpeta target no existe (necesitará compilarse)"
    fi
else
    log_warning "   pom.xml no encontrado"
fi

echo ""

# Python IoT
log_info "Analizando Python IoT..."
set +e
if test_file "Documentación/iot/iot_client.py"; then
    if command -v python3 &> /dev/null; then
        log_info "   Verificando sintaxis Python..."
        if python3 -m py_compile "Documentación/iot/iot_client.py" 2>/dev/null; then
            log_success "   iot_client.py sintaxis válida"
        else
            log_warning "   iot_client.py tiene errores de sintaxis (no bloqueante para el despliegue)"
        fi
        
        # Verificar imports
        if python3 << 'PYEOF' 2>/dev/null
import ast
with open("Documentación/iot/iot_client.py") as f:
    ast.parse(f.read())
PYEOF
        then
            log_success "   Imports de Python válidos"
        else
            log_warning "   Imports de Python con posibles problemas (no bloqueante)"
        fi
    else
        log_warning "   Python3 no instalado, saltando validación"
    fi
    
    # Verificar dependencias
    if test_file "Documentación/iot/Dockerfile"; then
        if grep -q "paho-mqtt" "Documentación/iot/Dockerfile"; then
            log_success "   Dependencias MQTT definidas"
        fi
    fi
else
    log_warning "   iot_client.py no encontrado"
fi
set -e

echo ""

# Dashboard Node.js / FastAPI
log_info "Analizando Dashboard (React + FastAPI)..."

# Validación package.json
if test_file "Documentación/dashboard-v2/frontend/package.json"; then
    if command -v python3 &> /dev/null; then
        if python3 -m json.tool "Documentación/dashboard-v2/frontend/package.json" > /dev/null 2>&1; then
            log_success "   package.json válido"
        else
            log_warning "   package.json tiene posibles errores JSON (no bloqueante)"
        fi
    fi
fi

# Validación requirements.txt
if test_file "Documentación/dashboard-v2/backend/requirements.txt"; then
    log_success "   requirements.txt (FastAPI) encontrado"
    
    # Verificar dependencias críticas
    if grep -q "fastapi\|uvicorn" "Documentación/dashboard-v2/backend/requirements.txt"; then
        log_success "   FastAPI y uvicorn definidos"
    else
        log_warning "   FastAPI o uvicorn no encontrados en requirements.txt"
    fi
fi

# Validación sintaxis main.py (no bloqueante)
set +e
if test_file "Documentación/dashboard-v2/backend/main.py"; then
    if command -v python3 &> /dev/null; then
        if python3 -m py_compile "Documentación/dashboard-v2/backend/main.py" 2>/dev/null; then
            log_success "   main.py (FastAPI) sintaxis válida"
        else
            log_warning "   main.py tiene errores de sintaxis (no bloqueante para el despliegue)"
        fi
    fi
fi
set -e

echo ""

# Base de datos
log_info "Analizando Base de Datos..."
if test_file "Documentación/infrastructure/db/init.sql"; then
    log_info "   Verificando estructura SQL..."
    
    # Contar tablas
    table_count=$(grep -c "CREATE TABLE" "Documentación/infrastructure/db/init.sql" || echo 0)
    if [ "$table_count" -gt 0 ]; then
        log_success "   $table_count tablas SQL definidas"
    else
        log_warning "   No se encontraron definiciones de tabla"
    fi
    
    # Verificar tabla principal
    if grep -q "CREATE TABLE.*usuario" "Documentación/infrastructure/db/init.sql"; then
        log_success "   Tabla 'usuario' encontrada"
    fi
    
    if grep -q "CREATE TABLE.*propiedad" "Documentación/infrastructure/db/init.sql"; then
        log_success "   Tabla 'propiedad' encontrada"
    fi
else
    log_warning "   init.sql no encontrado"
fi

echo ""

# ============================================================================
# FASE 3: VALIDACIÓN DE CONFIGURACIÓN
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 3: VALIDACIÓN DE CONFIGURACIÓN"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Validando secrets.env..."
if [ -f "secrets.env" ]; then
    # Contar variables críticas
    required_vars=("MYSQL_PASSWORD" "AD_ADMIN_PASSWORD" "MQTT_PASSWORD" "WG_SERVER_PORT")
    for var in "${required_vars[@]}"; do
        if grep -q "^$var=" "secrets.env"; then
            log_success "   $var definida"
        else
            log_warning "   $var no definida"
        fi
    done
fi

echo ""

log_info "Validando docker-compose.yml..."
if test_file "docker-compose.yml"; then
    auto_pin_latest_images

    # Contar servicios
    service_count=$(grep -c "^  [a-z].*:$" "docker-compose.yml" || echo 0)
    log_success "   $service_count servicios definidos"
    
    # Verificar servicios críticos
    critical_services=("router-fw" "mysql-db" "app-server" "ad-dc-primary")
    for service in "${critical_services[@]}"; do
        if grep -q "^  $service:" "docker-compose.yml"; then
            log_success "   Servicio '$service' definido"
        else
            log_error "   Servicio '$service' NO encontrado"
        fi
    done
fi

echo ""

# ============================================================================
# FASE 4: VERIFICACIÓN DE REQUISITOS DEL SISTEMA
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 4: VERIFICACIÓN DE REQUISITOS DEL SISTEMA"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Verificando sistema operativo..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    log_success "Linux detectado"
    
    # Detectar distribución
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        log_info "   Distribución: $PRETTY_NAME"
    fi
else
    log_error "Este script solo funciona en Linux"
    exit 1
fi

echo ""

log_info "Verificando Docker..."
if ! command -v docker &> /dev/null; then
    log_warning "Docker no está instalado"
    
    read -p "¿Instalar Docker ahora? (s/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        log_info "Instalando Docker..."
        
        sudo apt-get update -qq
        sudo apt-get install -y -qq \
            apt-transport-https \
            ca-certificates \
            curl \
            gnupg \
            lsb-release
        
        sudo mkdir -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
        
        sudo apt-get update -qq
        sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        
        sudo usermod -aG docker $USER
        
        log_success "Docker instalado"
        log_warning "IMPORTANTE: Ejecuta 'newgrp docker' para aplicar permisos"
    else
        log_error "Docker es requerido"
        exit 1
    fi
else
    log_success "Docker instalado: $(docker --version)"
fi

echo ""

log_info "Verificando Docker Compose..."
if docker compose version &> /dev/null; then
    log_success "Docker Compose V2: $(docker compose version --short)"
elif command -v docker-compose &> /dev/null; then
    log_success "Docker Compose V1: $(docker-compose --version)"
else
    log_error "Docker Compose no encontrado"
    exit 1
fi

echo ""

log_info "Verificando daemon Docker..."
if docker ps > /dev/null 2>&1; then
    log_success "Docker daemon funciona"
    log_info "   Imágenes presentes: $(docker images --quiet | wc -l)"
    log_info "   Contenedores: $(docker ps -a --quiet | wc -l)"
else
    log_error "No se puede conectar a Docker daemon"
    exit 1
fi

echo ""

log_info "Verificando recursos del sistema..."
total_mem=$(free -h | grep "^Mem:" | awk '{print $2}')
log_info "   Memoria total: $total_mem"

cpu_cores=$(nproc)
log_info "   CPU cores: $cpu_cores"

available_disk=$(df -h / | tail -1 | awk '{print $4}')
log_info "   Espacio disponible: $available_disk"

# Advertencia si recursos bajos
if [ "$cpu_cores" -lt 4 ]; then
    log_warning "CPU: Se recomienda mínimo 4 cores (tienes $cpu_cores)"
fi

echo ""

# ============================================================================
# FASE 5: RESUMEN PRE-DESPLIEGUE
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 5: RESUMEN PRE-DESPLIEGUE"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

echo "Checks completados:"
echo -e "  ${GREEN}Pasados:${NC}      $CHECKS_PASSED"
echo -e "  ${YELLOW}Advertencias:${NC} $WARNINGS"
echo -e "  ${RED}Errores:${NC}       $ERRORS"
echo ""

if [ $ERRORS -gt 0 ]; then
    log_error "Se encontraron $ERRORS problemas críticos"
    log_warning "Continuando despliegue automático (modo no interactivo)..."
    # read -p "¿Continuar de todas formas? (s/N): " -n 1 -r
    # echo
    # if ! [[ $REPLY =~ ^[Ss]$ ]]; then
    #     exit 1
    # fi
fi

if [ $WARNINGS -gt 0 ]; then
    log_warning "Hay $WARNINGS advertencias (pero puede continuar)"
fi

echo ""
# read -p "Presiona Enter para continuar con el despliegue..."
echo ""

# ============================================================================
# FASE 6: PREPARACIÓN DE DIRECTORIOS
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 6: PREPARACIÓN DE DIRECTORIOS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Creando estructura de directorios..."

mkdir -p logs/{router,haproxy,web1,web2,app,mysql,ad-dc,zabbix,wazuh,mqtt,dashboard,deploy}
mkdir -p wireguard/{config,keys}
mkdir -p ssh/keys
mkdir -p backups
mkdir -p data/{mysql,zabbix,wazuh}

chmod 600 secrets.env
ensure_lab_zabbix_agent_defaults
load_secrets_env
log_success "Directorios creados"

echo ""

# ============================================================================
# FASE 7: GENERAR CONTRASEÑAS MOSQUITTO
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 7: CONFIGURACIÓN DE SERVICIOS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Generando configuración de Mosquitto..."

if [ ! -f "infrastructure/config/mosquitto/passwd" ]; then
    source secrets.env
    
    docker run --rm -v "$(pwd)/infrastructure/config/mosquitto:/mosquitto/config" \
        eclipse-mosquitto:2.0 \
        sh -c "mosquitto_passwd -c -b /mosquitto/config/passwd ${MQTT_USERNAME:-quickstay_iot} ${MQTT_PASSWORD:-QuickStay2026!MQTT}" 2>/dev/null || true
    
    log_success "Contraseñas de Mosquitto generadas"
else
    log_success "Archivo de contraseñas de Mosquitto ya existe"
fi

# Asegurar permisos de mosquitto para que el contenedor pueda leerlos
docker run --rm -v "$(pwd)/infrastructure/config/mosquitto:/mosquitto/config" alpine chmod 644 /mosquitto/config/passwd /mosquitto/config/mosquitto.conf 2>/dev/null || true

echo ""

# ============================================================================
# FASE 8: LIMPIAR CONTENEDORES PREVIOS (OPCIONAL)
# ============================================================================
log_info "Saltando limpieza de contenedores (modo automático)..."
# read -p "Respuesta: " -n 1 -r
# echo
# if [[ $REPLY =~ ^[Ss]$ ]]; then
#     log_warning "Deteniendo y eliminando contenedores existentes..."
#     docker-compose down -v 2>/dev/null || true
#     log_success "Limpieza completada"
# fi

echo ""

# ============================================================================
# FASE 9: CONSTRUIR IMÁGENES DOCKER
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 9: CONSTRUCCIÓN DE IMÁGENES DOCKER"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_warning "Esto puede tardar 15-30 minutos en la primera ejecución..."
log_info "Construyendo imágenes Docker en paralelo..."

if ! ensure_build_disk_headroom; then
    log_info "Ver detalles de limpieza en: logs/deploy/prebuild-cleanup.log"
    exit 1
fi

prefetch_base_images

if run_compose_build; then
    build_exit=0
else
    build_exit=$?
fi

if [ "$build_exit" -eq 0 ]; then
    log_success "Imágenes construidas correctamente"
else
    if grep -Eiq 'no space left on device|ENOSPC' "logs/deploy/build.log"; then
        log_warning "Build falló por falta de espacio (ENOSPC). Intentando limpieza y reintento único..."
        docker_cleanup_for_build

        if run_compose_build; then
            log_success "Imágenes construidas correctamente tras limpieza automática"
            build_exit=0
        else
            build_exit=$?
        fi
    fi

    if [ "$build_exit" -ne 0 ] && is_transient_network_build_failure; then
        net_retries="${DEPLOY_BUILD_NET_RETRIES:-2}"
        net_attempt=1
        net_delay="${DEPLOY_BUILD_NET_DELAY_SEC:-15}"
        log_warning "Build falló por red transitoria. Se intentará reintento automático (${net_retries} intentos)..."

        while [ "$net_attempt" -le "$net_retries" ] && [ "$build_exit" -ne 0 ]; do
            log_info "Reintento de build por red (${net_attempt}/${net_retries})..."
            sleep "$net_delay"
            prefetch_base_images
            if run_compose_build; then
                log_success "Imágenes construidas correctamente tras reintento de red"
                build_exit=0
                break
            else
                build_exit=$?
            fi
            net_attempt=$((net_attempt + 1))
        done
    fi

    if [ "$build_exit" -ne 0 ]; then
        log_error "Error al construir imágenes"
        log_info "Ver logs en: logs/deploy/build.log"
        log_info "Ver limpieza en: logs/deploy/prebuild-cleanup.log"
        log_info "Ver prefetch en: logs/deploy/prefetch-base-images.log"
        exit 1
    fi
fi

echo ""

# ============================================================================
# FASE 10: DESPLEGAR INFRAESTRUCTURA
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 10: DESPLIEGUE DE CONTENEDORES"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Desplegando contenedores..."

prefetch_runtime_images

if run_compose_up; then
    up_exit=0
else
    up_exit=$?
fi

if [ "$up_exit" -ne 0 ] && is_transient_network_up_failure; then
    up_net_retries="${DEPLOY_UP_NET_RETRIES:-2}"
    up_net_delay="${DEPLOY_UP_NET_DELAY_SEC:-15}"
    up_try=1
    log_warning "docker-compose up falló por red transitoria. Reintentando (${up_net_retries} intentos)..."

    while [ "$up_try" -le "$up_net_retries" ] && [ "$up_exit" -ne 0 ]; do
        sleep "$up_net_delay"
        prefetch_runtime_images
        if run_compose_up; then
            up_exit=0
            break
        fi
        up_exit=$?
        up_try=$((up_try + 1))
    done
fi

if [ "$up_exit" -eq 0 ]; then
    log_success "Contenedores iniciados"
else
    log_error "Error al desplegar contenedores"
    log_info "Ver logs en: logs/deploy/startup.log"
    log_info "Ver prefetch runtime en: logs/deploy/prefetch-runtime-images.log"
    exit 1
fi

echo ""

# ============================================================================
# FASE 11: ESPERAR Y VALIDAR SERVICIOS
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 11: VALIDACIÓN DE SERVICIOS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Esperando a que los servicios estén listos..."

wait_for_service() {
    local service=$1
    local max_wait=${2:-90}
    local counter=0
    
    while ! compose --env-file secrets.env ps | grep -q "$service.*Up"; do
        sleep 2
        counter=$((counter + 2))
        if [ $counter -ge $max_wait ]; then
            log_warning "$service timeout (>$max_wait segundos)"
            return 1
        fi
        echo -n "."
    done
    echo ""
    log_success "$service online"
}

seed_ad_dns_records() {
    if ! compose --env-file secrets.env ps | grep -q "ad-dc-primary.*Up"; then
        log_warning "ad-dc-primary no está operativo; se omite carga de registros DNS"
        return 0
    fi

    local ad_container="quickstay-ad-dc1"
    local ad_timeout=12

    if ! docker ps --format '{{.Names}}' | grep -qx "$ad_container"; then
        log_warning "Contenedor AD no encontrado ($ad_container); se omite carga de registros DNS"
        return 0
    fi

    ad_exec_timeout() {
        if command -v timeout >/dev/null 2>&1; then
            timeout "$ad_timeout" docker exec "$ad_container" "$@"
        else
            docker exec "$ad_container" "$@"
        fi
    }

    log_info "Sincronizando registros DNS A en AD (quickstay.local)..."
    log_info "   Contenedor AD: $ad_container | timeout por operacion: ${ad_timeout}s"

    # Smoke test para no entrar en bucle si Samba no responde.
    if ! ad_exec_timeout samba-tool --version >/dev/null 2>&1; then
        log_warning "AD no responde a samba-tool (timeout/error). Se omite sincronización DNS para no bloquear despliegue."
        return 0
    fi

    # Credenciales no interactivas para operaciones DNS RPC.
    local ad_admin_user="Administrator"
    local ad_admin_pass
    ad_admin_pass="$(docker exec "$ad_container" sh -lc 'printf %s "$ADMIN_PASSWORD"' 2>/dev/null || true)"
    if [ -z "$ad_admin_pass" ]; then
        log_warning "No se pudo leer ADMIN_PASSWORD del contenedor AD; se omite sincronización DNS"
        return 0
    fi
    local ad_auth
    ad_auth="${ad_admin_user}%${ad_admin_pass}"

    local total=15
    local idx=0
    local ok=0
    local failed=0
    local started_ts
    started_ts="$(date +%s)"

    add_ad_a_record() {
        local name="$1"
        local ip="$2"
        idx=$((idx + 1))

        log_info "   [$idx/$total] Verificando DNS ${name}.quickstay.local -> ${ip}"

        local exists
        exists="$(ad_exec_timeout samba-tool dns query 127.0.0.1 quickstay.local "$name" A -U "$ad_auth" 2>/dev/null || true)"

        if echo "$exists" | grep -q "$ip"; then
            log_success "   DNS $name -> $ip ya existe"
            ok=$((ok + 1))
            return 0
        fi

        log_info "   [$idx/$total] Añadiendo registro DNS ${name}.quickstay.local"

        if ad_exec_timeout samba-tool dns add 127.0.0.1 quickstay.local "$name" A "$ip" -U "$ad_auth" >/dev/null 2>&1; then
            log_success "   DNS añadido: $name -> $ip"
            ok=$((ok + 1))
        else
            log_warning "   No se pudo añadir DNS $name -> $ip (puede existir con otro valor)"
            failed=$((failed + 1))
        fi
    }

    add_ad_a_record "router-fw" "172.16.20.2"
    add_ad_a_record "lb" "172.16.10.10"
    add_ad_a_record "web1" "172.16.10.20"
    add_ad_a_record "web2" "172.16.10.21"
    add_ad_a_record "app" "172.16.20.10"
    add_ad_a_record "db" "172.16.20.20"
    add_ad_a_record "dc1" "172.16.30.10"
    add_ad_a_record "zabbix" "172.16.30.22"
    add_ad_a_record "zabbix-web" "172.16.30.20"
    add_ad_a_record "wazuh" "172.16.30.30"
    add_ad_a_record "wazuh-dashboard" "172.16.30.31"
    add_ad_a_record "wazuh-indexer" "172.16.30.32"
    add_ad_a_record "mqtt" "172.16.40.10"
    add_ad_a_record "grafana" "172.16.30.21"
    add_ad_a_record "dashboard-backend" "172.16.30.40"

    local ended_ts
    ended_ts="$(date +%s)"
    local elapsed=$((ended_ts - started_ts))
    log_info "Sincronización DNS AD finalizada: ok=${ok}, fallidos=${failed}, total=${total}, tiempo=${elapsed}s"
}

run_check_timeout() {
    local timeout_sec="$1"
    shift

    if command -v timeout >/dev/null 2>&1; then
        timeout "$timeout_sec" "$@"
    else
        # Fallback sin dependencia de coreutils timeout.
        "$@" &
        local cmd_pid=$!
        local elapsed=0

        while kill -0 "$cmd_pid" 2>/dev/null; do
            if [ "$elapsed" -ge "$timeout_sec" ]; then
                kill "$cmd_pid" 2>/dev/null || true
                sleep 1
                kill -9 "$cmd_pid" 2>/dev/null || true
                wait "$cmd_pid" 2>/dev/null || true
                return 124
            fi
            sleep 1
            elapsed=$((elapsed + 1))
        done

        wait "$cmd_pid"
    fi
}

# Servicios críticos
wait_for_service "router-fw" 30
wait_for_service "mysql-db" 90
wait_for_service "ad-dc-primary" 120
wait_for_service "mqtt-broker" 30
wait_for_service "app-server" 60
wait_for_service "zabbix-db" 120
wait_for_service "zabbix-server" 120
wait_for_service "zabbix-web" 120

seed_ad_dns_records
generate_default_vpn_peers

sleep 15  # Tiempo extra para estabilización

echo ""

# ============================================================================
# FASE 12: HEALTH CHECKS
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 12: HEALTH CHECKS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Verificando salud de servicios..."

# MySQL
log_info "   Probando MySQL..."
if run_check_timeout 15 docker exec quickstay-mysql sh -lc 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -e "SELECT 1"' > /dev/null 2>&1; then
    log_success "   MySQL responde"
    
    # Verificar base de datos
    log_info "   Verificando esquema humhouse..."
    if run_check_timeout 15 docker exec quickstay-mysql sh -lc 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -e "USE humhouse; SHOW TABLES;"' > /dev/null 2>&1; then
        log_success "   Base de datos 'humhouse' inicializada"
    else
        log_warning "   Base de datos 'humhouse' no accesible"
    fi
else
    log_warning "   MySQL no responde (aún inicializando)"
fi

echo ""

# App Server
log_info "   Probando App Server..."
if run_check_timeout 12 docker exec quickstay-app sh -lc 'test -f /app/app.jar || ls /app/*.jar >/dev/null 2>&1' > /dev/null 2>&1; then
    log_success "   App Server JAR presente"
else
    log_warning "   App Server JAR no encontrado"
fi

echo ""

# Conectividad inter-VLAN
log_info "   Probando conectividad inter-VLAN..."

log_info "   [1/2] DMZ -> App (172.16.10.20 -> 172.16.20.10)"
if run_check_timeout 10 docker exec quickstay-web-1 sh -lc 'php -r '\''$c=@fsockopen("172.16.20.10",1234,$e,$s,2); if($c){fclose($c); exit(0);} exit(1);'\''' > /dev/null 2>&1; then
    log_success "   DMZ → App TCP 1234 OK"
elif run_check_timeout 8 docker exec quickstay-web-1 ping -c 1 -W 2 172.16.20.10 > /dev/null 2>&1; then
    log_success "   DMZ → App ICMP OK"
else
    if [ "${DEPLOY_REQUIRE_DMZ_APP_CONNECTIVITY:-0}" = "1" ]; then
        log_warning "   DMZ → App sin conexión"
    else
        log_info "   DMZ → App no accesible (segmentación entre VLANs; esperado si no hay política de paso)"
    fi
fi

log_info "   [2/2] App -> MySQL (172.16.20.10 -> 172.16.20.20:3306)"
if run_check_timeout 8 docker exec quickstay-app sh -lc "nc -z -w 2 172.16.20.20 3306" > /dev/null 2>&1; then
    log_success "   App → MySQL OK"
else
    log_warning "   App → MySQL sin conexión"
fi

echo ""

# Reintento automatico sobre healthchecks de Docker (sin frenar el arranque).
heal_unhealthy_services 4 20

echo ""

# ============================================================================
# FASE 13: ESTADO FINAL
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 13: ESTADO FINAL"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Estado de contenedores:"
compose --env-file secrets.env ps

echo ""

log_info "Redes Docker:"
docker network ls | grep quickstay || docker network ls | grep proyectofinal

echo ""

# =========================================================================
# FASE 14: AUTO-CONFIGURACIÓN DE MONITOREO (ZABBIX / WAZUH)
# =========================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 14: AUTO-CONFIGURACIÓN DE MONITOREO"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

# 14.1 Agentes Zabbix en máquinas simuladas (contenedores)
if [ -n "${ZABBIX_AGENT_SERVER:-}" ]; then
    log_info "Agentes Zabbix en contenedores habilitados (objetivo: ${ZABBIX_AGENT_SERVER})"
else
    log_warning "ZABBIX_AGENT_SERVER vacio: los agentes de las máquinas simuladas no enviaran métricas"
fi

# 14.2 Instalar Zabbix Agent en el host real (opcional)
if [ "${ENABLE_HOST_ZABBIX_AGENT:-0}" = "1" ] && [ -f "deploy/install-zabbix-agent.sh" ]; then
    log_info "Instalando Zabbix Agent en el host real (opt-in, TLS-PSK)..."
    if [ -f "secrets.env" ]; then
        # Exportar variables esperadas por el instalador.
        set -a
        # shellcheck disable=SC1091
        source secrets.env
        set +a
    fi

    # Endpoint requerido para producción: no usar valores internos de Docker emulado.
    ZABBIX_AGENT_TARGET="${ZABBIX_AGENT_SERVER:-${ZABBIX_SERVER_HOST:-}}"
    if [ -z "$ZABBIX_AGENT_TARGET" ]; then
        log_warning "ZABBIX_AGENT_SERVER/ZABBIX_SERVER_HOST no definido; se omite instalación automática del agente"
        log_info "   Define en secrets.env un endpoint real (IP/FQDN) del servidor Zabbix corporativo"
    elif [[ "$ZABBIX_AGENT_TARGET" =~ ^172\.16\.(10|20|30|40|50)\. ]] && [ "${ZABBIX_ALLOW_INTERNAL_EMULATION:-0}" != "1" ]; then
        log_warning "ZABBIX_AGENT_SERVER apunta a red interna emulada ($ZABBIX_AGENT_TARGET)."
        log_info "   En modo producción este valor no es válido. Usa Zabbix externo o define ZABBIX_ALLOW_INTERNAL_EMULATION=1 solo para laboratorio."
    elif sudo -E bash deploy/install-zabbix-agent.sh "$ZABBIX_AGENT_TARGET" >/dev/null 2>&1; then
        log_success "Zabbix Agent instalado y configurado en el host (endpoint: $ZABBIX_AGENT_TARGET)"
    else
        log_warning "No se pudo instalar/configurar Zabbix Agent de forma automática (revisar permisos/repositorios)"
    fi
elif [ "${ENABLE_HOST_ZABBIX_AGENT:-0}" = "1" ]; then
    log_warning "No se encontró deploy/install-zabbix-agent.sh; se omite instalación automática"
else
    log_info "Host agent deshabilitado (ENABLE_HOST_ZABBIX_AGENT=0)"
fi

echo ""

# 14.3 Auto-provisión de hosts en Zabbix usando config.yaml (Dashboard v2)
if command -v python3 >/dev/null 2>&1; then
    # Asegurar que PyYAML está disponible para el script de provisión
    if ! python3 -c "import yaml" >/dev/null 2>&1; then
        log_info "Instalando módulo python3-yaml para auto-configuración de Zabbix..."
        if sudo apt-get update -qq && sudo apt-get install -y -qq python3-yaml; then
            log_success "python3-yaml instalado correctamente"
        else
            log_warning "No se pudo instalar python3-yaml; se omite auto-configuración de Zabbix"
        fi
    fi

    if python3 -c "import yaml" >/dev/null 2>&1; then
        if [ -f "monitoring/provision_zabbix.py" ]; then
            log_info "Auto-configurando hosts en Zabbix desde config.yaml..."
            zbx_provision_retries="${ZABBIX_PROVISION_RETRIES:-6}"
            zbx_provision_delay="${ZABBIX_PROVISION_DELAY_SEC:-10}"
            zbx_ok=0

            for zbx_try in $(seq 1 "$zbx_provision_retries"); do
                if python3 monitoring/provision_zabbix.py; then
                    zbx_ok=1
                    break
                fi

                if [ "$zbx_try" -lt "$zbx_provision_retries" ]; then
                    log_warning "Zabbix API no lista (intento ${zbx_try}/${zbx_provision_retries}); reintentando en ${zbx_provision_delay}s..."
                    sleep "$zbx_provision_delay"
                fi
            done

            if [ "$zbx_ok" -eq 1 ]; then
                log_success "Auto-configuración de Zabbix completada"
            else
                if [ "${ZABBIX_STRICT_PROVISION:-1}" = "1" ]; then
                    log_error "La auto-configuración de Zabbix falló tras ${zbx_provision_retries} intentos (modo estricto activo)"
                    exit 1
                fi

                log_warning "La auto-configuración de Zabbix falló tras ${zbx_provision_retries} intentos (ver mensajes anteriores)"
            fi
        else
            log_warning "monitoring/provision_zabbix.py no encontrado; se omite auto-configuración de Zabbix"
        fi
    fi
else
    log_warning "Python3 no disponible; se omite auto-configuración de Zabbix"
fi

echo ""

# 14.4 Agente Wazuh en host real (opt-in). En esta maqueta va en máquinas simuladas.
if [ "${ENABLE_HOST_WAZUH_AGENT:-0}" = "1" ]; then
    if [ -f "Documentación/infrastructure/scripts/14_deploy_security.sh" ]; then
        log_info "Instalando agente Wazuh en el host real (opt-in)..."
        if sudo bash Documentación/infrastructure/scripts/14_deploy_security.sh >/dev/null 2>&1; then
            log_success "Agente Wazuh instalado/configurado en el host (opt-in)"
        else
            log_warning "No se pudo instalar agente Wazuh en host automáticamente (puede requerir ajustes manuales)"
        fi
    else
        log_warning "Script 14_deploy_security.sh no encontrado; se omite instalación de agente Wazuh en host"
    fi
else
    log_info "Host Wazuh agent deshabilitado (ENABLE_HOST_WAZUH_AGENT=0). Se asume despliegue en máquinas simuladas."
fi

echo ""

# ============================================================================
# FASE 15: RESILIENCIA POST-SUSPENSION
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 15: RESILIENCIA POST-SUSPENSION"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

setup_resume_recovery

echo ""

# ============================================================================
# RESUMEN FINAL
# ============================================================================
echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                   DESPLIEGUE COMPLETADO                       ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

log_success "Infraestructura QuickStay desplegada exitosamente"

# Cargar variables para info final
source secrets.env

cat << EOF

${GREEN}ACCESO INMEDIATO:${NC}
  • QuickStay Web:   http://localhost
  • HAProxy Stats:   http://localhost:8404

${GREEN}ACCESO VPN (Administradores):${NC}
    1. Descarga: ./wireguard/config/peers/peer1/peer1.conf
    2. Conecta: sudo wg-quick up ./wireguard/config/peers/peer1/peer1.conf
  3. Accede a servicios internos (172.16.x.x)

${GREEN}SERVICIOS INTERNOS:${NC}
    • Zabbix:      http://172.16.30.20:8080
  • Grafana:     http://172.16.30.21:3000
  • Wazuh:       https://172.16.30.30:5601
  • App Java:    http://172.16.20.10:1234
  • MySQL:       172.16.20.20:3306

${GREEN}LOGS Y MONITOREO:${NC}
  • docker-compose ps
  • docker-compose logs -f [servicio]
  • ./logs/[servicio]/
  • ./logs/deploy/build.log
  • ./logs/deploy/startup.log

${GREEN}SIGUIENTES PASOS:${NC}
  1. Revisar logs: tail -f logs/*/
  2. Conectar VPN WireGuard
  3. Acceder a Zabbix/Grafana para monitoreo
  4. Ejecutar: ./verify.sh

${YELLOW}DOCUMENTACIÓN:${NC}
  • README_DESPLIEGUE.md
  • QUICKSTART.md

${MAGENTA}Fecha: $(date)${NC}

EOF

log_success "¡Despliegue finalizado!"
echo ""
