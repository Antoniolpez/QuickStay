#!/bin/bash
# ============================================================================
# QUICKSTAY - WireGuard VPN Helper
# ----------------------------------------------------------------------------
# Script para inicializar el servidor WireGuard (contenedor docker) y dejar
# preparada la configuración del cliente admin en una ruta fácil de localizar.
#
# Uso (en la VM, dentro de ProyectoFinal/):
#   chmod +x setup-wireguard-vpn.sh
#   ./setup-wireguard-vpn.sh
# ============================================================================

set -e

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info()   { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok()     { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()   { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()  { echo -e "${RED}[ERROR]${NC} $1"; }

# 1. Comprobaciones básicas
if ! command -v docker-compose >/dev/null 2>&1 && ! docker compose version >/dev/null 2>&1; then
  log_error "Docker Compose no está instalado. Ejecuta primero ./deploy-all.sh o instala docker-compose."
  exit 1
fi

if [ ! -f "docker-compose.yml" ]; then
  log_error "No se encuentra docker-compose.yml. Ejecuta el script desde la carpeta ProyectoFinal/"
  exit 1
fi

if [ ! -f "secrets.env" ]; then
  log_error "Archivo secrets.env no encontrado. Cópialo/ajústalo antes de continuar."
  exit 1
fi

# 2. Asegurar archivo .env para interpolación de variables
if [ ! -f ".env" ]; then
  log_info "Creando .env a partir de secrets.env para Docker Compose..."
  cp secrets.env .env
  log_ok ".env creado (copia de secrets.env)."
else
  log_info ".env ya existe, se usarán esas variables para WireGuard."
fi

# 3. Preparar estructura de directorios WireGuard
mkdir -p wireguard/config wireguard/keys
log_ok "Directorios wireguard/ preparados."

# 4. Levantar solo el servicio de WireGuard VPN
log_info "Iniciando contenedor wireguard-vpn..."
if command -v docker-compose >/dev/null 2>&1; then
  docker-compose up -d wireguard-vpn
else
  docker compose up -d wireguard-vpn
fi

# 5. Esperar a que el contenedor esté en estado Up
log_info "Esperando a que wireguard-vpn esté en estado Up..."
MAX_WAIT=60
COUNTER=0
while true; do
  STATUS_LINE=$(docker ps --filter "name=quickstay-vpn" --format '{{.Status}}' || true)
  if echo "$STATUS_LINE" | grep -q "Up"; then
    log_ok "wireguard-vpn está en marcha ($STATUS_LINE)."
    break
  fi
  sleep 2
  COUNTER=$((COUNTER+2))
  if [ $COUNTER -ge $MAX_WAIT ]; then
    log_warn "Timeout esperando a wireguard-vpn. Revisa 'docker-compose logs wireguard-vpn'."
    break
  fi
done

# 6. Localizar y copiar la configuración del cliente admin
#   La imagen linuxserver/wireguard suele generar:
#     /config/peer_admin1/peer_admin1.conf
#   En el host, gracias al volumen ./wireguard/config:/config, eso será:
#     ./wireguard/config/peer_admin1/peer_admin1.conf

SRC1="wireguard/config/peer_admin1.conf"
SRC2="wireguard/config/peer_admin1/peer_admin1.conf"
DEST="wireguard/peer_admin1.conf"

if [ -f "$SRC1" ]; then
  cp "$SRC1" "$DEST"
elif [ -f "$SRC2" ]; then
  cp "$SRC2" "$DEST"
else
  log_warn "No se encontró aún la configuración del peer admin1 en wireguard/config/."
  log_warn "Es posible que el contenedor aún esté generando los ficheros."
  log_warn "Revisa el contenido de wireguard/config/ y copia manualmente el *.conf del peer."
  exit 0
fi

log_ok "Configuración de cliente copiada en: $DEST"

cat <<EOF

${GREEN}Cliente WireGuard listo:${NC}
  • Fichero: $DEST
  • Ejemplo de uso en Linux:  sudo wg-quick up $DEST
  • En Windows/macOS: importar ese .conf en el cliente gráfico de WireGuard.

Si ya tienes la VPN levantada y conectas con este peer, deberías poder
alcanzar las redes internas 172.16.0.0/16 (AD, MySQL, Zabbix, etc.).
EOF
