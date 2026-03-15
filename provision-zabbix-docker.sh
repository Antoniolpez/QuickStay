#!/bin/bash
# Script para ejecutar provisioning de Zabbix desde un contenedor temporal
# Esto permite acceder a las redes internas de Docker

cd "$(dirname "$0")"

echo "🔧 Ejecutando provisioning de Zabbix desde contenedor temporal..."

docker run --rm \
  --network=proyectofinal_mgmt_net \
  -v "$(pwd)/monitoring":/monitoring \
  -v "$(pwd)/Documentación/dashboard-v2/backend/config.yaml":/config.yaml \
  python:3.9-slim \
  sh -c "pip install -q pyyaml requests && python /monitoring/provision_zabbix.py"

echo "✅ Provisioning completado"
