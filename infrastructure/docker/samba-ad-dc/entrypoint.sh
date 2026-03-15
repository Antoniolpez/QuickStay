#!/bin/bash
set -e

echo "==> Iniciando Samba AD-DC para QuickStay"

# Variables de entorno
DOMAIN=${DOMAIN:-QUICKSTAY}
REALM=${REALM:-QUICKSTAY.LOCAL}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-QuickStay2026!}
DNS_FORWARDER=${DNS_FORWARDER:-8.8.8.8}

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
