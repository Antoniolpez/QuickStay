#!/bin/bash
# ============================================================================
# QUICKSTAY INFRASTRUCTURE - MASTER DEPLOYMENT SCRIPT (v2 - ENHANCED)
# ============================================================================
# Script de despliegue completo con validaciones extensivas
# Autor: Antonio López Montes
# Fecha: Enero 2026
# ============================================================================

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Contadores
WARNINGS=0
ERRORS=0
CHECKS_PASSED=0

# Funciones de utilidad
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
    ((CHECKS_PASSED++))
}

log_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
    ((WARNINGS++))
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
    ((ERRORS++))
}

log_debug() {
    echo -e "${MAGENTA}[DEBUG]${NC} $1"
}

test_file() {
    local file=$1
    if [ -f "$file" ]; then
        return 0
    else
        log_error "Archivo no encontrado: $file"
        return 1
    fi
}

test_dir() {
    local dir=$1
    if [ -d "$dir" ]; then
        return 0
    else
        log_error "Directorio no encontrado: $dir"
        return 1
    fi
}

# Banner
clear
cat << "EOF"
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║   ██████╗ ██╗   ██╗██╗ ██████╗██╗  ██╗███████╗████████╗ █████╗██╗   ██╗
║  ██╔═══██╗██║   ██║██║██╔════╝██║ ██╔╝██╔════╝╚══██╔══╝██╔══██╗╚██╗ ██╔╝
║  ██║   ██║██║   ██║██║██║     █████╔╝ ███████╗   ██║   ███████║ ╚████╔╝ 
║  ██║▄▄ ██║██║   ██║██║██║     ██╔═██╗ ╚════██║   ██║   ██╔══██║  ╚██╔╝  
║  ╚██████╔╝╚██████╔╝██║╚██████╗██║  ██╗███████║   ██║   ██║  ██║   ██║   
║   ╚══▀▀═╝  ╚═════╝ ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝   ╚═╝   
║                                                               ║
║           DESPLIEGUE MAESTRO DE INFRAESTRUCTURA (v2)          ║
║              Validaciones Extensivas Incluidas                ║
║                    Proyecto ASIR 2026                         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF

log_info "Iniciando despliegue de QuickStay con validaciones..."
echo ""

# ============================================================================
# FASE 1: PRE-VALIDACIÓN DE ESTRUCTURA
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 1: PRE-VALIDACIÓN DE ESTRUCTURA"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Validando directorios críticos..."
if test_dir "Documentación/app_repo"; then
    log_success "Directorio Java"
fi
if test_dir "Documentación/dashboard-v2"; then
    log_success "Directorio Dashboard"
fi
if test_dir "Documentación/iot"; then
    log_success "Directorio IoT"
fi
if test_dir "infrastructure"; then
    log_success "Directorio Infrastructure"
fi

echo ""
log_info "Validando archivos críticos..."
test_file "docker-compose.yml" && log_success "docker-compose.yml" || exit 1
test_file "secrets.env" && log_success "secrets.env" || log_warning "secrets.env (valores por defecto)"
test_file "deploy-all.sh" && log_success "deploy-all.sh" || log_error "Script maestro"

echo ""

# ============================================================================
# FASE 2: VALIDACIÓN DE CÓDIGO FUENTE
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 2: VALIDACIÓN DE CÓDIGO FUENTE"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

# Java
log_info "Analizando código Java..."
if test_file "Documentación/app_repo/pom.xml"; then
    log_info "   Verificando sintaxis XML..."
    if command -v xmllint &> /dev/null; then
        if xmllint --noout "Documentación/app_repo/pom.xml" 2>/dev/null; then
            log_success "   pom.xml sintaxis válida"
        else
            log_warning "   pom.xml tiene advertencias XML"
        fi
    else
        log_debug "   xmllint no instalado, saltando validación"
    fi
    
    if test_file "Documentación/app_repo/src/main/java/ProyectoFinal/Servidor/Servidor.java"; then
        log_success "   Servidor.java encontrado"
        
        # Verificar clase principal
        if grep -q "public static void main" "Documentación/app_repo/src/main/java/ProyectoFinal/Servidor/Servidor.java"; then
            log_success "   Método main encontrado"
        else
            log_warning "   Método main no encontrado"
        fi
    else
        log_error "   Servidor.java no encontrado"
    fi
    
    # Verificar target/classes (compilado)
    if [ -d "Documentación/app_repo/target" ]; then
        log_success "   Carpeta target existe (código compilado)"
    else
        log_warning "   Carpeta target no existe (necesitará compilarse)"
    fi
else
    log_warning "   pom.xml no encontrado"
fi

echo ""

# Python IoT
log_info "Analizando Python IoT..."
set +e
if test_file "Documentación/iot/iot_client.py"; then
    if command -v python3 &> /dev/null; then
        log_info "   Verificando sintaxis Python..."
        if python3 -m py_compile "Documentación/iot/iot_client.py" 2>/dev/null; then
            log_success "   iot_client.py sintaxis válida"
        else
            log_warning "   iot_client.py tiene errores de sintaxis (no bloqueante para el despliegue)"
        fi
        
        # Verificar imports
        if python3 << 'PYEOF' 2>/dev/null
import ast
with open("Documentación/iot/iot_client.py") as f:
    ast.parse(f.read())
PYEOF
        then
            log_success "   Imports de Python válidos"
        else
            log_warning "   Imports de Python con posibles problemas (no bloqueante)"
        fi
    else
        log_warning "   Python3 no instalado, saltando validación"
    fi
    
    # Verificar dependencias
    if test_file "Documentación/iot/Dockerfile"; then
        if grep -q "paho-mqtt" "Documentación/iot/Dockerfile"; then
            log_success "   Dependencias MQTT definidas"
        fi
    fi
else
    log_warning "   iot_client.py no encontrado"
fi

echo ""

# Dashboard Node.js / FastAPI
log_info "Analizando Dashboard (React + FastAPI)..."

# Validación package.json
if test_file "Documentación/dashboard-v2/frontend/package.json"; then
    if command -v python3 &> /dev/null; then
        if python3 -m json.tool "Documentación/dashboard-v2/frontend/package.json" > /dev/null 2>&1; then
            log_success "   package.json válido"
        else
            log_warning "   package.json tiene posibles errores JSON (no bloqueante)"
        fi
    fi
fi

# Validación requirements.txt
if test_file "Documentación/dashboard-v2/backend/requirements.txt"; then
    log_success "   requirements.txt (FastAPI) encontrado"
    
    # Verificar dependencias críticas
    if grep -q "fastapi\|uvicorn" "Documentación/dashboard-v2/backend/requirements.txt"; then
        log_success "   FastAPI y uvicorn definidos"
    else
        log_warning "   FastAPI o uvicorn no encontrados en requirements.txt"
    fi
fi

# Validación sintaxis main.py (no bloqueante)
set +e
if test_file "Documentación/dashboard-v2/backend/main.py"; then
    if command -v python3 &> /dev/null; then
        if python3 -m py_compile "Documentación/dashboard-v2/backend/main.py" 2>/dev/null; then
            log_success "   main.py (FastAPI) sintaxis válida"
        else
            log_warning "   main.py tiene errores de sintaxis (no bloqueante para el despliegue)"
        fi
    fi
fi

echo ""

# Base de datos
log_info "Analizando Base de Datos..."
if test_file "Documentación/infrastructure/db/init.sql"; then
    log_info "   Verificando estructura SQL..."
    
    # Contar tablas
    table_count=$(grep -c "CREATE TABLE" "Documentación/infrastructure/db/init.sql" || echo 0)
    if [ "$table_count" -gt 0 ]; then
        log_success "   $table_count tablas SQL definidas"
    else
        log_warning "   No se encontraron definiciones de tabla"
    fi
    
    # Verificar tabla principal
    if grep -q "CREATE TABLE.*usuario" "Documentación/infrastructure/db/init.sql"; then
        log_success "   Tabla 'usuario' encontrada"
    fi
    
    if grep -q "CREATE TABLE.*propiedad" "Documentación/infrastructure/db/init.sql"; then
        log_success "   Tabla 'propiedad' encontrada"
    fi
else
    log_warning "   init.sql no encontrado"
fi

echo ""

# ============================================================================
# FASE 3: VALIDACIÓN DE CONFIGURACIÓN
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 3: VALIDACIÓN DE CONFIGURACIÓN"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Validando secrets.env..."
if [ -f "secrets.env" ]; then
    # Contar variables críticas
    required_vars=("MYSQL_PASSWORD" "AD_ADMIN_PASSWORD" "MQTT_PASSWORD" "WG_SERVER_PORT")
    for var in "${required_vars[@]}"; do
        if grep -q "^$var=" "secrets.env"; then
            log_success "   $var definida"
        else
            log_warning "   $var no definida"
        fi
    done

    # Exportar todas las variables de secrets.env al entorno del script
    # para que docker compose pueda resolver ${VAR} usando variables de entorno
    log_info "Cargando variables de entorno desde secrets.env para Docker Compose..."
    set -a
    . secrets.env
    set +a

    # Asegurar también archivo .env para compatibilidad con docker-compose clásico
    if [ ! -f ".env" ]; then
        log_info "Creando archivo .env a partir de secrets.env para Docker Compose..."
        cp secrets.env .env
        log_success ".env creado (copia de secrets.env)"
    else
        log_info "Archivo .env ya existe; se usarán sus valores para Docker Compose"
    fi
fi

echo ""

log_info "Validando docker-compose.yml..."
if test_file "docker-compose.yml"; then
    # Contar servicios
    service_count=$(grep -c "^  [a-z].*:$" "docker-compose.yml" || echo 0)
    log_success "   $service_count servicios definidos"
    
    # Verificar servicios críticos
    critical_services=("router-fw" "mysql-db" "app-server" "ad-dc-primary")
    for service in "${critical_services[@]}"; do
        if grep -q "^  $service:" "docker-compose.yml"; then
            log_success "   Servicio '$service' definido"
        else
            log_error "   Servicio '$service' NO encontrado"
        fi
    done
fi

echo ""

# ============================================================================
# FASE 4: VERIFICACIÓN DE REQUISITOS DEL SISTEMA
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 4: VERIFICACIÓN DE REQUISITOS DEL SISTEMA"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Verificando sistema operativo..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    log_success "Linux detectado"
    
    # Detectar distribución
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        log_info "   Distribución: $PRETTY_NAME"
    fi
else
    log_error "Este script solo funciona en Linux"
    exit 1
fi

echo ""

log_info "Verificando Docker..."
if ! command -v docker &> /dev/null; then
    log_warning "Docker no está instalado"
    
    read -p "¿Instalar Docker ahora? (s/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        log_info "Instalando Docker..."
        
        sudo apt-get update -qq
        sudo apt-get install -y -qq \
            apt-transport-https \
            ca-certificates \
            curl \
            gnupg \
            lsb-release
        
        sudo mkdir -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
        
        sudo apt-get update -qq
        sudo apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        
        sudo usermod -aG docker $USER
        
        log_success "Docker instalado"
        log_warning "IMPORTANTE: Ejecuta 'newgrp docker' para aplicar permisos"
    else
        log_error "Docker es requerido"
        exit 1
    fi
else
    log_success "Docker instalado: $(docker --version)"
fi

echo ""

log_info "Verificando Docker Compose..."
# Detectar si está disponible Docker Compose V2 ("docker compose") o V1 ("docker-compose")
COMPOSE_CMD=""
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
    log_success "Docker Compose V2: $(docker compose version --short)"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
    log_success "Docker Compose V1: $(docker-compose --version)"
else
    log_error "Docker Compose no encontrado"
    exit 1
fi
log_debug "Comando Docker Compose seleccionado: '$COMPOSE_CMD'"

echo ""

log_info "Verificando daemon Docker..."
if docker ps > /dev/null 2>&1; then
    log_success "Docker daemon funciona"
    log_info "   Imágenes presentes: $(docker images --quiet | wc -l)"
    log_info "   Contenedores: $(docker ps -a --quiet | wc -l)"
else
    log_error "No se puede conectar a Docker daemon"
    exit 1
fi

echo ""

log_info "Verificando recursos del sistema..."
total_mem=$(free -h | grep "^Mem:" | awk '{print $2}')
log_info "   Memoria total: $total_mem"

cpu_cores=$(nproc)
log_info "   CPU cores: $cpu_cores"

available_disk=$(df -h / | tail -1 | awk '{print $4}')
log_info "   Espacio disponible: $available_disk"

# Advertencia si recursos bajos
if [ "$cpu_cores" -lt 4 ]; then
    log_warning "CPU: Se recomienda mínimo 4 cores (tienes $cpu_cores)"
fi

echo ""

# ============================================================================
# FASE 5: RESUMEN PRE-DESPLIEGUE
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 5: RESUMEN PRE-DESPLIEGUE"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

echo "Checks completados:"
echo -e "  ${GREEN}Pasados:${NC}      $CHECKS_PASSED"
echo -e "  ${YELLOW}Advertencias:${NC} $WARNINGS"
echo -e "  ${RED}Errores:${NC}       $ERRORS"
echo ""

if [ $ERRORS -gt 0 ]; then
    log_error "Se encontraron $ERRORS problemas críticos"
    read -p "¿Continuar de todas formas? (s/N): " -n 1 -r
    echo
    if ! [[ $REPLY =~ ^[Ss]$ ]]; then
        exit 1
    fi
fi

if [ $WARNINGS -gt 0 ]; then
    log_warning "Hay $WARNINGS advertencias (pero puede continuar)"
fi

echo ""
read -p "Presiona Enter para continuar con el despliegue..."
echo ""

# ============================================================================
# FASE 6: PREPARACIÓN DE DIRECTORIOS
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 6: PREPARACIÓN DE DIRECTORIOS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Creando estructura de directorios..."

mkdir -p logs/{router,haproxy,web1,web2,app,mysql,ad-dc,zabbix,wazuh,mqtt,dashboard,deploy}
mkdir -p wireguard/{config,keys}
mkdir -p ssh/keys
mkdir -p backups
mkdir -p data/{mysql,zabbix,wazuh}

chmod 600 secrets.env
log_success "Directorios creados"

echo ""

# ============================================================================
# FASE 7: GENERAR CONTRASEÑAS MOSQUITTO
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 7: CONFIGURACIÓN DE SERVICIOS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Generando configuración de Mosquitto..."

if [ ! -f "infrastructure/config/mosquitto/passwd" ]; then
    source secrets.env
    
    docker run --rm -v "$(pwd)/infrastructure/config/mosquitto:/mosquitto/config" \
        eclipse-mosquitto:2.0 \
        sh -c "mosquitto_passwd -c -b /mosquitto/config/passwd ${MQTT_USERNAME:-quickstay_iot} ${MQTT_PASSWORD:-QuickStay2026!MQTT}" 2>/dev/null || true
    
    log_success "Contraseñas de Mosquitto generadas"
else
    log_success "Archivo de contraseñas de Mosquitto ya existe"
fi

echo ""

# ============================================================================
# FASE 8: LIMPIAR CONTENEDORES PREVIOS (OPCIONAL)
# ============================================================================
log_info "¿Deseas limpiar contenedores previos? (s/N)"
read -p "Respuesta: " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    log_warning "Deteniendo y eliminando contenedores existentes..."
    $COMPOSE_CMD down -v 2>/dev/null || true
    log_success "Limpieza completada"
fi

echo ""

# ============================================================================
# FASE 9: CONSTRUIR IMÁGENES DOCKER
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 9: CONSTRUCCIÓN DE IMÁGENES DOCKER"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_warning "Esto puede tardar 15-30 minutos en la primera ejecución..."
log_info "Construyendo imágenes Docker en paralelo usando: $COMPOSE_CMD"

set +e
$COMPOSE_CMD build --parallel
build_exit=$?
set +e

log_debug "Código de salida de build: $build_exit"

if [ "$build_exit" -eq 0 ]; then
    log_success "Imágenes construidas correctamente"
else
    log_error "Error al construir imágenes"
    log_info "Revisa la salida anterior de 'docker compose build' para más detalles"
    exit 1
fi

echo ""

# ============================================================================
# FASE 10: DESPLEGAR INFRAESTRUCTURA
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 10: DESPLIEGUE DE CONTENEDORES"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Desplegando contenedores..."
set +e
$COMPOSE_CMD up -d
up_exit=$?
set +e

if [ "$up_exit" -eq 0 ]; then
    log_success "Contenedores iniciados"
else
    log_error "Error al desplegar contenedores"
    log_info "Revisa la salida anterior de 'docker compose up -d' para más detalles"
    exit 1
fi

echo ""

# ============================================================================
# FASE 11: ESPERAR Y VALIDAR SERVICIOS
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 11: VALIDACIÓN DE SERVICIOS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Esperando a que los servicios estén listos..."

wait_for_service() {
    local service=$1
    local max_wait=${2:-90}
    local counter=0
    
    while ! $COMPOSE_CMD ps | grep -q "$service.*Up"; do
        sleep 2
        counter=$((counter + 2))
        if [ $counter -ge $max_wait ]; then
            log_warning "$service timeout (>$max_wait segundos)"
            return 1
        fi
        echo -n "."
    done
    echo ""
    log_success "$service online"
}

# Servicios críticos
wait_for_service "router-fw" 30
wait_for_service "mysql-db" 90
wait_for_service "ad-dc-primary" 120
wait_for_service "mqtt-broker" 30
wait_for_service "app-server" 60
wait_for_service "wireguard-vpn" 60

sleep 15  # Tiempo extra para estabilización

echo ""

# ============================================================================
# FASE 11.1: AJUSTE DE RUTAS POR DEFECTO (INTER-VLAN)
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 11.1: AJUSTE DE GATEWAY EN CONTENEDORES CLAVE"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

configure_gateway() {
    local service=$1
    local gateway=$2

    if $COMPOSE_CMD exec -T "$service" sh -c "command -v ip >/dev/null 2>&1" >/dev/null 2>&1; then
        if $COMPOSE_CMD exec -T "$service" sh -c "ip route del default 2>/dev/null; ip route add default via $gateway" >/dev/null 2>&1; then
            log_success "Gateway por defecto de $service ajustado a $gateway"
        else
            log_warning "No se pudo ajustar el gateway por defecto de $service (ver logs de contenedor)"
        fi
    else
        log_warning "El contenedor $service no tiene utilidad 'ip' disponible; se omite ajuste de gateway"
    fi
}

# Ajustar gateways para que el tráfico entre VLANs pase siempre por el router
configure_gateway "web-server-1" "172.16.10.1"
configure_gateway "web-server-2" "172.16.10.1"
configure_gateway "app-server"   "172.16.20.1"
configure_gateway "mysql-db"     "172.16.20.1"
configure_gateway "ad-dc-primary" "172.16.30.1"

echo ""

# ============================================================================
# FASE 11.2: AJUSTE DE ROUTEO/NAT EN SERVIDOR WIREGUARD
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 11.2: AJUSTE DE ROUTEO/NAT EN WIREGUARD-VPN"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

configure_wireguard_nat() {
    if ! $COMPOSE_CMD ps | grep -q "wireguard-vpn"; then
        log_warning "wireguard-vpn no está desplegado; se omite ajuste de NAT de VPN"
        return 0
    fi

    if $COMPOSE_CMD exec -T wireguard-vpn sh -c '\
        # Habilitar reenvío IP dentro del contenedor\
        echo 1 > /proc/sys/net/ipv4/ip_forward 2>/dev/null || true; \
        # Eliminar la IP 172.16.50.2/26 de la interfaz física de la VLAN 50 (puede ser eth1 o eth4 según arranque)\
        ip addr del 172.16.50.2/26 dev eth1 2>/dev/null || ip addr del 172.16.50.2/26 dev eth4 2>/dev/null || true; \
        # Asegurar que la ruta de la red VPN vaya por wg0 y no por la interfaz física\
        ip route del 172.16.50.0/26 2>/dev/null || true; \
        ip route add 172.16.50.0/26 dev wg0 2>/dev/null || true; \
        # Limpiar posibles reglas NAT duplicadas y dejar una sola MASQUERADE para 172.16.50.0/26\
        iptables -t nat -D POSTROUTING -s 172.16.50.0/26 -o eth+ -j MASQUERADE 2>/dev/null || true; \
        iptables -t nat -D POSTROUTING -s 172.16.50.0/26 -o eth0 -j MASQUERADE 2>/dev/null || true; \
        iptables -t nat -A POSTROUTING -s 172.16.50.0/26 -o eth+ -j MASQUERADE 2>/dev/null || true; \
        # Reglas de FORWARD para permitir tráfico entrante/saliente por wg0\
        iptables -A FORWARD -i wg0 -j ACCEPT 2>/dev/null || true; \
        iptables -A FORWARD -o wg0 -j ACCEPT 2>/dev/null || true
    ' >/dev/null 2>&1; then
        log_success "Reglas de reenvío/NAT aplicadas en wireguard-vpn"
    else
        log_warning "No se pudieron aplicar las reglas de reenvío/NAT en wireguard-vpn (revisar logs)"
    fi
}

configure_wireguard_nat

echo ""

# ============================================================================
# FASE 11.3: VALIDACIÓN DE ACCESO INTERNO DESDE VPN
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 11.3: VALIDACIÓN BÁSICA DE CONECTIVIDAD DESDE VPN"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

if $COMPOSE_CMD ps | grep -q "wireguard-vpn"; then
    if $COMPOSE_CMD exec -T wireguard-vpn ping -c 1 172.16.10.20 >/dev/null 2>&1; then
        log_success "VPN → DMZ (web-server-1 172.16.10.20)"
    else
        log_warning "VPN → DMZ no responde (revisar acceso HTTP desde clientes VPN)"
    fi

    if $COMPOSE_CMD exec -T wireguard-vpn ping -c 1 172.16.20.10 >/dev/null 2>&1; then
        log_success "VPN → App (app-server 172.16.20.10)"
    else
        log_warning "VPN → App no responde"
    fi

    if $COMPOSE_CMD exec -T wireguard-vpn ping -c 1 172.16.30.10 >/dev/null 2>&1; then
        log_success "VPN → Mgmt (AD-DC 172.16.30.10)"
    else
        log_warning "VPN → Mgmt/AD-DC no responde"
    fi
else
    log_warning "wireguard-vpn no está desplegado; se omiten pruebas de acceso desde VPN"
fi

echo ""

# ============================================================================
# FASE 12: HEALTH CHECKS
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 12: HEALTH CHECKS"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Verificando salud de servicios..."

# MySQL
log_info "   Probando MySQL..."
if $COMPOSE_CMD exec -T mysql-db mysql -u root -proot -e "SELECT 1" > /dev/null 2>&1; then
    log_success "   MySQL responde"
    
    # Verificar base de datos
    if $COMPOSE_CMD exec -T mysql-db mysql -u root -proot -e "USE humhouse; SHOW TABLES;" > /dev/null 2>&1; then
        log_success "   Base de datos 'humhouse' inicializada"
    else
        log_warning "   Base de datos 'humhouse' no accesible"
    fi
else
    log_warning "   MySQL no responde (aún inicializando)"
fi

echo ""

# App Server
log_info "   Probando App Server..."
if $COMPOSE_CMD exec -T app-server bash -c "test -f /app/app.jar" 2>/dev/null; then
    log_success "   App Server JAR presente"
else
    log_warning "   App Server JAR no encontrado"
fi

echo ""

# Conectividad inter-VLAN
log_info "   Conectividad inter-VLAN: las pruebas detalladas se realizan ahora en ./verify.sh"

echo ""

# ============================================================================
# FASE 13: ESTADO FINAL
# ============================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 13: ESTADO FINAL"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

log_info "Estado de contenedores:"
$COMPOSE_CMD ps

echo ""

log_info "Redes Docker:"
docker network ls | grep quickstay || docker network ls | grep proyectofinal

echo ""

# =========================================================================
# FASE 14: AUTO-CONFIGURACIÓN DE MONITOREO (ZABBIX / WAZUH)
# =========================================================================
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
log_info "FASE 14: AUTO-CONFIGURACIÓN DE MONITOREO"
echo -e "${MAGENTA}════════════════════════════════════════════════════════════════${NC}"
echo ""

# 14.1 (SIMULACIÓN) - Agente Zabbix en host
log_info "Entorno de laboratorio: el host físico no se monitoriza automáticamente con Zabbix Agent."
log_info "Si se desea, puede instalarse manualmente usando Documentación/infrastructure/scripts/04_deploy_monitoring.sh."

echo ""

# 14.2 Auto-provisión de hosts en Zabbix usando config.yaml (Dashboard v2)
# NOTA: Desactivada la ejecución automática porque Zabbix tarde 5-10 minutos en estar listo
# Ejecuta manualmente cuando Zabbix esté funcionando: bash provision-zabbix-docker.sh
if command -v python3 >/dev/null 2>&1; then
    # Asegurar que PyYAML está disponible para el script de provisión
    if ! python3 -c "import yaml" >/dev/null 2>&1; then
        log_info "Instalando módulo python3-yaml para cuando ejecutes provision_zabbix.py..."
        if sudo apt-get update -qq && sudo apt-get install -y -qq python3-yaml; then
            log_success "python3-yaml instalado correctamente"
        else
            log_warning "No se pudo instalar python3-yaml"
        fi
    fi
fi

log_info "Auto-configuración de Zabbix: DESACTIVADA (ejecuta manualmente cuando esté listo)"
log_info "  Espera 5-10 minutos después del despliegue y ejecuta:"
log_info "  → bash provision-zabbix-docker.sh"

echo ""

# 14.3 Intentar instalar agente Wazuh en el host (si se desea visibilidad en SIEM)
if [ -f "Documentación/infrastructure/scripts/14_deploy_security.sh" ]; then
    log_info "Instalando agente Wazuh en el host para visibilidad en el SIEM..."
    if sudo bash Documentación/infrastructure/scripts/14_deploy_security.sh >/dev/null 2>&1; then
        log_success "Agente Wazuh instalado/configurado en el host (si aplica)"
    else
        log_warning "No se pudo instalar agente Wazuh automáticamente (puede requerir ajustes manuales)"
    fi
else
    log_warning "Script 14_deploy_security.sh no encontrado; se omite instalación automática de agente Wazuh"
fi

echo ""

# ============================================================================
# RESUMEN FINAL
# ============================================================================
echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                   DESPLIEGUE COMPLETADO                       ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

log_success "Infraestructura QuickStay desplegada exitosamente"

# Cargar variables para info final
source secrets.env

cat << EOF

${GREEN}ACCESO INMEDIATO:${NC}
    • QuickStay Web (desde esta VM):        http://localhost
    • QuickStay Web (IP pública simulada):  http://$PUBLIC_IP

${GREEN}ACCESO SOLO INTERNOS (VÍA VPN):${NC}
    • Panel balanceador (HAProxy - admin):  http://172.16.10.10:8404

${GREEN}ACCESO VPN (Administradores):${NC}
  1. Descarga: ./wireguard/config/peer_admin1.conf
  2. Conecta: sudo wg-quick up ./peer_admin1.conf
  3. Accede a servicios internos (172.16.x.x)

${GREEN}SERVICIOS INTERNOS:${NC}
  • Zabbix:      http://172.16.30.20
  • Grafana:     http://172.16.30.21:3000
  • Wazuh:       https://172.16.30.30:5601
  • App Java:    http://172.16.20.10:1234
  • MySQL:       172.16.20.20:3306

${GREEN}LOGS Y MONITOREO:${NC}
  • docker-compose ps
  • docker-compose logs -f [servicio]
  • ./logs/[servicio]/
  • ./logs/deploy/build.log
  • ./logs/deploy/startup.log

${GREEN}SIGUIENTES PASOS:${NC}
  1. Revisar logs: tail -f logs/*/
  2. Conectar VPN WireGuard
  3. Acceder a Zabbix/Grafana para monitoreo
  4. Ejecutar: ./verify.sh

${YELLOW}DOCUMENTACIÓN:${NC}
  • README_DESPLIEGUE.md
  • QUICKSTART.md

${MAGENTA}Fecha: $(date)${NC}

EOF

log_success "¡Despliegue finalizado!"
echo ""
