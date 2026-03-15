# Diagnóstico de Problemas QuickStay

## Problema 1: Web no accesible desde 192.168.1.40

### Posibles causas:
1. El contenedor `router-fw` no se ha reconstruido con la nueva regla DNAT
2. El puerto 80 del host no está realmente mapeado al router-fw
3. Falta configuración de gateway en los web servers

### Solución:
```bash
# 1. Reconstruir solo el router-fw con la nueva regla DNAT
docker-compose build router-fw

# 2. Reiniciar el router-fw
docker-compose up -d router-fw

# 3. Verificar que la regla DNAT está activa
docker exec quickstay-router-fw nft list ruleset | grep -A5 "prerouting"

# 4. Verificar conectividad desde el router al load balancer
docker exec quickstay-router-fw ping -c 2 172.16.10.10

# 5. Verificar que HAProxy está funcionando
docker exec quickstay-lb ps aux | grep haproxy

# 6. Probar acceso directo al load balancer (desde la VM)
curl -I http://172.16.10.10

# 7. Probar acceso a través del router (desde la VM)
curl -I http://localhost
```

## Problema 2: Zabbix API no responde

### Posibles causas:
1. Zabbix-web no está completamente inicializado
2. La ruta de la API es incorrecta
3. Zabbix-server no está conectado al frontend

### Diagnóstico:
```bash
# 1. Verificar estado de contenedores Zabbix
docker-compose ps | grep zabbix

# 2. Ver logs del frontend
docker logs quickstay-zabbix-web --tail 50

# 3. Ver logs del servidor
docker logs quickstay-zabbix-server --tail 50

# 4. Verificar conectividad desde dentro de la red
docker exec quickstay-ad-dc-primary curl -I http://172.16.30.21

# 5. Probar la API manualmente
docker exec quickstay-ad-dc-primary curl -X POST -H "Content-Type: application/json-rpc" \
  -d '{"jsonrpc":"2.0","method":"apiinfo.version","params":{},"id":1}' \
  http://172.16.30.21/api_jsonrpc.php

# 6. Verificar que la base de datos de Zabbix está lista
docker exec quickstay-zabbix-db mysql -u zabbix -p${ZABBIX_DB_PASSWORD} -e "SHOW TABLES;" zabbix | wc -l
```

## Comandos rápidos de verificación

```bash
# Ver todos los contenedores
docker-compose ps

# Ver IPs de todos los contenedores
docker network inspect proyectofinal_dmz_net | grep -A3 "Name.*quickstay"
docker network inspect proyectofinal_app_net | grep -A3 "Name.*quickstay"
docker network inspect proyectofinal_mgmt_net | grep -A3 "Name.*quickstay"

# Logs en tiempo real
docker-compose logs -f router-fw
docker-compose logs -f load-balancer
docker-compose logs -f zabbix-web
```

## Pasos para resolución completa

1. **Reconstruir router-fw** (tiene la nueva regla DNAT):
   ```bash
   docker-compose build router-fw
   docker-compose up -d router-fw
   ```

2. **Esperar a que Zabbix esté 100% listo** (puede tardar 5-10 minutos DESPUÉS de que el contenedor esté "Up"):
   ```bash
   # Esperar y monitorear
   watch -n 5 'docker logs quickstay-zabbix-web --tail 20'
   ```

3. **Ejecutar provisioning de Zabbix manualmente cuando esté listo**:
   ```bash
   bash provision-zabbix-docker.sh
   ```
4. **Probar acceso web**:
   - Desde VM: `http://localhost`
   - Desde red local: `http://192.168.1.40`

## Dashboard de Router

Para monitorear y controlar el firewall/router en tiempo real, accede al Router Management Dashboard:

```
URL: http://172.16.30.50:5000
Acceso: Solo desde VLAN Management (172.16.30.0/24) o VPN
```

**Características:**
- 📊 Dashboard con estadísticas en tiempo real
- 📡 Estado de todas las interfaces de red
- 🌐 Configuración de VLANs
- 🛣️ Tabla de enrutamiento
- 🔥 Reglas NFTables (firewall)
- 📤 Estadísticas de conexiones TCP/UDP
- 🎚️ Controles para habilitar/deshabilitar interfaces

Para más detalles, ver infrastracture/router-ui/README.md
