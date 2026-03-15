#!/bin/bash
# ============================================================================
# VERIFICACIÓN POST-DESPLIEGUE
# ============================================================================
# Script para verificar que todo está funcionando correctamente
# Uso: ./verify.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_pass() { echo -e "${GREEN}[✓]${NC} $1"; }
log_fail() { echo -e "${RED}[✗]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[⚠]${NC} $1"; }
log_info() { echo -e "${BLUE}[i]${NC} $1"; }

COMPOSE_CMD=()

init_compose_cmd() {
    if docker compose version >/dev/null 2>&1; then
        COMPOSE_CMD=(docker compose)
        return 0
    fi

    if command -v docker-compose >/dev/null 2>&1; then
        COMPOSE_CMD=(docker-compose)
        return 0
    fi

    log_fail "Docker Compose no encontrado"
    return 1
}

compose() {
    if [ "${#COMPOSE_CMD[@]}" -eq 0 ]; then
        init_compose_cmd || return 1
    fi

    "${COMPOSE_CMD[@]}" "$@"
}

PASS=0
FAIL=0
WARN=0

test_service() {
    local name=$1
    local host=$2
    local port=$3
    
    if nc -z $host $port 2>/dev/null; then
        log_pass "$name ($host:$port)"
        ((PASS++))
    else
        log_fail "$name ($host:$port) NO RESPONDE"
        ((FAIL++))
    fi
}

test_container() {
    local name=$1
    
    if compose ps | grep -q "$name.*Up"; then
        log_pass "$name está corriendo"
        ((PASS++))
    else
        log_fail "$name no está corriendo"
        ((FAIL++))
    fi
}

clear
cat << "EOF"
╔════════════════════════════════════════════════════════════════╗
║   VERIFICACIÓN POST-DESPLIEGUE QUICKSTAY                      ║
║   Checklist de servicios y conectividad                       ║
╚════════════════════════════════════════════════════════════════╝
EOF
echo ""

# ============================================================================
# 1. VERIFICACIÓN DE DOCKER
# ============================================================================
log_info "1. Verificando Docker..."
if command -v docker &> /dev/null; then
    log_pass "Docker instalado"
    ((PASS++))
else
    log_fail "Docker no está instalado"
    ((FAIL++))
fi

if docker ps > /dev/null 2>&1; then
    log_pass "Docker daemon activo"
    ((PASS++))
else
    log_fail "Docker daemon no responde"
    ((FAIL++))
fi
echo ""

# ============================================================================
# 2. ESTADO DE CONTENEDORES
# ============================================================================
log_info "2. Estado de contenedores..."

test_container "router-fw"
test_container "load-balancer"
test_container "web-server-1"
test_container "web-server-2"
test_container "app-server"
test_container "mysql-db"
test_container "ad-dc-primary"
test_container "mqtt-broker"
test_container "zabbix-server"
test_container "wazuh-manager"
test_container "grafana"
test_container "dashboard-backend"
test_container "dashboard-frontend"

echo ""

# ============================================================================
# 3. CONECTIVIDAD DE RED
# ============================================================================
log_info "3. Conectividad de red..."

# Esperar un poco para que los servicios estén listos
echo "   Esperando servicios..."
sleep 5

# Probar conectividad entre redes
if compose exec -T web-server-1 ping -c 1 app-server > /dev/null 2>&1; then
    log_pass "DMZ → App (172.16.20.10)"
    ((PASS++))
else
    log_fail "DMZ → App no conecta"
    ((FAIL++))
fi

if compose exec -T app-server ping -c 1 mysql-db > /dev/null 2>&1; then
    log_pass "App → MySQL (172.16.20.20)"
    ((PASS++))
else
    log_fail "App → MySQL no conecta"
    ((FAIL++))
fi

if compose exec -T app-server ping -c 1 ad-dc-primary > /dev/null 2>&1; then
    log_pass "App → AD-DC (172.16.30.10)"
    ((PASS++))
else
    log_fail "App → AD-DC no conecta"
    ((FAIL++))
fi

echo ""

# ============================================================================
# 4. SERVICIOS PRINCIPALES
# ============================================================================
log_info "4. Verificación de servicios..."

# MySQL
if compose exec -T mysql-db mysql -u root -proot -e "SELECT 1" > /dev/null 2>&1; then
    log_pass "MySQL disponible"
    ((PASS++))
else
    log_fail "MySQL no responde"
    ((FAIL++))
fi

# Database humhouse
if compose exec -T mysql-db mysql -u root -proot -e "USE humhouse; SHOW TABLES;" > /dev/null 2>&1; then
    log_pass "Base de datos 'humhouse' inicializada"
    ((PASS++))
else
    log_warn "Base de datos 'humhouse' no existe o no está accesible"
    ((WARN++))
fi

# MQTT
if timeout 2 compose exec -T mqtt-broker mosquitto_sub -h localhost -p 1883 -t '$SYS/#' -W 1 > /dev/null 2>&1; then
    log_pass "MQTT Broker disponible"
    ((PASS++))
else
    log_warn "MQTT no responde (puede ser normal)"
    ((WARN++))
fi

# AD-DC
if timeout 2 compose exec -T ad-dc-primary samba-tool user list > /dev/null 2>&1; then
    log_pass "Active Directory disponible"
    ((PASS++))
else
    log_fail "Active Directory no responde"
    ((FAIL++))
fi

echo ""

# ============================================================================
# 5. ACCESO WEB
# ============================================================================
log_info "5. Acceso web..."

if curl -s http://localhost > /dev/null 2>&1; then
    log_pass "QuickStay Web (http://localhost)"
    ((PASS++))
else
    log_warn "QuickStay Web no responde en localhost"
    ((WARN++))
fi

echo ""

# ============================================================================
# 6. ARCHIVOS Y DIRECTORIOS
# ============================================================================
log_info "6. Archivos de configuración..."

test_file() {
    local file=$1
    if [ -f "$file" ]; then
        log_pass "$file existe"
        ((PASS++))
    else
        log_fail "$file no existe"
        ((FAIL++))
    fi
}

test_file "secrets.env"
test_file "docker-compose.yml"
test_file "deploy-all.sh"
test_file "README_DESPLIEGUE.md"

echo ""

# ============================================================================
# 7. VOLÚMENES Y ALMACENAMIENTO
# ============================================================================
log_info "7. Volúmenes y almacenamiento..."

if docker volume ls | grep -q quickstay_mysql-data; then
    log_pass "Volumen MySQL existe"
    ((PASS++))
else
    log_warn "Volumen MySQL no encontrado"
    ((WARN++))
fi

if [ -d "logs" ]; then
    log_pass "Directorio de logs existe"
    ((PASS++))
else
    log_fail "Directorio de logs no existe"
    ((FAIL++))
fi

echo ""

# ============================================================================
# 8. RESUMEN
# ============================================================================
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                      RESUMEN                                   ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "  ${GREEN}Pasadas:${NC}     $PASS"
echo "  ${RED}Fallos:${NC}       $FAIL"
echo "  ${YELLOW}Advertencias:${NC} $WARN"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ DESPLIEGUE CORRECTO${NC}"
    echo ""
    echo "Todos los servicios están operativos. Puedes acceder a:"
    echo ""
    echo "  • QuickStay Web:  http://localhost"
    echo "  • HAProxy Stats:  http://localhost:8404"
    echo ""
    echo "Para servicios internos (Zabbix, Grafana, Wazuh, etc),"
    echo "conecta mediante VPN WireGuard desde ./wireguard/config/"
    echo ""
    exit 0
else
    echo -e "${RED}✗ FALLOS DETECTADOS${NC}"
    echo ""
    echo "Soluciona los problemas anteriores:"
    echo "  1. Ver logs: docker-compose logs -f [servicio]"
    echo "  2. Reiniciar: docker-compose restart"
    echo "  3. Verificar recursos: docker stats"
    echo ""
    exit 1
fi
