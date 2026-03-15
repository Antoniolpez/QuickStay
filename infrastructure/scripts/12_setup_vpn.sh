#!/bin/bash
# 12_setup_vpn.sh
# Phase 2 Guide 3: Admin VPN Implementation (Simulated with WireGuard)
# Usage: sudo ./12_setup_vpn.sh <PUBLIC_IP>
# Example: sudo ./12_setup_vpn.sh 203.0.113.2

SERVER_IP=${1:-"$(hostname -I | cut -d' ' -f1)"}
VPN_SUBNET="172.16.50.0/24"
VPN_SERVER_IP="172.16.50.1"
ADMIN_PEER_IP="172.16.50.10"

echo "Installing WireGuard..."
apt-get update
apt-get install -y wireguard

echo "Generating Keys..."
umask 077
wg genkey | tee server_private.key | wg pubkey > server_public.key
wg genkey | tee admin_private.key | wg pubkey > admin_public.key

SERVER_PRIV=$(cat server_private.key)
ADMIN_PUB=$(cat admin_public.key)
ADMIN_PRIV=$(cat admin_private.key)
SERVER_PUB=$(cat server_public.key)

echo "Configuring WireGuard Server Interface (wg0)..."
cat <<EOF > /etc/wireguard/wg0.conf
[Interface]
Address = $VPN_SERVER_IP/24
SaveConfig = true
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o enp0s3 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o enp0s3 -j MASQUERADE
ListenPort = 51820
PrivateKey = $SERVER_PRIV

[Peer]
# Admin User
PublicKey = $ADMIN_PUB
AllowedIPs = $ADMIN_PEER_IP/32
EOF

echo "Enabling IP Forwarding (if not already done)..."
sysctl -w net.ipv4.ip_forward=1

echo "Starting WireGuard..."
systemctl enable wg-quick@wg0
systemctl start wg-quick@wg0

echo "=== Client Configuration (Save this for the Admin) ==="
cat <<EOF
[Interface]
PrivateKey = $ADMIN_PRIV
Address = $ADMIN_PEER_IP/24
DNS = 172.16.30.10

[Peer]
PublicKey = $SERVER_PUB
Endpoint = $SERVER_IP:51820
AllowedIPs = 172.16.0.0/16
PersistentKeepalive = 25
EOF
echo "===================================================="
echo "VPN Deployed. Admin VLAN 50 access enabled."
