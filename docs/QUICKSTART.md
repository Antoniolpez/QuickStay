#!/bin/bash
# ============================================================================
# GUÍA DE INSTALACIÓN RÁPIDA - QUICKSTAY
# ============================================================================

cat << "EOF"
╔════════════════════════════════════════════════════════════════╗
║     QUICKSTAY - GUÍA INSTALACIÓN RÁPIDA                        ║
║     Despliegue en 3 pasos                                      ║
╚════════════════════════════════════════════════════════════════╝

PASO 1: PREPARAR VM
═══════════════════════════════════════════════════════════════

1. Crear VM con:
   - OS: Ubuntu Server 22.04 LTS
   - CPU: 4+ cores
   - RAM: 16+ GB
   - Disco: 80+ GB

2. Conectarse a la VM y ejecutar:
   wget https://raw.github.com/.../setup-vm.sh
   bash setup-vm.sh

   (Este script instala Docker automáticamente)

   O si ya tienes Docker:
   docker --version  # Verificar que exista


PASO 2: DESCARGAR PROYECTO
═══════════════════════════════════════════════════════════════

1. Descargar el repositorio:
   git clone <URL-REPO> quickstay
   cd quickstay

   O si está comprimido:
   unzip ProyectoFinal.zip
   cd ProyectoFinal


PASO 3: EJECUTAR DESPLIEGUE
═══════════════════════════════════════════════════════════════

1. Hacer ejecutable el script:
   chmod +x deploy-all.sh

2. Ejecutar despliegue (automático):
   ./deploy-all.sh

   El script:
   ✓ Instala Docker (si no existe)
   ✓ Verifica requisitos
   ✓ Construye imágenes
   ✓ Despliega contenedores
   ✓ Espera servicios listos
   ✓ Configura DNS y VPN

3. Esperar a que termine (10-20 minutos)


ACCESO A SERVICIOS
═══════════════════════════════════════════════════════════════

Inmediatamente disponible:
  ✓ QuickStay Web (Pública):    http://localhost
  ✓ HAProxy Stats:              http://localhost:8404


Para acceso interno (via VPN):
  1. Obtener cliente WireGuard:
     - Linux:   sudo apt install wireguard-tools wireguard
     - Windows: https://www.wireguard.com/install/
     - macOS:   https://www.wireguard.com/install/

  2. Descargar configuración desde VM:
     ./wireguard/config/peer_admin1.conf

  3. Conectar:
     sudo wg-quick up peer_admin1.conf

  4. Acceder a servicios:
     - Zabbix:      http://172.16.30.20
     - Grafana:     http://172.16.30.21:3000
     - Wazuh:       https://172.16.30.30:5601
     - App:         http://172.16.20.10:1234
     - MySQL:       172.16.20.20:3306


COMANDOS ÚTILES
═══════════════════════════════════════════════════════════════

# Ver estado
docker-compose ps

# Ver logs
docker-compose logs -f

# Reiniciar servicio
docker-compose restart mysql-db

# Parar todo
docker-compose down

# Entrar en contenedor
docker-compose exec app-server bash


CREDENCIALES
═══════════════════════════════════════════════════════════════

Ver archivo: secrets.env

Principales:
  • Domain: QUICKSTAY.LOCAL
  • MySQL User: quickstay_app
  • MQTT User: quickstay_iot
  • Zabbix Admin: admin
  • Grafana Admin: admin
  • Wazuh Admin: admin


SOLUCIÓN DE PROBLEMAS
═══════════════════════════════════════════════════════════════

"Docker: command not found"
  → Ejecutar: sudo apt install -y docker.io docker-compose
  → O ejecutar: bash setup-vm.sh

Contenedor no inicia
  → Ver logs: docker-compose logs mysql-db
  → Reintentar: docker-compose restart mysql-db

Sin conexión a servicios
  → Verificar VPN servidor: docker-compose exec router-fw wg show
  → Verificar contenedores: docker-compose ps
  → Ver firewall: docker-compose exec router-fw nft list ruleset


DOCUMENTACIÓN COMPLETA
═══════════════════════════════════════════════════════════════

Leer: README_DESPLIEGUE.md


SOPORTE
═══════════════════════════════════════════════════════════════

Proyecto ASIR 2026
Autor: Antonio López Montes

EOF
