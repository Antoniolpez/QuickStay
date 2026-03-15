#!/bin/bash
set -e

start_zabbix_agent() {
  if [ "${ZABBIX_AGENT_ENABLE:-1}" != "1" ]; then
    return 0
  fi

  if ! command -v zabbix_agentd >/dev/null 2>&1; then
    return 0
  fi

  if [ -z "${ZABBIX_AGENT_SERVER:-}" ]; then
    echo "[WARN] ZABBIX_AGENT_SERVER vacio; se omite agente en mqtt"
    return 0
  fi

  mkdir -p /etc/zabbix /var/log/zabbix /run/zabbix
  local host="${ZABBIX_AGENT_HOSTNAME:-$(hostname)}"
  local active="${ZABBIX_AGENT_ACTIVE_SERVER:-${ZABBIX_AGENT_SERVER}}"
  local meta="${ZABBIX_AGENT_METADATA:-quickstay-mqtt}"
  local psk_file="${ZABBIX_AGENT_PSK_FILE:-/etc/zabbix/zabbix_agentd.psk}"
  local psk_identity="${ZABBIX_AGENT_TLS_IDENTITY:-${host}-psk}"

  if [ -n "${ZABBIX_AGENT_PSK:-}" ]; then
    echo -n "${ZABBIX_AGENT_PSK}" >"${psk_file}"
  elif [ ! -s "${psk_file}" ]; then
    openssl rand -hex 32 >"${psk_file}"
  fi
  chmod 600 "${psk_file}" || true

  cat >/etc/zabbix/zabbix_agentd.conf <<EOF
PidFile=/tmp/zabbix_agentd.pid
LogFile=/tmp/zabbix_agentd.log
LogFileSize=5
Server=${ZABBIX_AGENT_SERVER}
ServerActive=${active}
Hostname=${host}
HostMetadata=${meta}
Timeout=10
TLSConnect=unencrypted
TLSAccept=unencrypted
EOF

  zabbix_agentd -c /etc/zabbix/zabbix_agentd.conf || echo "[WARN] no se pudo iniciar zabbix_agentd en mqtt"
}

start_zabbix_agent
exec "$@"
