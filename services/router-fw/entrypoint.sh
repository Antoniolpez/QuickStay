#!/bin/bash

# QuickStay Firewall - nftables Configuration Script
# Propósito: Configurar el firewall con nftables para segmentación de VLANs
# Fecha: 2026

set -e

echo "==> Iniciando Router/Firewall QuickStay con nftables"

# Detectar uplink real del router (salida al host/Internet) a partir de la default route.
UPLINK_IFACE="$(ip -4 route show default 2>/dev/null | awk 'NR==1{for (i=1; i<=NF; i++) if ($i=="dev") {print $(i+1); exit}}')"
UPLINK_GW="$(ip -4 route show default 2>/dev/null | awk 'NR==1{for (i=1; i<=NF; i++) if ($i=="via") {print $(i+1); exit}}')"

if [ -z "$UPLINK_IFACE" ]; then
	UPLINK_IFACE="eth0"
fi

echo "==> Uplink detectado: dev $UPLINK_IFACE via ${UPLINK_GW:-sin-gateway-explicita}"

# Ruta opcional para clientes LAN del host (escenario laboratorio local).
# Sin esta ruta, respuestas a 192.168.x.x pueden salir por la WAN simulada y romper WireGuard.
HOST_LAN_SUBNET="${ROUTER_HOST_LAN_SUBNET:-}"
HOST_LAN_GW="${ROUTER_HOST_LAN_GW:-172.16.20.1}"
if [ -n "$HOST_LAN_SUBNET" ]; then
	APP_IFACE="$(ip -4 route show 172.16.20.0/24 | awk 'NR==1{print $3}')"
	if [ -n "$APP_IFACE" ]; then
		ip route replace "$HOST_LAN_SUBNET" via "$HOST_LAN_GW" dev "$APP_IFACE" || true
		echo "==> Ruta LAN aplicada: $HOST_LAN_SUBNET via $HOST_LAN_GW dev $APP_IFACE"
	else
		echo "[WARN] No se pudo detectar interfaz app_net para ruta LAN"
	fi
fi

ROUTER_ADMIN_USER="${ROUTER_ADMIN_USER:-admin}"
ROUTER_ADMIN_PASSWORD="${ROUTER_ADMIN_PASSWORD:-QuickStay2026!Router}"
ROUTER_ADMIN_ALLOWED_CIDRS="${ROUTER_ADMIN_ALLOWED_CIDRS:-192.168.0.0/16,172.16.0.0/12,10.0.0.0/8,127.0.0.1/32}"
ROUTER_EDGE_PUBLIC_IP="${ROUTER_EDGE_PUBLIC_IP:-${HOST_IP:-192.168.1.40}}"
ROUTER_EDGE_PRIVATE_GATEWAYS="${ROUTER_EDGE_PRIVATE_GATEWAYS:-172.16.10.1,172.16.20.1,172.16.30.1,172.16.40.1,172.16.50.1}"

# Normalizar lista de gateways privados para ACL HTTP/SNI.
ROUTER_EDGE_PRIVATE_IPS="$(echo "$ROUTER_EDGE_PRIVATE_GATEWAYS" | tr ',' ' ' | xargs)"
ROUTER_EDGE_PRIVATE_HTTP_HOSTS=""
for _ip in $ROUTER_EDGE_PRIVATE_IPS; do
	ROUTER_EDGE_PRIVATE_HTTP_HOSTS="$ROUTER_EDGE_PRIVATE_HTTP_HOSTS ${_ip} ${_ip}:80"
done
ROUTER_EDGE_PRIVATE_HTTP_HOSTS="$(echo "$ROUTER_EDGE_PRIVATE_HTTP_HOSTS" | xargs)"
ROUTER_EDGE_CERT_CN="${ROUTER_EDGE_CERT_CN:-172.16.20.1}"
ROUTER_WG_CONF="${ROUTER_WG_CONF:-/etc/wireguard/wg0.conf}"

ensure_wireguard_base_conf() {
	if [ -f "$ROUTER_WG_CONF" ]; then
		return 0
	fi

	if ! command -v wg >/dev/null 2>&1; then
		echo "[WARN] No existe $ROUTER_WG_CONF y comando wg no disponible para generar uno nuevo"
		return 0
	fi

	mkdir -p "$(dirname "$ROUTER_WG_CONF")"
	local _priv _pub
	_priv="$(wg genkey)"
	_pub="$(printf '%s' "$_priv" | wg pubkey)"

	cat >"$ROUTER_WG_CONF" <<EOF
[Interface]
Address = ${WG_SERVER_IP:-10.50.0.1}/24
ListenPort = ${WG_SERVER_PORT:-51820}
PrivateKey = ${_priv}
EOF

	chmod 600 "$ROUTER_WG_CONF" || true
	echo "$_priv" > "$(dirname "$ROUTER_WG_CONF")/server_private.key"
	echo "$_pub" > "$(dirname "$ROUTER_WG_CONF")/server_public.key"
	chmod 600 "$(dirname "$ROUTER_WG_CONF")/server_private.key" || true

	echo "==> WireGuard base generado: $ROUTER_WG_CONF"
}

start_zabbix_agent() {
	if [ "${ZABBIX_AGENT_ENABLE:-1}" != "1" ]; then
		echo "==> Zabbix agent deshabilitado en router"
		return 0
	fi

	if ! command -v zabbix_agentd >/dev/null 2>&1; then
		echo "[WARN] zabbix_agentd no disponible en router"
		return 0
	fi

	if [ -z "${ZABBIX_AGENT_SERVER:-}" ]; then
		echo "[WARN] ZABBIX_AGENT_SERVER vacio; se omite agente en router"
		return 0
	fi

	mkdir -p /etc/zabbix /var/log/zabbix /run/zabbix
	local _host="${ZABBIX_AGENT_HOSTNAME:-$(hostname)}"
	local _active="${ZABBIX_AGENT_ACTIVE_SERVER:-${ZABBIX_AGENT_SERVER}}"
	local _meta="${ZABBIX_AGENT_METADATA:-quickstay-router-fw}"
	local _psk_file="${ZABBIX_AGENT_PSK_FILE:-/etc/zabbix/zabbix_agentd.psk}"
	local _psk_identity="${ZABBIX_AGENT_TLS_IDENTITY:-${_host}-psk}"

	if [ -n "${ZABBIX_AGENT_PSK:-}" ]; then
		echo -n "${ZABBIX_AGENT_PSK}" >"${_psk_file}"
	elif [ ! -s "${_psk_file}" ]; then
		openssl rand -hex 32 >"${_psk_file}"
	fi
	chmod 600 "${_psk_file}" || true

	cat >/etc/zabbix/zabbix_agentd.conf <<EOF
PidFile=/tmp/zabbix_agentd.pid
LogFile=/tmp/zabbix_agentd.log
LogFileSize=5
Server=${ZABBIX_AGENT_SERVER}
ServerActive=${_active}
Hostname=${_host}
HostMetadata=${_meta}
Timeout=10
TLSConnect=unencrypted
TLSAccept=unencrypted
EOF

	zabbix_agentd -c /etc/zabbix/zabbix_agentd.conf || echo "[WARN] no se pudo iniciar zabbix_agentd en router"
}

start_local_wireguard() {
	if [ ! -f "$ROUTER_WG_CONF" ]; then
		echo "[WARN] No existe $ROUTER_WG_CONF; se omite WireGuard local"
		return 0
	fi

	if ! command -v wg >/dev/null 2>&1; then
		echo "[WARN] comando wg no disponible; instala wireguard-tools"
		return 0
	fi

	echo "==> Activando WireGuard local en router desde $ROUTER_WG_CONF"
	ip link del wg0 2>/dev/null || true
	ip link add dev wg0 type wireguard

	# wg setconf no acepta Address/DNS/PostUp/PostDown, se eliminan para runtime.
	sed -E '/^[[:space:]]*(Address|DNS|PostUp|PostDown)[[:space:]]*=/d' "$ROUTER_WG_CONF" > /tmp/wg0-runtime.conf
	wg setconf wg0 /tmp/wg0-runtime.conf

	WG_ADDR="$(awk -F'=' '/^[[:space:]]*Address[[:space:]]*=/{gsub(/ /,"",$2); print $2; exit}' "$ROUTER_WG_CONF")"
	if [ -z "$WG_ADDR" ]; then
		WG_ADDR="${WG_SERVER_IP:-10.50.0.1}/24"
	elif [[ "$WG_ADDR" != */* ]]; then
		WG_ADDR="$WG_ADDR/24"
	fi

	ip address add "$WG_ADDR" dev wg0
	ip link set mtu 1420 up dev wg0
	ip route replace "${WG_SUBNET:-10.50.0.0/24}" dev wg0

	echo "==> WireGuard local activo: $WG_ADDR"
	wg show || true
}

start_zabbix_agent
ensure_wireguard_base_conf

# Certificado interno para enrutar HTTPS de la IP interna al panel del router.
mkdir -p /etc/haproxy/certs
if [ ! -s /etc/haproxy/certs/router-internal.pem ]; then
	openssl req -x509 -nodes -newkey rsa:2048 -days 3650 \
		-keyout /etc/haproxy/certs/router-internal.key \
		-out /etc/haproxy/certs/router-internal.crt \
		-subj "/CN=${ROUTER_EDGE_CERT_CN}" >/dev/null 2>&1 || true
	cat /etc/haproxy/certs/router-internal.key /etc/haproxy/certs/router-internal.crt > /etc/haproxy/certs/router-internal.pem 2>/dev/null || true
fi

# Aplicar configuraciones del kernel
sysctl -w net.ipv4.ip_forward=1
sysctl -w net.ipv4.conf.all.forwarding=1

# ============================================================================
# LIMPIAR Y CREAR TABLA BASE
# ============================================================================

# Limpiar tabla existente si existe
nft flush ruleset 2>/dev/null || true

echo "[1/5] Creando tabla inet 'quickstay' y cadenas base..."

nft create table inet quickstay

# ============================================================================
# CADENAS DE FILTRADO
# ============================================================================

# Cadenas de ingreso principal
nft 'add chain inet quickstay input { type filter hook input priority 0; policy drop; }'
nft 'add chain inet quickstay output { type filter hook output priority 0; policy accept; }'
nft 'add chain inet quickstay forward { type filter hook forward priority 0; policy drop; }'

# Cadenas NAT
nft 'add chain inet quickstay prerouting { type nat hook prerouting priority -100; policy accept; }'
nft 'add chain inet quickstay postrouting { type nat hook postrouting priority 100; policy accept; }'

# ============================================================================
# REGLAS INPUT (Tráfico local del router)
# ============================================================================

echo "[2/5] Configurando reglas INPUT (tráfico local)..."

# Permitir loopback
nft 'add rule inet quickstay input iifname "lo" accept'

# Permitir tráfico establecido
nft 'add rule inet quickstay input ct state established,related accept'

# Permitir SSH desde mgmt_net (172.16.30.0/24)
nft 'add rule inet quickstay input ip saddr 172.16.30.0/24 tcp dport 22 accept comment "SSH from mgmt_net"'

# Permitir SSH desde vpn_net (172.16.50.0/26)
nft 'add rule inet quickstay input ip saddr 172.16.50.0/26 tcp dport 22 accept comment "SSH from vpn_net"'

# Permitir ICMP (ping) para diagnóstico
nft 'add rule inet quickstay input icmp type echo-request accept comment "Allow ping"'

# Router como edge de entrada publica
nft 'add rule inet quickstay input tcp dport { 80, 443 } accept comment "Allow edge HTTP/HTTPS"'
nft 'add rule inet quickstay input udp dport 51820 accept comment "Allow WireGuard to router"'

# GUI admin del router: solo redes internas
nft 'add rule inet quickstay input ip saddr 192.168.1.40 tcp dport 8443 drop comment "Deny router admin from host IP"'
nft 'add rule inet quickstay input ip saddr 192.168.0.0/16 tcp dport 8443 accept comment "Router admin from LAN"'
nft 'add rule inet quickstay input ip saddr 172.16.0.0/12 tcp dport 8443 accept comment "Router admin from RFC1918 172.16/12"'
nft 'add rule inet quickstay input ip saddr 10.0.0.0/8 tcp dport 8443 accept comment "Router admin from RFC1918 10/8"'
nft 'add rule inet quickstay input ip saddr 127.0.0.1/32 tcp dport 8443 accept comment "Router admin from localhost"'

# ============================================================================
# REGLAS FORWARD (Tráfico inter-VLAN)
# ============================================================================

echo "[3/5] Configurando reglas FORWARD (inter-VLAN)..."

# Permitir tráfico establecido y relacionado
nft 'add rule inet quickstay forward ct state established,related accept'

# === VLAN 10 (DMZ) ===
# DMZ a Internet (simulated WAN) - HTTP/HTTPS
nft 'add rule inet quickstay forward ip saddr 172.16.10.0/24 ip daddr != 172.16.0.0/16 tcp dport { 80, 443 } accept comment "DMZ to WAN HTTP/S"'

# DMZ a App (puerto 8080, 1234)
nft 'add rule inet quickstay forward ip saddr 172.16.10.0/24 ip daddr 172.16.20.0/24 tcp dport { 8080, 1234 } accept comment "DMZ to App services"'

# DMZ a DNS de Management (AD-DC)
nft 'add rule inet quickstay forward ip saddr 172.16.10.0/24 ip daddr 172.16.30.0/24 udp dport 53 accept comment "DMZ to Mgmt DNS (UDP)"'
nft 'add rule inet quickstay forward ip saddr 172.16.10.0/24 ip daddr 172.16.30.0/24 tcp dport 53 accept comment "DMZ to Mgmt DNS (TCP)"'

# DMZ a App MySQL (puerto 3306)
nft 'add rule inet quickstay forward ip saddr 172.16.10.0/24 ip daddr 172.16.20.0/24 tcp dport 3306 accept comment "DMZ to MySQL"'

# DMZ interno (10 a 10)
nft 'add rule inet quickstay forward ip saddr 172.16.10.0/24 ip daddr 172.16.10.0/24 accept comment "DMZ internal traffic"'

# === VLAN 20 (App/Servicios) ===
# App a Internet
nft 'add rule inet quickstay forward ip saddr 172.16.20.0/24 ip daddr != 172.16.0.0/16 accept comment "App to WAN"'

# App a Management (DNS 53, LDAP 389/636, SSH 22)
nft 'add rule inet quickstay forward ip saddr 172.16.20.0/24 ip daddr 172.16.30.0/24 tcp dport { 22, 389, 636 } accept comment "App to Mgmt LDAP/SSH"'
nft 'add rule inet quickstay forward ip saddr 172.16.20.0/24 ip daddr 172.16.30.0/24 udp dport 53 accept comment "App to Mgmt DNS"'
nft 'add rule inet quickstay forward ip saddr 172.16.20.0/24 ip daddr 172.16.30.0/24 tcp dport 53 accept comment "App to Mgmt DNS (TCP)"'

# App a IoT (MQTT 1883/8883)
nft 'add rule inet quickstay forward ip saddr 172.16.20.0/24 ip daddr 172.16.40.0/23 tcp dport { 1883, 8883 } accept comment "App to IoT MQTT"'

# === VLAN 30 (Management) ===
# Management a Internet
nft 'add rule inet quickstay forward ip saddr 172.16.30.0/24 ip daddr != 172.16.0.0/16 accept comment "Mgmt to WAN"'

# Management a App (para monitoreo: Zabbix 10051, Wazuh 9200)
nft 'add rule inet quickstay forward ip saddr 172.16.30.0/24 ip daddr 172.16.20.0/24 tcp dport { 10051, 9200, 9300 } accept comment "Mgmt to App monitoring"'

# Management a IoT (monitoreo)
nft 'add rule inet quickstay forward ip saddr 172.16.30.0/24 ip daddr 172.16.40.0/23 accept comment "Mgmt to IoT"'

# Management a DMZ (backups, configuración)
nft 'add rule inet quickstay forward ip saddr 172.16.30.0/24 ip daddr 172.16.10.0/24 tcp dport { 22, 445 } accept comment "Mgmt to DMZ admin"'

# === VLAN 40 (IoT) ===
# IoT a Management (envío de logs y métricas)
nft 'add rule inet quickstay forward ip saddr 172.16.40.0/23 ip daddr 172.16.30.0/24 accept comment "IoT to Mgmt"'

# IoT a DNS de Management (AD-DC)
nft 'add rule inet quickstay forward ip saddr 172.16.40.0/23 ip daddr 172.16.30.0/24 udp dport 53 accept comment "IoT to Mgmt DNS (UDP)"'
nft 'add rule inet quickstay forward ip saddr 172.16.40.0/23 ip daddr 172.16.30.0/24 tcp dport 53 accept comment "IoT to Mgmt DNS (TCP)"'

# IoT a Internet (control remoto si es necesario - restringido)
nft 'add rule inet quickstay forward ip saddr 172.16.40.0/23 ip daddr != 172.16.0.0/16 tcp dport { 80, 443 } accept comment "IoT to WAN restricted"'

# IoT interno
nft 'add rule inet quickstay forward ip saddr 172.16.40.0/23 ip daddr 172.16.40.0/23 accept comment "IoT internal traffic"'

# === VLAN 50 (VPN) ===
# VPN a Management (acceso full)
nft 'add rule inet quickstay forward ip saddr 10.50.0.0/24 ip daddr 172.16.30.0/24 accept comment "VPN to Mgmt"'
nft 'add rule inet quickstay forward ip saddr 172.16.50.0/26 ip daddr 172.16.30.0/24 accept comment "vpn_net to Mgmt"'

# VPN a DNS de Management (AD-DC)
nft 'add rule inet quickstay forward ip saddr 10.50.0.0/24 ip daddr 172.16.30.0/24 udp dport 53 accept comment "VPN to Mgmt DNS (UDP)"'
nft 'add rule inet quickstay forward ip saddr 10.50.0.0/24 ip daddr 172.16.30.0/24 tcp dport 53 accept comment "VPN to Mgmt DNS (TCP)"'
nft 'add rule inet quickstay forward ip saddr 172.16.50.0/26 ip daddr 172.16.30.0/24 udp dport 53 accept comment "vpn_net to Mgmt DNS (UDP)"'
nft 'add rule inet quickstay forward ip saddr 172.16.50.0/26 ip daddr 172.16.30.0/24 tcp dport 53 accept comment "vpn_net to Mgmt DNS (TCP)"'

# VPN a App (acceso full)
nft 'add rule inet quickstay forward ip saddr 10.50.0.0/24 ip daddr 172.16.20.0/24 accept comment "VPN to App"'

# VPN a DMZ (acceso full)
nft 'add rule inet quickstay forward ip saddr 10.50.0.0/24 ip daddr 172.16.10.0/24 accept comment "VPN to DMZ"'

# VPN a IoT (acceso full)
nft 'add rule inet quickstay forward ip saddr 10.50.0.0/24 ip daddr 172.16.40.0/23 accept comment "VPN to IoT"'

# VPN return traffic desde todas las redes
nft 'add rule inet quickstay forward ip daddr 10.50.0.0/24 ct state established,related accept comment "Return traffic to VPN"'

# === ICMP para diagnóstico ===
nft 'add rule inet quickstay forward icmp type echo-request accept comment "Allow ping forward"'

# ============================================================================
# NAT Y MASQUERADE
# ============================================================================

echo "[4/5] Configurando reglas NAT..."

# SNAT: Masquerade desde todas las VLANs internas a WAN (cualquier interfaz que no sea interna)
nft 'add rule inet quickstay postrouting ip saddr 172.16.0.0/16 ip daddr != 172.16.0.0/16 counter masquerade comment "Masquerade internal to WAN"'

# SNAT: Masquerade desde VPN a redes internas (para evitar problemas de routing asimétrico)
nft 'add rule inet quickstay postrouting ip saddr 10.50.0.0/24 ip daddr 172.16.0.0/16 counter masquerade comment "Masquerade VPN to internal"'
nft 'add rule inet quickstay postrouting ip saddr 172.16.50.0/26 ip daddr 172.16.0.0/16 counter masquerade comment "Masquerade vpn_net to internal"'

start_local_wireguard

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

# ============================================================================
# EDGE PROXY + GUI ADMIN (HAProxy en el router)
# ============================================================================

cat > /etc/haproxy/haproxy-edge.cfg <<EOF
global
	log stdout format raw local0
	daemon
	maxconn 4096

defaults
	log global
	mode http
	timeout connect 5000ms
	timeout client  50000ms
	timeout server  50000ms

frontend edge_http
	bind *:80
	option httplog
	# Gateways privados => panel router. IP publica => DMZ.
	acl host_private_gateway hdr(host) -i ${ROUTER_EDGE_PRIVATE_HTTP_HOSTS}
	use_backend router_admin_http if host_private_gateway
	default_backend dmz_lb_http

frontend edge_https
	bind *:443
	mode tcp
	option tcplog
	tcp-request inspect-delay 5s
	tcp-request content accept if { req_ssl_hello_type 1 }
	acl sni_private_gateway req.ssl_sni -i ${ROUTER_EDGE_PRIVATE_IPS}
	use_backend router_admin_tls_in if sni_private_gateway
	default_backend dmz_lb_https

frontend router_admin_tls_front
	bind 127.0.0.1:9443 ssl crt /etc/haproxy/certs/router-internal.pem
	mode http
	option httplog
	default_backend router_admin_http

backend dmz_lb_http
	mode http
	option httpchk GET /
	server lb 172.16.10.10:80 check inter 2000 rise 2 fall 3

backend router_admin_http
	mode http
	server router_admin 127.0.0.1:8443

backend dmz_lb_https
	mode tcp
	server lb_tls 172.16.10.10:443 check

backend router_admin_tls_in
	mode tcp
	server router_tls_local 127.0.0.1:9443 check
EOF

echo "==> Iniciando Router Admin API en :8443 (acceso interno)"
export ROUTER_ADMIN_USER ROUTER_ADMIN_PASSWORD ROUTER_ADMIN_ALLOWED_CIDRS
if python3 -c "import gunicorn" >/dev/null 2>&1; then
	python3 -m gunicorn --chdir /usr/local/bin --bind 0.0.0.0:8443 --workers 2 --threads 4 --timeout 30 router_admin:app &
else
	echo "[WARN] gunicorn no encontrado; usando servidor Flask fallback"
	python3 /usr/local/bin/router_admin.py &
fi

echo "==> Iniciando HAProxy edge en router (80/443)"
haproxy -f /etc/haproxy/haproxy-edge.cfg

# Mantener el contenedor en ejecución
exec "$@"
