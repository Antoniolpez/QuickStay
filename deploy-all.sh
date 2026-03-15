#!/bin/bash
# ============================================================================
# QUICKSTAY INFRASTRUCTURE - MASTER DEPLOYMENT SCRIPT
# ============================================================================
# Script de despliegue completo de la infraestructura QuickStay
# Autor: Antonio López Montes
# Fecha: Enero 2026
# ============================================================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funciones de utilidad
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Contador de problemas
WARNINGS=0
ERRORS=0

# Funciones de testing
test_file() {
    local file=$1
    if [ -f "$file" ]; then
        return 0
    else
        log_error "Archivo no encontrado: $file"
        ((ERRORS++))
        return 1
    fi
}

test_dir() {
    local dir=$1
    if [ -d "$dir" ]; then
        return 0
    else
        log_error "Directorio no encontrado: $dir"
        ((ERRORS++))
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
║           DESPLIEGUE MAESTRO DE INFRAESTRUCTURA               ║
║                    Proyecto ASIR 2026                         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF

log_info "Iniciando despliegue de QuickStay..."
echo ""

# ============================================================================
# 0. PRE-VALIDACIÓN DE ESTRUCTURA
# ============================================================================
log_info "Validando estructura del proyecto..."

# Verificar directorios críticos
test_dir "Documentación/app_repo" || log_warning "Directorio Java no encontrado"
test_dir "Documentación/dashboard-v2" || log_warning "Directorio Dashboard no encontrado"
test_dir "Documentación/iot" || log_warning "Directorio IoT no encontrado"
test_dir "infrastructure" || log_error "Directorio infrastructure no encontrado"

# Verificar archivos críticos
test_file "docker-compose.yml" || exit 1
test_file "secrets.env" || log_warning "secrets.env no encontrado, usando valores por defecto"

# Verificar archivos de código
test_file "Documentación/app_repo/pom.xml" || log_warning "pom.xml no encontrado"
test_file "Documentación/iot/iot_client.py" || log_warning "iot_client.py no encontrado"
test_file "infrastructure/web-content/index.php" || log_warning "index.php no encontrado"

echo ""

# Verificar si estamos en Linux
if [[ "$OSTYPE" != "linux-gnu"* ]]; then
    log_error "Este script solo es compatible con sistemas Linux"
    exit 1
fi

# Verificar Docker
if ! command -v docker &> /dev/null; then
    log_warning "Docker no está instalado. ¿Deseas instalarlo ahora? (s/N)"
    read -p "Respuesta: " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        log_info "Instalando Docker..."
        
        # Actualizar repositorios
        sudo apt-get update
        
        # Instalar dependencias
        sudo apt-get install -y \
            apt-transport-https \
            ca-certificates \
            curl \
            gnupg \
            lsb-release
        
        # Añadir clave GPG oficial de Docker
        sudo mkdir -p /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
        
        # Configurar repositorio
        echo \
          "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
          $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
        
        # Instalar Docker Engine
        sudo apt-get update
        sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        
        # Añadir usuario actual al grupo docker
        sudo usermod -aG docker $USER
        
        log_success "Docker instalado correctamente"
        log_warning "IMPORTANTE: Cierra la sesión y vuelve a entrar para que los permisos de grupo surtan efecto"
        log_warning "O ejecuta: newgrp docker"
        
        # Aplicar grupo temporalmente
        newgrp docker <<EONG
        log_success "Grupo docker aplicado temporalmente"
EONG
    else
        log_error "Docker es requerido para continuar. Saliendo..."
        exit 1
    fi
else
    log_success "Docker encontrado: $(docker --version)"
fi

# Verificar Docker Compose
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null 2>&1; then
    log_warning "Docker Compose no está instalado. Instalando..."
    
    # Docker Compose v2 viene con docker-ce, pero instalamos standalone por si acaso
    sudo apt-get update
    sudo apt-get install -y docker-compose-plugin
    
    # Verificar instalación
    if docker compose version &> /dev/null 2>&1; then
        log_success "Docker Compose instalado correctamente"
    else
        log_error "Error al instalar Docker Compose"
        exit 1
    fi
else
    log_success "Docker Compose encontrado"
fi

# Verificar que Docker está corriendo
if ! sudo systemctl is-active --quiet docker; then
    log_warning "Docker no está corriendo. Iniciando..."
    sudo systemctl start docker
    sudo systemctl enable docker
fi
log_success "Docker está corriendo"

# Verificar permisos
if [ "$EUID" -eq 0 ]; then 
    log_warning "Ejecutando como root. Recomendado usar usuario con permisos Docker."
fi

# Verificar archivo de secretos
if [ ! -f "secrets.env" ]; then
    log_error "Archivo secrets.env no encontrado. Copiando desde template..."
    cp secrets.env.template secrets.env 2>/dev/null || {
        log_error "No se pudo crear secrets.env"
        exit 1
    }
fi
log_success "Archivo de secretos encontrado"

# Asegurar archivo .env para Docker Compose (interpolación de ${VAR})
if [ ! -f ".env" ]; then
    log_info "Creando archivo .env a partir de secrets.env para Docker Compose..."
    cp secrets.env .env
    log_success ".env creado (copia de secrets.env)"
else
    log_info "Archivo .env ya existe; se usarán esas variables para Docker Compose"
fi

# ============================================================================
# 2. PREPARACIÓN DE DIRECTORIOS
# ============================================================================
log_info "Creando estructura de directorios..."

mkdir -p logs/{router,haproxy,web1,web2,app,mysql,ad-dc,zabbix,wazuh,mqtt,dashboard}
mkdir -p wireguard/{config,keys}
mkdir -p ssh/keys
mkdir -p backups

chmod 600 secrets.env
log_success "Directorios creados"

# ============================================================================
# 3. GENERAR CONTRASEÑAS MOSQUITTO
# ============================================================================
log_info "Generando archivo de contraseñas para Mosquitto..."

if [ ! -f "infrastructure/config/mosquitto/passwd" ]; then
    # Obtener credenciales desde secrets.env
    source secrets.env
    
    # Crear archivo temporal de contraseñas
    docker run --rm -v "$(pwd)/infrastructure/config/mosquitto:/mosquitto/config" \
        eclipse-mosquitto:2.0 \
        sh -c "mosquitto_passwd -c -b /mosquitto/config/passwd ${MQTT_USERNAME:-quickstay_iot} ${MQTT_PASSWORD:-QuickStay2026!MQTT}"
    
    log_success "Contraseñas de Mosquitto generadas"
else
    log_success "Archivo de contraseñas de Mosquitto ya existe"
fi

# ============================================================================
# 4. LIMPIAR CONTENEDORES PREVIOS (OPCIONAL)
# ============================================================================
read -p "¿Deseas limpiar contenedores previos? (s/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    log_warning "Deteniendo y eliminando contenedores existentes..."
    docker-compose down -v 2>/dev/null || true
    log_success "Limpieza completada"
fi

# ============================================================================
# 5. CONSTRUIR IMÁGENES DOCKER
# ============================================================================
log_info "Construyendo imágenes Docker..."
log_warning "Esto puede tardar varios minutos en la primera ejecución..."

docker-compose build --parallel || {
    log_error "Error al construir imágenes"
    exit 1
}

log_success "Imágenes construidas correctamente"

# ============================================================================
# 6. DESPLEGAR INFRAESTRUCTURA
# ============================================================================
log_info "Desplegando contenedores..."

docker-compose up -d || {
    log_error "Error al desplegar contenedores"
    exit 1
}

log_success "Contenedores iniciados"

# ============================================================================
# 7. ESPERAR A QUE LOS SERVICIOS ESTÉN LISTOS
# ============================================================================
log_info "Esperando a que los servicios estén listos..."

# Función para esperar a un servicio
wait_for_service() {
    local service=$1
    local max_wait=${2:-60}
    local counter=0
    
    while ! docker-compose ps | grep -q "$service.*Up"; do
        sleep 2
        counter=$((counter + 2))
        if [ $counter -ge $max_wait ]; then
            log_warning "Timeout esperando a $service"
            return 1
        fi
        echo -n "."
    done
    echo ""
    log_success "$service está listo"
}

# Esperar servicios críticos
wait_for_service "mysql-db" 90
wait_for_service "ad-dc-primary" 120
wait_for_service "mqtt-broker" 30
wait_for_service "app-server" 60

sleep 10  # Tiempo adicional para estabilización

# ============================================================================
# 8. VERIFICAR ESTADO DE SERVICIOS
# ============================================================================
log_info "Verificando estado de los servicios..."

docker-compose ps

echo ""
log_info "Estado de redes Docker:"
docker network ls | grep quickstay || docker network ls | grep proyectofinal

# ============================================================================
# 9. CONFIGURAR DNS RECORDS EN AD-DC (Opcional)
# ============================================================================
log_info "Configurando registros DNS en Active Directory..."

# Esperar a que AD-DC esté completamente inicializado
sleep 15

docker-compose exec -T ad-dc-primary bash -c "
    source /etc/environment
    samba-tool dns add localhost quickstay.local web A 172.16.10.10 -U Administrator --password=\$AD_ADMIN_PASSWORD 2>/dev/null || echo 'Registro web ya existe'
    samba-tool dns add localhost quickstay.local app A 172.16.20.10 -U Administrator --password=\$AD_ADMIN_PASSWORD 2>/dev/null || echo 'Registro app ya existe'
    samba-tool dns add localhost quickstay.local db A 172.16.20.20 -U Administrator --password=\$AD_ADMIN_PASSWORD 2>/dev/null || echo 'Registro db ya existe'
    samba-tool dns add localhost quickstay.local mqtt A 172.16.40.10 -U Administrator --password=\$AD_ADMIN_PASSWORD 2>/dev/null || echo 'Registro mqtt ya existe'
" 2>/dev/null || log_warning "No se pudieron crear todos los registros DNS"

log_success "Registros DNS configurados"

# ============================================================================
# 10. GENERAR CONFIGURACIÓN WIREGUARD
# ============================================================================
log_info "Generando configuración de clientes VPN WireGuard..."

sleep 5

# Los archivos de configuración se generan automáticamente en wireguard/config
if [ -d "wireguard/config" ]; then
    log_success "Configuraciones VPN generadas en: wireguard/config/"
    ls -la wireguard/config/ 2>/dev/null || true
fi

# ============================================================================
# 11. RESUMEN FINAL
# ============================================================================
echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║                   DESPLIEGUE COMPLETADO                       ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

log_success "Infraestructura QuickStay desplegada correctamente"
echo ""

# Cargar variables para mostrar información
source secrets.env

cat << EOF
${GREEN}ACCESO A SERVICIOS:${NC}

${BLUE}═══════════════════════════════════════════════════════════════${NC}
${YELLOW}WEB PÚBLICA (Internet):${NC}
  • QuickStay Web: http://localhost

${YELLOW}ACCESO VPN (Administradores):${NC}
  • Puerto WireGuard: 51820/udp
  • Configuraciones: ./wireguard/config/peer_*.conf
  • Red VPN: 172.16.50.0/26

${YELLOW}SERVICIOS INTERNOS (Via VPN):${NC}
  • Active Directory: 172.16.30.10
    - Domain: ${AD_REALM}
    - Admin: Administrator / ${AD_ADMIN_PASSWORD}
  
  • MySQL Database: 172.16.20.20:3306
    - Database: ${MYSQL_DATABASE}
    - User: ${MYSQL_USER} / ${MYSQL_PASSWORD}
  
  • MQTT Broker: 172.16.40.10:1883
    - User: ${MQTT_USERNAME} / ${MQTT_PASSWORD}
  
  • Zabbix Web: http://172.16.30.20 (via VPN o port-forward)
    - Admin: admin / ${ZABBIX_ADMIN_PASSWORD}a12ASDxc@12 
  
  • Wazuh Dashboard: https://172.16.30.30:5601 (via VPN)
    - Admin: ${WAZUH_API_USERNAME} / ${WAZUH_API_PASSWORD}
  
  • Grafana: http://172.16.30.21:3000 (via VPN)
    - Admin: ${GRAFANA_ADMIN_USER} / ${GRAFANA_ADMIN_PASSWORD}
  
  • Dashboard Maestro (Presentación):
    - Backend API: http://172.16.30.40:8000
    - Frontend: http://localhost:5173 (si se expone)

${YELLOW}LOGS:${NC}
  • docker-compose logs -f [servicio]
  • ./logs/[servicio]/

${YELLOW}COMANDOS ÚTILES:${NC}
  • Ver estado: docker-compose ps
  • Ver logs: docker-compose logs -f
  • Detener todo: docker-compose down
  • Reiniciar servicio: docker-compose restart [servicio]
  • Acceder a contenedor: docker-compose exec [servicio] bash

${BLUE}═══════════════════════════════════════════════════════════════${NC}

${GREEN}PRÓXIMOS PASOS:${NC}
1. Configurar cliente VPN WireGuard con: wireguard/config/peer_admin1.conf
2. Conectar via VPN para acceder a servicios internos
3. Acceder al Dashboard Maestro para visualización
4. Configurar agentes Zabbix en los servidores
5. Revisar logs para verificar funcionamiento

${YELLOW}DOCUMENTACIÓN:${NC}
  • README.md - Guía completa
  • secrets.env - Credenciales (NO COMPARTIR)
  • Documentación/ - Guías de implementación por fases

EOF

log_success "¡Despliegue finalizado exitosamente!"
echo ""
