#!/usr/bin/env bash
# shellcheck disable=SC2034
set -euo pipefail

# QuickStay production-style Zabbix agent bootstrap.
# Idempotent install/config for Debian/Ubuntu hosts.

ZBX_SERVER="${ZABBIX_AGENT_SERVER:-${1:-}}"
ZBX_ACTIVE_SERVER="${ZABBIX_AGENT_ACTIVE_SERVER:-$ZBX_SERVER}"
HOST_BASE="${ZABBIX_AGENT_HOSTNAME:-$(hostname -s)}"
HOST_SUFFIX="${ZABBIX_AGENT_HOSTNAME_SUFFIX:--hypervisor}"
ZBX_HOSTNAME="${HOST_BASE}${HOST_SUFFIX}"
ZBX_METADATA="${ZABBIX_AGENT_METADATA:-quickstay-linux}"
PSK_IDENTITY="${ZABBIX_AGENT_TLS_IDENTITY:-${ZBX_HOSTNAME}-psk}"
PSK_FILE="${ZABBIX_AGENT_PSK_FILE:-/etc/zabbix/zabbix_agent2.psk}"
PSK_VALUE="${ZABBIX_AGENT_PSK:-}"
CONF_FILE="/etc/zabbix/zabbix_agent2.d/quickstay-production.conf"
ALLOW_INTERNAL_EMULATION="${ZABBIX_ALLOW_INTERNAL_EMULATION:-0}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok() { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_err() { echo -e "${RED}[ERR]${NC} $1"; }

require_root() {
  if [[ "${EUID}" -ne 0 ]]; then
    log_err "Este script requiere root (usa sudo)."
    exit 1
  fi
}

validate_target() {
  if [[ -z "$ZBX_SERVER" ]]; then
    log_err "Debes definir ZABBIX_AGENT_SERVER (o pasarlo como argumento)."
    exit 1
  fi

  # En modo produccion no permitimos apuntar al rango interno emulado en Docker.
  if [[ "$ALLOW_INTERNAL_EMULATION" != "1" ]] && [[ "$ZBX_SERVER" =~ ^172\.16\.(10|20|30|40|50)\. ]]; then
    log_err "ZABBIX_AGENT_SERVER=$ZBX_SERVER parece una IP interna emulada (Docker)."
    log_err "Define un endpoint de Zabbix de infraestructura real (IP/FQDN corporativo)."
    log_info "Si estas en laboratorio y quieres permitirlo, exporta ZABBIX_ALLOW_INTERNAL_EMULATION=1"
    exit 1
  fi
}

install_agent2() {
  export DEBIAN_FRONTEND=noninteractive
  apt-get update -qq

  if apt-cache show zabbix-agent2 >/dev/null 2>&1; then
    apt-get install -y -qq zabbix-agent2 zabbix-sender
    log_ok "zabbix-agent2 instalado desde repositorio del sistema"
    return
  fi

  log_warn "No se encontró zabbix-agent2 en repositorio actual, instalando zabbix-agent como fallback"
  apt-get install -y -qq zabbix-agent zabbix-sender
  log_ok "zabbix-agent instalado (fallback)"
}

generate_or_store_psk() {
  mkdir -p "$(dirname "$PSK_FILE")"

  if [[ -n "$PSK_VALUE" ]]; then
    printf '%s' "$PSK_VALUE" > "$PSK_FILE"
  elif [[ ! -s "$PSK_FILE" ]]; then
    # 32 bytes = 64 hex chars.
    head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n' > "$PSK_FILE"
  fi

  chmod 600 "$PSK_FILE"
  if id zabbix >/dev/null 2>&1; then
    chown root:zabbix "$PSK_FILE"
  fi

  log_ok "PSK preparado en $PSK_FILE"
}

write_agent2_config() {
  cat > "$CONF_FILE" <<EOF_CONF
# Managed by QuickStay deploy/install-zabbix-agent.sh
Server=${ZBX_SERVER}
ServerActive=${ZBX_ACTIVE_SERVER}
Hostname=${ZBX_HOSTNAME}
HostMetadata=${ZBX_METADATA}

# TLS-PSK for production-grade encrypted channel
TLSConnect=psk
TLSAccept=psk
TLSPSKIdentity=${PSK_IDENTITY}
TLSPSKFile=${PSK_FILE}

# Agent behavior
Timeout=10
AllowKey=system.run[*]
DenyKey=system.run[powershell*]
EOF_CONF

  chmod 640 "$CONF_FILE"
  if id zabbix >/dev/null 2>&1; then
    chown root:zabbix "$CONF_FILE"
  fi

  log_ok "Configuración escrita en $CONF_FILE"
}

restart_service() {
  if systemctl list-unit-files | grep -q '^zabbix-agent2.service'; then
    systemctl enable --now zabbix-agent2
    systemctl restart zabbix-agent2
    log_ok "Servicio zabbix-agent2 activo"
    return
  fi

  if systemctl list-unit-files | grep -q '^zabbix-agent.service'; then
    local fallback_conf="/etc/zabbix/zabbix_agentd.conf"
    cp "$fallback_conf" "${fallback_conf}.bak.quickstay.$(date +%s)"
    sed -i "s|^Server=.*|Server=${ZBX_SERVER}|" "$fallback_conf"
    sed -i "s|^ServerActive=.*|ServerActive=${ZBX_ACTIVE_SERVER}|" "$fallback_conf"
    sed -i "s|^Hostname=.*|Hostname=${ZBX_HOSTNAME}|" "$fallback_conf"

    if ! grep -q '^HostMetadata=' "$fallback_conf"; then
      echo "HostMetadata=${ZBX_METADATA}" >> "$fallback_conf"
    else
      sed -i "s|^HostMetadata=.*|HostMetadata=${ZBX_METADATA}|" "$fallback_conf"
    fi

    if ! grep -q '^TLSConnect=' "$fallback_conf"; then
      cat >> "$fallback_conf" <<EOF_TLS
TLSConnect=psk
TLSAccept=psk
TLSPSKIdentity=${PSK_IDENTITY}
TLSPSKFile=${PSK_FILE}
EOF_TLS
    else
      sed -i "s|^TLSConnect=.*|TLSConnect=psk|" "$fallback_conf"
      sed -i "s|^TLSAccept=.*|TLSAccept=psk|" "$fallback_conf"
      sed -i "s|^TLSPSKIdentity=.*|TLSPSKIdentity=${PSK_IDENTITY}|" "$fallback_conf"
      sed -i "s|^TLSPSKFile=.*|TLSPSKFile=${PSK_FILE}|" "$fallback_conf"
    fi

    systemctl enable --now zabbix-agent
    systemctl restart zabbix-agent
    log_ok "Servicio zabbix-agent activo (fallback)"
    return
  fi

  log_err "No se encontró servicio zabbix-agent2 ni zabbix-agent"
  exit 1
}

print_summary() {
  echo
  log_ok "Bootstrap Zabbix Agent completado"
  echo "  Server:       $ZBX_SERVER"
  echo "  ServerActive: $ZBX_ACTIVE_SERVER"
  echo "  Hostname:     $ZBX_HOSTNAME"
  echo "  Metadata:     $ZBX_METADATA"
  echo "  PSK Identity: $PSK_IDENTITY"
  echo "  PSK File:     $PSK_FILE"
  echo
  log_info "Siguiente paso en Zabbix: crear Action de autoregistro por HostMetadata='${ZBX_METADATA}'"
}

main() {
  require_root
  validate_target
  install_agent2
  generate_or_store_psk
  write_agent2_config
  restart_service
  print_summary
}

main "$@"
