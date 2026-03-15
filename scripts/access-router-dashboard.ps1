powershell
# Router Dashboard - Verificación y Acceso
# Script para PowerShell (Windows)

Write-Host "🔧 Router Dashboard - Verificacion y Acceso" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que router-ui está en ejecución
$status = & docker-compose ps router-ui

if ($status -match "Up") {
    Write-Host "✅ Router-UI está ejecutándose" -ForegroundColor Green
} else {
    Write-Host "❌ Router-UI no está ejecutándose" -ForegroundColor Red
    Write-Host "Iniciando router-ui..." -ForegroundColor Yellow
    & docker-compose up -d router-ui
    Start-Sleep -Seconds 5
}

Write-Host ""
Write-Host "📡 Router-UI IP: 172.16.30.50 (en VLAN Management)" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 ACCESO AL DASHBOARD:" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URL:     http://172.16.30.50:5000" -ForegroundColor Green
Write-Host "Acceso:  Solo desde VPN (VPN/VLAN interna)" -ForegroundColor Yellow
Write-Host ""

Write-Host "OPCIONES DE ACCESO:" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. DESDE VPN WIREGUARD:" -ForegroundColor White
Write-Host "   Conectar a VPN y luego abrir:" -ForegroundColor Gray
Write-Host "   http://172.16.30.50:5000" -ForegroundColor Green
Write-Host ""

Write-Host "2. DESDE RED INTERNA (si estás conectado a red local):" -ForegroundColor White
Write-Host "   Abrir directamente:" -ForegroundColor Gray
Write-Host "   http://172.16.30.50:5000" -ForegroundColor Green
Write-Host ""

Write-Host "📊 CARACTERÍSTICAS DEL DASHBOARD:" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  📊 Dashboard con estadísticas en tiempo real" -ForegroundColor Gray
Write-Host "  📡 Estado de interfaces de red (UP/DOWN)" -ForegroundColor Gray
Write-Host "  🌐 Configuración de VLANs (DMZ, App, Management, IoT, VPN)" -ForegroundColor Gray
Write-Host "  🛣️ Tabla de enrutamiento completa" -ForegroundColor Gray
Write-Host "  🔥 Reglas NFTables del firewall" -ForegroundColor Gray
Write-Host "  📤 Estadísticas de conexiones TCP/UDP" -ForegroundColor Gray
Write-Host "  🎚️ Controles para habilitar/deshabilitar interfaces" -ForegroundColor Gray
Write-Host ""

Write-Host "🔌 API REST DISPONIBLE:" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ejemplos de endpoints:" -ForegroundColor White
Write-Host ""
Write-Host "  GET /api/health                - Health check" -ForegroundColor Gray
Write-Host "  GET /api/dashboard             - Dashboard principal" -ForegroundColor Gray
Write-Host "  GET /api/interfaces            - Todas las interfaces" -ForegroundColor Gray
Write-Host "  GET /api/vlans                 - Configuración de VLANs" -ForegroundColor Gray
Write-Host "  GET /api/routing               - Tabla de enrutamiento" -ForegroundColor Gray
Write-Host "  GET /api/nftables              - Reglas firewall (JSON)" -ForegroundColor Gray
Write-Host "  GET /api/nftables/stats        - Estadísticas de firewall" -ForegroundColor Gray
Write-Host "  GET /api/network-stats         - Conexiones activas" -ForegroundColor Gray
Write-Host "  POST /api/control/reload-rules - Recargar reglas" -ForegroundColor Gray
Write-Host ""

Write-Host "📁 ARCHIVOS GENERADOS:" -ForegroundColor Cyan
Write-Host "======================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  infrastructure/router-ui/" -ForegroundColor White
Write-Host "    ├── README.md              (Documentación completa)" -ForegroundColor Gray
Write-Host "    ├── Dockerfile            (Container definition)" -ForegroundColor Gray
Write-Host "    ├── backend/" -ForegroundColor Gray
Write-Host "    │   ├── app.py            (Flask API)" -ForegroundColor Gray
Write-Host "    │   └── requirements.txt   (Python dependencies)" -ForegroundColor Gray
Write-Host "    └── frontend/" -ForegroundColor Gray
Write-Host "        └── index.html         (Dashboard web)" -ForegroundColor Gray
Write-Host ""

Write-Host "🚀 PRÓXIMOS PASOS:" -ForegroundColor Cyan
Write-Host "=================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Reconstruir y desplegar el router-fw con la nueva regla DNAT:" -ForegroundColor White
Write-Host "   cd ProyectoFinal" -ForegroundColor Gray
Write-Host "   docker-compose build router-fw" -ForegroundColor Gray
Write-Host "   docker-compose up -d router-fw" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Conectarse a la VPN WireGuard (para acceso remoto)" -ForegroundColor White
Write-Host ""
Write-Host "3. Abrir el Router Dashboard:" -ForegroundColor White
Write-Host "   http://172.16.30.50:5000" -ForegroundColor Green
Write-Host ""
Write-Host "4. Ver documentación completa:" -ForegroundColor White
Write-Host "   infrastructure/router-ui/README.md" -ForegroundColor Gray
Write-Host ""

Write-Host "📝 DOCUMENTACION:" -ForegroundColor Cyan
Write-Host "=================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Ver infrastracture/router-ui/README.md" -ForegroundColor Yellow
Write-Host "  Ver DIAGNOSTICO.md para troubleshooting" -ForegroundColor Yellow
Write-Host ""

Write-Host "✅ Router Dashboard completamente configurado!" -ForegroundColor Green
