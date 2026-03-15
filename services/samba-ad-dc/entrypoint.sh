#!/bin/bash
set -e

echo "==> Iniciando Samba AD-DC para QuickStay"

# Variables de entorno
DOMAIN=${DOMAIN:-QUICKSTAY}
REALM=${REALM:-QUICKSTAY.LOCAL}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-QuickStay2026!}
DNS_FORWARDER=${DNS_FORWARDER:-8.8.8.8}

start_zabbix_agent() {
    if [ "${ZABBIX_AGENT_ENABLE:-1}" != "1" ]; then
        return 0
    fi

    if ! command -v zabbix_agentd >/dev/null 2>&1; then
        return 0
    fi

    if [ -z "${ZABBIX_AGENT_SERVER:-}" ]; then
        echo "[WARN] ZABBIX_AGENT_SERVER vacio; se omite agente en ad-dc"
        return 0
    fi

    mkdir -p /etc/zabbix /var/log/zabbix /run/zabbix
    local host="${ZABBIX_AGENT_HOSTNAME:-$(hostname)}"
    local active="${ZABBIX_AGENT_ACTIVE_SERVER:-${ZABBIX_AGENT_SERVER}}"
    local meta="${ZABBIX_AGENT_METADATA:-quickstay-ad-dc}"
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

    zabbix_agentd -c /etc/zabbix/zabbix_agentd.conf || echo "[WARN] no se pudo iniciar zabbix_agentd en ad-dc"
}

start_zabbix_agent

# Verificar si ya está provisionado
if [ ! -f /var/lib/samba/private/sam.ldb ]; then
    echo "==> Provisionando nuevo dominio AD-DC: $REALM"
    
    # Eliminar configuración existente
    rm -f /etc/samba/smb.conf
    rm -rf /var/lib/samba/*
    
    # Provisionar el dominio
    samba-tool domain provision \
        --realm="$REALM" \
        --domain="$DOMAIN" \
        --adminpass="$ADMIN_PASSWORD" \
        --server-role=dc \
        --dns-backend=SAMBA_INTERNAL \
        --use-rfc2307 \
        --function-level=2008_R2
    
    # Copiar configuración de Kerberos
    cp /var/lib/samba/private/krb5.conf /etc/krb5.conf
    
    # Configurar DNS forwarder
    if [ -n "$DNS_FORWARDER" ]; then
        echo "==> Configurando DNS forwarder: $DNS_FORWARDER"
        sed -i "s/dns forwarder = .*/dns forwarder = $DNS_FORWARDER/" /etc/samba/smb.conf
    fi
    
    echo "==> Dominio provisionado correctamente"
else
    echo "==> Dominio ya existe, iniciando servicios"
fi

# Deshabilitar servicios que interfieren con Samba AD-DC
systemctl stop smbd nmbd winbind 2>/dev/null || true
systemctl disable smbd nmbd winbind 2>/dev/null || true

# Iniciar Samba AD-DC
echo "==> Iniciando Samba AD-DC"
samba -i --option="server role = active directory domain controller"
