#!/bin/bash

# QuickStay Firewall - nftables Configuration Script
# Propósito: Configurar el firewall con nftables para segmentación de VLANs
# Fecha: 2026

set -e

echo "==> Iniciando Router/Firewall QuickStay con nftables"

# Aplicar configuraciones del kernel
sysctl -w net.ipv4.ip_forward=1
sysctl -w net.ipv4.conf.all.forwarding=1

# ============================================================================
# LIMPIAR Y CREAR TABLA BASE + REGLAS (via nft script)
# ============================================================================

# Limpiar tabla existente si existe
nft flush ruleset 2>/dev/null || true

echo "[1/5] Creando tabla inet 'quickstay' y reglas base..."

nft -f - << 'EOF'
create table inet quickstay

# Cadenas
create chain inet quickstay input { type filter hook input priority 0; policy drop; }
create chain inet quickstay output { type filter hook output priority 0; policy accept; }
create chain inet quickstay forward { type filter hook forward priority 0; policy drop; }
create chain inet quickstay prerouting { type nat hook prerouting priority -100; policy accept; }
create chain inet quickstay postrouting { type nat hook postrouting priority 100; policy accept; }

# REGLAS INPUT (tráfico local)
add rule inet quickstay input iifname "lo" accept
add rule inet quickstay input ct state established,related accept
add rule inet quickstay input iifname "eth*" ip saddr 172.16.30.0/24 tcp dport 22 accept comment "SSH from mgmt_net"
add rule inet quickstay input iifname "eth*" ip saddr 172.16.50.0/26 tcp dport 22 accept comment "SSH from vpn_net"
add rule inet quickstay input iifname "eth*" icmp type echo-request accept comment "Allow ping"

# REGLAS FORWARD (inter-VLAN)
add rule inet quickstay forward ct state established,related accept

# VLAN 10 (DMZ)
add rule inet quickstay forward iifname "eth1" oifname "eth0" ip saddr 172.16.10.0/24 tcp dport { 80, 443 } accept comment "DMZ to WAN HTTP/S"
add rule inet quickstay forward iifname "eth1" oifname "eth2" ip saddr 172.16.10.0/24 ip daddr 172.16.20.0/24 tcp dport { 8080, 1234 } accept comment "DMZ to App services"
add rule inet quickstay forward iifname "eth1" oifname "eth2" ip saddr 172.16.10.0/24 ip daddr 172.16.20.0/24 tcp dport 3306 accept comment "DMZ to MySQL"
add rule inet quickstay forward iifname "eth1" oifname "eth1" ip saddr 172.16.10.0/24 ip daddr 172.16.10.0/24 accept comment "DMZ internal traffic"

# VLAN 20 (App/Servicios)
add rule inet quickstay forward iifname "eth2" oifname "eth0" ip saddr 172.16.20.0/24 accept comment "App to WAN"
add rule inet quickstay forward iifname "eth2" oifname "eth3" ip saddr 172.16.20.0/24 ip daddr 172.16.30.0/24 tcp dport { 22, 389, 636 } accept comment "App to Mgmt LDAP/SSH"
add rule inet quickstay forward iifname "eth2" oifname "eth3" ip saddr 172.16.20.0/24 ip daddr 172.16.30.0/24 udp dport 53 accept comment "App to Mgmt DNS"
add rule inet quickstay forward iifname "eth2" oifname "eth4" ip saddr 172.16.20.0/24 ip daddr 172.16.40.0/23 tcp dport { 1883, 8883 } accept comment "App to IoT MQTT"

# VLAN 30 (Management)
add rule inet quickstay forward iifname "eth3" oifname "eth0" ip saddr 172.16.30.0/24 accept comment "Mgmt to WAN"
add rule inet quickstay forward iifname "eth3" oifname "eth2" ip saddr 172.16.30.0/24 tcp dport { 10051, 9200, 9300 } accept comment "Mgmt to App monitoring"
add rule inet quickstay forward iifname "eth3" oifname "eth4" ip saddr 172.16.30.0/24 accept comment "Mgmt to IoT"
add rule inet quickstay forward iifname "eth3" oifname "eth1" ip saddr 172.16.30.0/24 tcp dport { 22, 445 } accept comment "Mgmt to DMZ admin"

# VLAN 40 (IoT)
add rule inet quickstay forward iifname "eth4" oifname "eth3" ip saddr 172.16.40.0/23 accept comment "IoT to Mgmt"
add rule inet quickstay forward iifname "eth4" oifname "eth0" ip saddr 172.16.40.0/23 tcp dport { 80, 443 } accept comment "IoT to WAN restricted"
add rule inet quickstay forward iifname "eth4" oifname "eth4" ip saddr 172.16.40.0/23 accept comment "IoT internal traffic"

# VLAN 50 (VPN)
add rule inet quickstay forward iifname "eth5" oifname "eth3" ip saddr 172.16.50.0/26 accept comment "VPN to Mgmt"
add rule inet quickstay forward iifname "eth5" oifname "eth2" ip saddr 172.16.50.0/26 accept comment "VPN to App"
add rule inet quickstay forward iifname "eth5" oifname "eth1" ip saddr 172.16.50.0/26 accept comment "VPN to DMZ"
add rule inet quickstay forward iifname "eth5" oifname "eth4" ip saddr 172.16.50.0/26 accept comment "VPN to IoT"
add rule inet quickstay forward iifname "eth*" oifname "eth5" ip daddr 172.16.50.0/26 ct state established,related accept comment "Return traffic to VPN"

# ICMP diagnóstico en forward
add rule inet quickstay forward icmp type echo-request accept comment "Allow ping forward"

# NAT y MASQUERADE

# DNAT: Redirigir tráfico HTTP/HTTPS desde WAN (eth0) hacia HAProxy en DMZ (172.16.10.10)
add rule inet quickstay prerouting iifname "eth0" ip protocol tcp tcp dport { 80, 443 } dnat to 172.16.10.10 comment "DNAT WAN HTTP/HTTPS to HAProxy"

add rule inet quickstay postrouting oifname "eth0" ip saddr 172.16.0.0/16 counter masquerade comment "Masquerade internal to WAN"
EOF

echo "[2/5] Reglas de INPUT configuradas"
echo "[3/5] Reglas de FORWARD configuradas"
echo "[4/5] Reglas de NAT configuradas"

# ============================================================================
# LOG Y ESTADÍSTICAS
# ============================================================================

echo "[5/5] Finalizando configuración..."

# Mostrar resumen de configuración
echo ""
echo "==> Configuración de nftables aplicada:"
nft list ruleset | head -30

echo ""
echo "==> Verificación de IP Forwarding:"
sysctl net.ipv4.ip_forward
sysctl net.ipv4.conf.all.forwarding

echo ""
echo "==> Estado de interfaces de red:"
ip link show | grep -E "eth[0-9]|link"

echo ""
echo "==> Rutas de red:"
ip route show

echo ""
echo "✓ Firewall nftables configurado correctamente"
echo "✓ IP Forwarding habilitado"
echo "✓ NAT MASQUERADE activo para tráfico interno"
echo "✓ Segmentación inter-VLAN configurada"
echo ""
echo "==> Router/Firewall iniciado"

# Mantener el contenedor en ejecución
exec "$@"
