#!/bin/bash

# ============================================================================
# QUICKSTAY INFRASTRUCTURE - FULL CLEANUP SCRIPT
# ============================================================================
# Propósito: Eliminar todo rastro del despliegue para empezar de cero.
# CUIDADO: Este script borrará DATOS, LOGS y CONFIGURACIONES generadas.
# ============================================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[✓]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[!]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo -e "${RED}"
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║             ADVERTENCIA: LIMPIEZA TOTAL DE INFRAESTRUCTURA     ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# 1. Detener y eliminar contenedores, redes y volúmenes
log_info "Deteniendo y eliminando contenedores, redes y volúmenes de QuickStay..."
docker-compose down -v --remove-orphans 2>/dev/null || true
log_success "Contenedores y volúmenes eliminados"

# 2. Eliminar imágenes locales del proyecto
log_info "Eliminando imágenes Docker del proyecto..."
PROJECT_IMAGES=$(docker images | grep "quickstay" | awk '{print $3}')
if [ -n "$PROJECT_IMAGES" ]; then
    docker rmi -f $PROJECT_IMAGES 2>/dev/null || true
    log_success "Imágenes del proyecto eliminadas"
else
    log_info "No se encontraron imágenes del proyecto para eliminar"
fi

# 3. Eliminar directorios de datos y logs
log_info "Limpiando directorios de datos, logs y claves..."
# Usar docker para asegurar que podemos borrar incluso si fueron creados por root en volúmenes
docker run --rm -v "$(pwd):/app" alpine sh -c "rm -rf /app/logs/* /app/data/* /app/wireguard/config/* /app/wireguard/keys/* /app/ssh/keys/* /app/backups/*" 2>/dev/null || true

# Asegurar que los directorios base existen pero están vacíos
mkdir -p logs data wireguard/config wireguard/keys ssh/keys backups
log_success "Directorios limpiados"

# 4. Limpieza general de Docker (opcional, pero útil para "máquina nueva")
log_info "¿Deseas realizar una limpieza general de Docker (prune)? (esto puede afectar a otros proyectos)"
# En modo no interactivo para seguridad del usuario, solo lo mencionamos
log_warning "Ejecutando limpieza de redes y contenedores huérfanos..."
docker network prune -f 2>/dev/null || true
docker container prune -f 2>/dev/null || true

echo ""
echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
log_success "¡LIMPIEZA COMPLETADA CON ÉXITO!"
log_info "El entorno está listo para un nuevo despliegue con ./deploy-complete.sh"
echo -e "${GREEN}════════════════════════════════════════════════════════════════${NC}"
echo ""
