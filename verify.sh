#!/bin/bash
# ============================================================================
# VERIFICACIÓN POST-DESPLIEGUE
# ============================================================================
# Script para verificar que todo está funcionando correctamente
# Uso: ./verify.sh

# No abortar en el primer fallo: queremos ver todo el resumen
set +e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_pass() { echo -e "${GREEN}[✓]${NC} $1"; }
log_fail() { echo -e "${RED}[✗]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[⚠]${NC} $1"; }
log_info() { echo -e "${BLUE}[i]${NC} $1"; }

PASS=0
FAIL=0
WARN=0

# Detectar Docker Compose (V2 "docker compose" o V1 "docker-compose")
COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    log_fail "Docker Compose no encontrado (ni 'docker compose' ni 'docker-compose')"
    exit 1
fi

log_info "Usando comando Docker Compose: $COMPOSE_CMD"

# Cargar secrets.env si existe para obtener credenciales reales (MySQL, etc.)
if [ -f "secrets.env" ]; then
    . secrets.env
fi

test_service() {
    local name=$1
    local host=$2
    local port=$3

    if command -v nc > /dev/null 2>&1; then
        if nc -z -w 2 "$host" "$port" 2>/dev/null; then
            log_pass "$name ($host:$port)"
            ((PASS++))
        else
            log_fail "$name ($host:$port) NO RESPONDE"
            ((FAIL++))
        fi
    else
        log_warn "nc no está instalado; se omite prueba $name ($host:$port)"
        ((WARN++))
    fi
}

test_container() {
    local name=$1

    # Consideramos que el servicio está corriendo si aparece en la salida de ps
    if $COMPOSE_CMD ps | grep -q "$name"; then
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

# Probar conectividad entre redes (usando IPs reales entre VLANs)
if $COMPOSE_CMD exec -T web-server-1 ping -c 1 172.16.20.10 > /dev/null 2>&1; then
    log_pass "DMZ → App (172.16.20.10)"
    ((PASS++))
else
    log_fail "DMZ → App no conecta"
    ((FAIL++))
fi

if $COMPOSE_CMD exec -T app-server ping -c 1 172.16.20.20 > /dev/null 2>&1; then
    log_pass "App → MySQL (172.16.20.20)"
    ((PASS++))
else
    log_fail "App → MySQL no conecta"
    ((FAIL++))
fi

if $COMPOSE_CMD exec -T app-server ping -c 1 172.16.30.10 > /dev/null 2>&1; then
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

# Comprobaciones rápidas vía TCP con nc (2s máx por servicio)
test_service "MySQL" 172.16.20.20 3306
test_service "MQTT Broker" 172.16.40.10 1883
test_service "Active Directory (LDAP)" 172.16.30.10 389

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
    echo "  • HAProxy Stats (vía VPN):  http://172.16.10.10:8404"
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
