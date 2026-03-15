#!/bin/bash
# 07_deploy_ad.sh
# Installs and Provisions Samba AD-DC for QuickStay (Fase 2 / Guia 4)
# Usage: sudo ./07_deploy_ad.sh

DOMAIN="QUICKSTAY"
REALM="QUICKSTAY.LOCAL"
ADMIN_PASS="TuContraseñaSegura" # As per Guide 4
NET_IP="172.16.30.10"

echo "Installing Samba AD-DC dependencies..."
export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y samba krb5-user krb5-config winbind libpam-winbind libnss-winbind

echo "Backing up original smb.conf..."
if [ -f /etc/samba/smb.conf ]; then
    mv /etc/samba/smb.conf /etc/samba/smb.conf.bak
fi

echo "Provisioning Domain $REALM..."
samba-tool domain provision \
    --realm="$REALM" \
    --domain="$DOMAIN" \
    --adminpass="$ADMIN_PASS" \
    --server-role=dc \
    --dns-backend=SAMBA_INTERNAL \
    --use-rfc2307 \
    --function-level=2008_R2

echo "Configuring Kerberos..."
cp /var/lib/samba/private/krb5.conf /etc/krb5.conf

echo "Enabling Samba AD-DC Service..."
systemctl unmask samba-ad-dc
systemctl enable samba-ad-dc
systemctl start samba-ad-dc

# Disable standard shares as this is a DC
systemctl stop smbd nmbd winbind
systemctl disable smbd nmbd winbind

echo "Creating DNS Records for QuickStay Infrastructure..."
# Web Server
samba-tool dns add localhost $REALM web A 172.16.10.10 -U Administrator --password="$ADMIN_PASS"
samba-tool dns add localhost $REALM www CNAME web.quickstay.local -U Administrator --password="$ADMIN_PASS"
# App Server
samba-tool dns add localhost $REALM app A 172.16.20.10 -U Administrator --password="$ADMIN_PASS"
# DB Server
samba-tool dns add localhost $REALM db A 172.16.20.20 -U Administrator --password="$ADMIN_PASS"
# Security Server
samba-tool dns add localhost $REALM security A 172.16.30.30 -U Administrator --password="$ADMIN_PASS"

echo "Samba AD-DC Deployed. Domain: $REALM"
