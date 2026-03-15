# Script para ejecutar provisioning de Zabbix desde un contenedor temporal
# Esto permite acceder a las redes internas de Docker

Write-Host "🔧 Ejecutando provisioning de Zabbix desde contenedor temporal..." -ForegroundColor Cyan

$currentDir = Get-Location

docker run --rm `
  --network=proyectofinal_mgmt_net `
  -v "${currentDir}/monitoring:/monitoring" `
  -v "${currentDir}/Documentación/dashboard-v2/backend/config.yaml:/config.yaml" `
  python:3.9-slim `
  sh -c "pip install -q pyyaml requests && python /monitoring/provision_zabbix.py"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Provisioning completado" -ForegroundColor Green
} else {
    Write-Host "❌ Error en el provisioning" -ForegroundColor Red
    exit 1
}
