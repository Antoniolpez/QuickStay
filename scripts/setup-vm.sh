#!/bin/bash
# ============================================================================
# SETUP-VM.SH - Script de preparación inicial de VM
# ============================================================================
# Prepara una VM virgen (Ubuntu 22.04) para ejecutar QuickStay
# Uso: wget https://raw.github.com/...setup-vm.sh && bash setup-vm.sh
# ============================================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[✓]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[⚠]${NC} $1"; }
log_error() { echo -e "${RED}[✗]${NC} $1"; }

clear
cat << "EOF"
╔════════════════════════════════════════════════════════════════╗
║   QUICKSTAY - SETUP VM                                        ║
║   Preparación inicial de máquina virtual                       ║
╚════════════════════════════════════════════════════════════════╝
EOF
echo ""

# Verificar permisos
if [[ $EUID -ne 0 ]]; then
   log_error "Este script debe ejecutarse con sudo"
   exit 1
fi

log_info "Iniciando configuración de VM..."

# ============================================================================
# 1. ACTUALIZAR SISTEMA
# ============================================================================
log_info "Actualizando sistema operativo..."
apt-get update -qq
apt-get upgrade -y -qq
log_success "Sistema actualizado"

# ============================================================================
# 2. INSTALAR DEPENDENCIAS BÁSICAS
# ============================================================================
log_info "Instalando herramientas esenciales..."
apt-get install -y -qq \
    curl \
    wget \
    git \
    vim \
    nano \
    net-tools \
    htop \
    iotop \
    ca-certificates \
    gnupg \
    lsb-release \
    sudo

log_success "Herramientas esenciales instaladas"

# ============================================================================
# 3. INSTALAR DOCKER
# ============================================================================
log_info "Instalando Docker..."

# Añadir clave GPG oficial de Docker
mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Configurar repositorio
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar
apt-get update -qq
apt-get install -y -qq \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin

# Habilitar servicio
systemctl enable docker
systemctl start docker

log_success "Docker instalado: $(docker --version)"

# ============================================================================
# 4. INSTALAR DOCKER COMPOSE STANDALONE (V1 - Compatible)
# ============================================================================
log_info "Instalando Docker Compose standalone..."
apt-get install -y -qq docker-compose
log_success "Docker Compose instalado"

# ============================================================================
# 5. CREAR USUARIO NO-ROOT (OPCIONAL)
# ============================================================================
log_info "¿Deseas crear un usuario no-root para ejecutar Docker? (s/N)"
read -p "Respuesta: " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    log_info "¿Nombre de usuario? (default: quickstay)"
    read -p "Usuario: " username
    username=${username:-quickstay}
    
    if ! id "$username" &>/dev/null; then
        useradd -m -s /bin/bash "$username"
        usermod -aG docker "$username"
        usermod -aG sudo "$username"
        log_success "Usuario '$username' creado"
    else
        usermod -aG docker "$username"
        log_warning "Usuario '$username' ya existe, añadido a grupo docker"
    fi
else
    log_warning "Se ejecutará con permisos de root"
fi

# ============================================================================
# 6. AUMENTAR LÍMITES DOCKER
# ============================================================================
log_info "Configurando límites de sistema para Docker..."

# Aumentar descriptores de archivo
if ! grep -q "fs.file-max" /etc/sysctl.conf; then
    echo "fs.file-max = 2097152" >> /etc/sysctl.conf
fi

if ! grep -q "vm.max_map_count" /etc/sysctl.conf; then
    echo "vm.max_map_count = 262144" >> /etc/sysctl.conf
fi

sysctl -p > /dev/null 2>&1
log_success "Límites de sistema configurados"

# ============================================================================
# 7. CREAR DIRECTORIOS PARA DATOS
# ============================================================================
log_info "Creando estructura de directorios..."
mkdir -p /var/lib/quickstay/{data,logs,backups,config}
chmod -R 755 /var/lib/quickstay
log_success "Directorios creados en /var/lib/quickstay"

# ============================================================================
# 8. VERIFICACIÓN FINAL
# ============================================================================
log_info "Realizando verificaciones finales..."

echo ""
log_info "Estado del sistema:"
echo "  Docker:    $(docker --version)"
echo "  Compose:   $(docker-compose --version)"
echo "  OS:        $(lsb_release -ds)"
echo "  Kernel:    $(uname -r)"
echo ""

# Verificar conexión a Docker daemon
if docker info > /dev/null 2>&1; then
    log_success "Docker daemon funciona correctamente"
else
    log_warning "No se puede conectar a Docker daemon"
fi

# ============================================================================
# 9. DESCARGAR QUICKSTAY (OPCIONAL)
# ============================================================================
echo ""
log_info "¿Descargar el repositorio QuickStay? (s/N)"
read -p "Respuesta: " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    log_info "¿URL del repositorio?"
    read -p "URL (default: https://github.com/...quickstay.git): " repo_url
    repo_url=${repo_url:-"https://github.com/ejemplo/quickstay.git"}
    
    log_info "¿Directorio de destino? (default: ~/quickstay)"
    read -p "Ruta: " dest_dir
    dest_dir=${dest_dir:-"$HOME/quickstay"}
    
    if [ ! -d "$dest_dir" ]; then
        git clone "$repo_url" "$dest_dir"
        log_success "Repositorio clonado en: $dest_dir"
    else
        log_warning "Directorio ya existe: $dest_dir"
    fi
fi

# ============================================================================
# RESUMEN FINAL
# ============================================================================
clear
cat << "EOF"
╔════════════════════════════════════════════════════════════════╗
║   CONFIGURACIÓN COMPLETADA                                    ║
╚════════════════════════════════════════════════════════════════╝
EOF
echo ""

cat << EOF
${GREEN}✓ VM Lista para QuickStay${NC}

${BLUE}Próximos pasos:${NC}
1. Ir al directorio del proyecto:
   cd ~/quickstay

2. Ejecutar el despliegue maestro:
   chmod +x deploy-all.sh
   ./deploy-all.sh

3. Esperar a que se complete el despliegue (10-20 minutos)

${BLUE}Información del Sistema:${NC}
  • Docker:    $(docker --version)
  • Compose:   $(docker-compose --version)
  • Espacio:   $(df -h / | tail -1)
  • RAM:       $(free -h | grep Mem | awk '{print $2}')

${YELLOW}Notas importantes:${NC}
  • Si ejecutaste con user root, considera crear un usuario:
    sudo useradd -m -s /bin/bash quickstay
    sudo usermod -aG docker quickstay

  • Para aplicar cambios de grupo sin logout:
    newgrp docker

  • Revisa logs de Docker:
    journalctl -u docker -f

${BLUE}Documentación:${NC}
  • README.md          - Guía completa
  • INSTALL.md         - Instalación detallada
  • docker-compose.yml - Configuración de servicios
  • secrets.env        - Credenciales (CAMBIAR)

EOF

log_success "¡Setup completado!"
echo ""
