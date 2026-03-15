# 🔧 Router Management Dashboard

Dashboard web para monitoreo y control del Firewall/Router QuickStay Infrastructure.

## 📊 Características

### Monitoreo
- ✅ **Estadísticas de Interfaces**: Visualización de todas las interfaces de red (eth0, eth1, vlan10, etc.)
- ✅ **Tabla de Enrutamiento**: Rutas activas, gateways, métricas
- ✅ **Configuración de VLANs**: Segmentación de red (DMZ, App, Management, IoT, VPN)
- ✅ **Reglas NFTables**: Visualización de todas las reglas firewall activas
- ✅ **Estadísticas de Conexiones**: TCP/UDP activas, conexiones establecidas
- ✅ **Logs del Router**: Últimos eventos del firewall

### Control
- 🎚️ **Habilitar/Deshabilitar Interfaces**: Control de interfaces de red
- 🎚️ **Recargar Reglas**: Aplicar cambios en configuración nftables
- 🎚️ **Monitoreo en Tiempo Real**: Actualización automática cada 10 segundos

## 🌐 Acceso

### Desde la Red de Management (VPN o Gestión)

```bash
# Via VPN
http://172.16.30.50:5000

# Nombre DNS (si está disponible)
http://router-ui.quickstay.local:5000
```

## 📡 API REST

El dashboard expone una API REST completa para integración con otras herramientas:

### Health & Status
```bash
# Health check
curl http://172.16.30.50:5000/api/health

# Dashboard general
curl http://172.16.30.50:5000/api/dashboard
```

### Interfaces
```bash
# Listado de todas las interfaces
curl http://172.16.30.50:5000/api/interfaces

# Detalles de una interfaz específica
curl http://172.16.30.50:5000/api/interfaces/eth0
```

### Enrutamiento
```bash
# Tabla de enrutamiento
curl http://172.16.30.50:5000/api/routing

# Configuración de VLANs
curl http://172.16.30.50:5000/api/vlans
```

### Firewall (NFTables)
```bash
# Todas las reglas nftables (JSON)
curl http://172.16.30.50:5000/api/nftables

# Tablas disponibles
curl http://172.16.30.50:5000/api/nftables/tables

# Estadísticas de firewall
curl http://172.16.30.50:5000/api/nftables/stats

# Reglas por tabla
curl http://172.16.30.50:5000/api/nftables/rules?table=quickstay
```

### Estadísticas
```bash
# Estadísticas de red
curl http://172.16.30.50:5000/api/network-stats

# Configuración actual
curl http://172.16.30.50:5000/api/config

# Logs del router
curl http://172.16.30.50:5000/api/logs?lines=50
```

### Control (POST)
```bash
# Recargar reglas nftables
curl -X POST http://172.16.30.50:5000/api/control/reload-rules

# Habilitar interfaz
curl -X POST http://172.16.30.50:5000/api/control/interface/eth0/up

# Deshabilitar interfaz
curl -X POST http://172.16.30.50:5000/api/control/interface/eth0/down
```

## 🛠️ Arquitectura

### Backend
- **Framework**: Flask 2.3.3
- **API**: RESTful con CORS habilitado
- **Fuentes de Datos**:
  - `nft list ruleset` - Reglas firewall
  - `ip command suite` - Interfaces, rutas, VLANs
  - `ss` - Estadísticas de conexión
  - `/proc` y `/sys` - Estadísticas del sistema

### Frontend
- **Tipo**: HTML/CSS/JavaScript puro (sin dependencias)
- **Estilo**: Dark mode, responsive
- **Actualización**: Cada 10 segundos automáticamente

### Container
- **Base**: Python 3.9-slim
- **Herramientas Incluidas**: iproute2, nftables, procps
- **Red**: Conectado a `mgmt_net` (VLAN 30)
- **IP**: 172.16.30.50

## 🚀 Despliegue

El servicio se despliega automáticamente con el docker-compose:

```bash
cd ProyectoFinal
docker-compose up -d router-ui
```

Verificar estado:
```bash
docker-compose logs router-ui
docker-compose ps router-ui
```

## 📊 Métricas Disponibles

| Métrica | Fuente | Actualización |
|---------|--------|---|
| Interfaces UP/DOWN | `ip link` | Real-time |
| Bytes RX/TX | `ip -s statistics` | Real-time |
| Rutas activas | `ip route show` | Real-time |
| VLANs | `ip -d link vlan` | Real-time |
| Reglas Firewall | `nft list ruleset` | Real-time |
| TCP Established | `ss -s` | Real-time |
| Logs | `journalctl`/`syslog` | Últimas 50 líneas |

## 🔐 Seguridad

- ✅ Acceso **solo por VLAN de Management** (172.16.30.0/24)
- ✅ No expuesto públicamente (sin mapeo de puertos en WAN)
- ✅ Accesible vía **VPN/WireGuard** para administradores remotos
- ⚠️ En producción, agregar autenticación (OAuth, JWT, básica)

## 🔧 Configuración Avanzada

### Cambiar puerto
Editar `docker-compose.yml`:
```yaml
router-ui:
  ports:
    - "5000:5000"  # Cambiar a puerto deseado
```

### Cambiar IP
Editar `docker-compose.yml`:
```yaml
router-ui:
  networks:
    mgmt_net:
      ipv4_address: 172.16.30.50  # Cambiar IP aquí
```

### Variables de Entorno
```bash
FLASK_ENV=production      # Modo producción
DEBUG=False               # Deshabilitar debug
```

## 📝 Logs

Ver logs en tiempo real:
```bash
docker-compose logs -f router-ui
```

## 🐛 Troubleshooting

### Dashboard no carga
```bash
# Verificar conectividad
curl http://172.16.30.50:5000/api/health

# Revisar logs del contenedor
docker-compose logs router-ui --tail 50

# Verificar que esté en la red mgmt_net
docker exec quickstay-router-ui ip addr show
```

### API lenta
- Los comandos nftables pueden tardar en sistemas con muchas reglas
- Considerar cachear resultados o usar interval más largo (>10s)

### Permisos insuficientes
El container necesita:
- `CAP_NET_ADMIN` para algunos comandos
- Acceso a `/proc` y `/sys` (read-only)
- Acceso a `unix socket` de docker (opcional)

## 📚 Referencias

- [NFTables Wiki](https://wiki.nftables.org/)
- [Linux ip command](https://linux.die.net/man/8/ip)
- [Flask Documentation](https://flask.palletsprojects.com/)
- [Docker CLI Reference](https://docs.docker.com/engine/reference/run/)

## 📞 Soporte

Para problemas con el router-ui:
1. Verificar logs: `docker-compose logs router-ui`
2. Validar conectividad a management VLAN
3. Revisar disponibilidad de herramientas (nft, ip)
4. Consultar sección de Troubleshooting

---

**Parte de QuickStay Infrastructure v1.0**
Última actualización: Febrero 2026
