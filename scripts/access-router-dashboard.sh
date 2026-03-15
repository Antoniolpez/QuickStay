#!/bin/bash
# Script para verificar y acceder al Router Dashboard

set -e

echo "🔧 Router Dashboard - Verificación y Acceso"
echo "==========================================="
echo ""

# Verificar que router-ui está en ejecución
if docker-compose ps router-ui | grep -q "Up"; then
    echo "✅ Router-UI está ejecutándose"
else
    echo "❌ Router-UI no está ejecutándose"
    echo "Iniciando router-ui..."
    docker-compose up -d router-ui
    sleep 5
fi

# Obtener IP del router-ui
ROUTER_UI_IP=$(docker-compose exec -T router-ui ip addr show | grep "172.16.30" | grep -oE '172\.16\.30\.[0-9]+' | head -1)

if [ -z "$ROUTER_UI_IP" ]; then
    ROUTER_UI_IP="172.16.30.50"
fi

echo "📡 Router-UI IP: $ROUTER_UI_IP"
echo ""

# Probar acceso a la API
echo "🔍 Verificando API..."
if docker-compose exec -T ad-dc-primary curl -s -I http://$ROUTER_UI_IP:5000/api/health | grep -q "200"; then
    echo "✅ API es accesible"
else
    echo "⚠️  API not responding yet, waiting..."
    sleep 5
fi

echo ""
echo "📊 ACCESO AL DASHBOARD:"
echo "======================"
echo ""
echo "URL:     http://$ROUTER_UI_IP:5000"
echo "Acceso:  Solo desde VLAN Management (VPN/LAN interna)"
echo ""

# Información de acceso
if command -v wg >/dev/null 2>&1; then
    echo "OPCIÓN 1 - Desde VPN WireGuard:"
    echo "  1. Conectar a VPN:"
    echo "     sudo wg-quick up ./wireguard/config/quickstay.conf"
    echo "  2. Abrir en navegador:"
    echo "     http://$ROUTER_UI_IP:5000"
    echo ""
fi

echo "OPCIÓN 2 - Acceso local (si está en red interna):"
echo "  Abrir en navegador: http://$ROUTER_UI_IP:5000"
echo ""

# Mostrar estadísticas rápidas
echo "📈 ESTADÍSTICAS RÁPIDAS:"
echo "======================="

echo ""
echo "Interfaces:"
docker-compose exec -T router-ui sh -c 'ip -s link | grep "packets" | wc -l' 2>/dev/null | tr -d '\r' || echo "  (No disponible)"

echo ""
echo "Reglas Firewall:"
docker-compose exec -T router-ui sh -c 'nft list ruleset 2>/dev/null | grep -c "^[[:space:]]*rule" || echo "0"' 2>/dev/null | tr -d '\r'

echo ""
echo "✅ Router Dashboard listo!"
echo ""
echo "Documentación: infrastructure/router-ui/README.md"
echo "Logs:          docker-compose logs -f router-ui"
