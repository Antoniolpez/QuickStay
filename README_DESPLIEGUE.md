# QuickStay - Plataforma de Alquiler Express
## Despliegue Maestro de Infraestructura

**Proyecto:** Ciclo Formativo Superior - Administración de Sistemas Informáticos en Red (ASIR)  
**Autor:** Antonio López Montes  
**Fecha:** Enero 2026

---

## 📋 Descripción General

Este repositorio contiene el **despliegue maestro completo** de la infraestructura QuickStay, una plataforma enterprise de alquiler de propiedades con alta disponibilidad, seguridad avanzada y redundancia.

La infraestructura está desplegada en **Docker** simulando una red segmentada en VLANs, con:
- ✅ Separación de zonas (DMZ, Servicios, Gestión, IoT, VPN)
- ✅ Firewall con nftables y control de acceso
- ✅ Load Balancer (HAProxy) con redundancia
- ✅ Base de datos MySQL centralizada
- ✅ Active Directory para autenticación
- ✅ VPN WireGuard para administración remota
- ✅ Monitoreo con Zabbix y Wazuh (SIEM)
- ✅ IoT con MQTT Mosquitto
- ✅ Dashboard maestro para presentación

---

## 🚀 Requisitos Previos

### Hardware Recomendado (VM)
```
CPU:    4 cores (6-8 óptimo)
RAM:    16 GB (20-24 GB ideal)
Disco:  80 GB SSD mínimo
Red:    Bridge o NAT con port forwarding
```

### Software
```
OS:     Ubuntu Server 22.04 LTS o Debian 12
Docker: 20.10+ (instalación automática incluida)
Git:    Para clonar el repositorio
```

---

## ⚡ Inicio Rápido

### 1. Clonar o descargar el proyecto
```bash
git clone <repositorio> quickstay
cd quickstay
```

### 2. Ejecutar el despliegue (se instala Docker automáticamente si es necesario)
```bash
# Hacer script ejecutable
chmod +x deploy-all.sh

# Ejecutar despliegue
./deploy-all.sh
```

El script se encargará de:
- ✅ Instalar Docker (si no está disponible)
- ✅ Validar requisitos
- ✅ Crear directorios necesarios
- ✅ Construir todas las imágenes Docker
- ✅ Desplegar todos los contenedores
- ✅ Esperar a que los servicios estén listos
- ✅ Configurar registros DNS
- ✅ Generar configuraciones VPN

---

## 🏗️ Arquitectura de Red

```
┌─────────────────────────────────────────────────────────────────┐
│                        INTERNET (WAN)                            │
│                       203.0.113.0/24                             │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │   ROUTER/FW  │
                    │  nftables    │
                    │172.16.0.1/16 │
                    └──────┬──────┘
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   ┌────▼────┐     ┌────────▼────────┐  ┌─────▼─────┐
   │ DMZ (10)│     │ SERVICIOS (20)  │  │ MGMT (30) │
   │172.16.. │     │    172.16.20.x  │  │172.16.30.x│
   │         │     │                 │  │           │
   │ •LB     │     │ • App Server    │  │ • AD-DC   │
   │ •Web1/2 │     │ • MySQL         │  │ • Zabbix  │
   │         │     │                 │  │ • Wazuh   │
   └─────────┘     └─────────────────┘  │ • Grafana │
                                        └───────────┘
        ┌───────────────────────┬──────────────────┐
        │                       │                  │
   ┌────▼─────┐         ┌───────▼─────┐    ┌──────▼─────┐
   │  IoT (40)│         │   VPN (50)  │    │  DASHBOARD │
   │172.16.40 │         │172.16.50.x  │    │  172.16.30.│
   │           │         │             │    │     40     │
   │ •MQTT     │         │ WireGuard   │    │ Backend    │
   │ •Simulador│         │ Server      │    │ Frontend   │
   └───────────┘         └─────────────┘    └────────────┘
```

---

## 🔐 Segmentación de Red (VLANs)

| VLAN | Nombre | Subred | Uso | Servidores |
|------|--------|--------|-----|-----------|
| **10** | DMZ | 172.16.10.0/24 | Zona Pública | Load Balancer, Web Servers |
| **20** | Servicios | 172.16.20.0/24 | App & BD | App Server, MySQL |
| **30** | Gestión | 172.16.30.0/24 | Admin | AD-DC, Zabbix, Wazuh, Grafana, Dashboard |
| **40** | IoT | 172.16.40.0/23 | Dispositivos IoT | MQTT Broker, Simulador |
| **50** | VPN | 172.16.50.0/26 | Admin Remoto | WireGuard VPN Server |

---

## 📦 Servicios Desplegados

### 🌐 Web & Load Balancing
| Servicio | IP:Puerto | Función |
|----------|-----------|---------|
| Load Balancer (HAProxy) | 172.16.10.10:80 | Balancea entre Web Servers |
| Web Server 1 (Apache) | 172.16.10.20:80 | Servidor Web (DMZ) |
| Web Server 2 (Apache) | 172.16.10.21:80 | Servidor Web redundancia |

### 🗄️ Aplicación & Base de Datos
| Servicio | IP:Puerto | Credenciales |
|----------|-----------|-------------|
| App Server (Java) | 172.16.20.10:1234 | - |
| MySQL Database | 172.16.20.20:3306 | `quickstay_app`:`contraseña` |

### 🔐 Autenticación & Directorio
| Servicio | IP:Puerto | Detalles |
|----------|-----------|---------|
| AD-DC (Samba) | 172.16.30.10 | Domain: `QUICKSTAY.LOCAL` |
| DNS | 172.16.30.10:53 | Integrado en AD-DC |

### 📊 Monitoreo & Seguridad
| Servicio | IP:Puerto | Acceso |
|----------|-----------|--------|
| Zabbix Server | 172.16.30.20 | Backend de monitorización |
| Zabbix Web | 172.16.30.21:8080 | http://172.16.30.21:8080 |
| Grafana | 172.16.30.22 | http://172.16.30.22:3000 |
| Wazuh Dashboard | 172.16.30.31 | https://172.16.30.31:5601 |
| Dashboard Maestro | 172.16.30.40:8000 | API REST + WebSockets |

### 🚀 IoT & VPN
| Servicio | IP:Puerto | Función |
|----------|-----------|---------|
| MQTT Broker (Mosquitto) | 172.16.40.10:1883 | IoT Hub |
| IoT Simulator | 172.16.40.x | Simula dispositivos |
| WireGuard VPN | 172.16.50.1:51820 | Acceso admin remoto |

---

## 🔓 Acceso a Servicios

### 1️⃣ Acceso Público (Internet)
```
QuickStay Web (IP pública simulada):  http://203.0.113.2
QuickStay Web (desde la propia VM):   http://localhost
```

### 2️⃣ Acceso VPN (Administradores)
```
1. Descargar configuración:
   ./wireguard/config/peer_admin1.conf

2. Instalar cliente WireGuard:
   - Linux: sudo apt install wireguard-tools wireguard
   - Windows: https://www.wireguard.com/install/
   - macOS: https://www.wireguard.com/install/

3. Importar configuración (ejemplos):
  - Linux:   sudo wg-quick up ./peer_admin1.conf
  - Windows: importar `peer_admin1.conf` en el cliente gráfico WireGuard

4. Acceso a servicios internos (desde tu cliente VPN):
  - Zabbix Web:   http://172.16.30.21:8080
   - Wazuh Dash:   https://172.16.30.31:5601
   - Grafana:      http://172.16.30.22:3000
```

> Nota sobre VPN y persistencia:
> - El servidor WireGuard se configura en `/wireguard/config/wg_confs/wg0.conf` (montado en el volumen `./wireguard/config` del host). Ahí quedan de forma **persistente** la IP `172.16.50.1`, los peers y las reglas `PostUp/PostDown`.
> - Al arrancar el contenedor `wireguard-vpn`, la imagen aplica automáticamente esas `PostUp` (reenvío y NAT con iptables) y, además, el script `deploy-complete.sh` ejecuta una fase extra (FASE 11.2) que fuerza `ip_forward` y las reglas de FORWARD/MASQUERADE necesarias.
> - Aunque las tablas iptables internas del contenedor se vacían al reiniciar, se **reconstruyen en cada despliegue**, por lo que la conectividad desde la VPN hacia 172.16.10.0/24, 172.16.20.0/24 y 172.16.30.0/24 se mantiene sin pasos manuales.

### 3️⃣ Credenciales Principales
```
Archivo: secrets.env (NO compartir)

Active Directory (AD-DC):
  - Dominio: QUICKSTAY.LOCAL
  - Usuario: Administrator
  - Contraseña: [ver en secrets.env]

MySQL:
  - Host: 172.16.20.20
  - User: quickstay_app
  - Pass: [ver en secrets.env]
  - DB: humhouse

MQTT:
  - Host: 172.16.40.10:1883
  - User: [ver en secrets.env]
  - Pass: [ver en secrets.env]

Zabbix Admin:
  - URL: http://172.16.30.21:8080
  - User: Admin
  - Pass: zabbix (por defecto de la imagen)

Grafana Admin:
  - URL: http://172.16.30.21:3000
  - User: admin
  - Pass: [ver en secrets.env]

Wazuh Admin:
  - URL: https://172.16.30.30:5601
  - User: admin
  - Pass: [ver en secrets.env]
```

---

## 📝 Estructura de Directorios

```
quickstay/
├── deploy-all.sh              # ⭐ Script maestro de despliegue
├── docker-compose.yml         # Configuración de servicios
├── secrets.env               # ⚠️ CREDENCIALES (NO COMPARTIR)
├── .gitignore               # Exclusiones de Git
├── README.md                # Este archivo
│
├── infrastructure/          # Configuraciones de infraestructura
│   ├── docker/             # Dockerfiles personalizados
│   │   ├── router-fw/
│   │   ├── haproxy/
│   │   ├── web-server/
│   │   └── samba-ad-dc/
│   │
│   ├── config/             # Configuraciones de servicios
│   │   ├── haproxy/
│   │   ├── mysql/
│   │   ├── mosquitto/
│   │   └── iptables/
│   │
│   └── web-content/        # Contenido web (index.php, etc)
│
├── Documentación/          # Aplicación Java y componentes
│   ├── app_repo/          # Servidor Java QuickStay
│   │   └── src/
│   ├── dashboard-v2/      # Dashboard maestro (React + FastAPI)
│   ├── iot/              # Simulador IoT (Python)
│   └── infrastructure/   # Scripts de infraestructura
│
├── logs/                 # Directorios para logs de servicios
├── wireguard/           # Configuraciones WireGuard VPN
├── ssh/                 # Claves SSH
└── backups/            # Copias de seguridad

```

---

## ⚙️ Comandos Útiles

### Estado y Logs
```bash
# Ver estado de todos los servicios
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f mysql-db
docker-compose logs -f app-server
docker-compose logs -f router-fw

# Ver estadísticas de recursos
docker stats
```

### Gestión de Contenedores
```bash
# Reiniciar un servicio
docker-compose restart app-server

# Detener todo
docker-compose stop

# Detener y eliminar (cuidado: elimina volúmenes)
docker-compose down -v

# Reconstruir imágenes
docker-compose build --no-cache
```

### Acceso a Contenedores
```bash
# Bash en un contenedor
docker-compose exec app-server bash
docker-compose exec mysql-db bash
docker-compose exec ad-dc-primary bash

# Comandos directos
docker-compose exec mysql-db mysql -u root -p humhouse
docker-compose exec ad-dc-primary samba-tool user list
```

### Verificación de Red
```bash
# Listar redes Docker
docker network ls

# Inspeccionar red específica
docker network inspect quickstay_dmz_net
docker network inspect quickstay_app_net

# Ping entre contenedores
docker-compose exec web-server-1 ping app-server
docker-compose exec app-server ping mysql-db
```

---

## 🔧 Configuración y Personalización

### Cambiar Credenciales
1. Editar `secrets.env`
2. Ejecutar: `docker-compose down -v`
3. Ejecutar: `docker-compose up -d --build`

### Exponer Servicios Específicos
Si necesitas acceder a servicios sin VPN, edita `docker-compose.yml`:
```yaml
services:
  zabbix-web:
    ports:
      - "9090:80"  # Ahora accesible en localhost:9090
```

### Aumentar Recursos
En `docker-compose.yml` o crea `docker-compose.override.yml`:
```yaml
version: '3.8'
services:
  mysql-db:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
```

---

## 🐛 Solución de Problemas

### "Docker: command not found"
```bash
# Instalar Docker manualmente
sudo apt update
sudo apt install -y docker.io docker-compose
sudo usermod -aG docker $USER
newgrp docker
```

### Contenedores no inician
```bash
# Ver logs detallados
docker-compose logs -f mysql-db

# Reintentar
docker-compose restart mysql-db

# Verificar permisos/recursos
docker ps -a
docker system df
```

### Conectividad entre VLANs
```bash
# Verificar reglas de firewall
docker-compose exec router-fw nft list ruleset

# Ping de prueba
docker-compose exec web-server-1 ping app-server
docker-compose exec app-server ping mysql-db
```

### WireGuard VPN no conecta
```bash
# Verificar que WireGuard esté activo
docker-compose ps wireguard-vpn

# Ver logs
docker-compose logs wireguard-vpn

# Regenerar configuraciones
docker-compose exec wireguard-vpn wg show
```

---

## 📚 Documentación Adicional

### Fases del Proyecto
- `Documentación/Fase 1/` - Diseño técnico avanzado
- `Documentación/Fase 2/` - Implementación de red y servicios base
- `Documentación/Fase 3/` - Capa web redundante, app, BD
- `Documentación/Fase 4/` - Backup, recuperación y optimización
- `Documentación/Fase 5/` - Pruebas finales y documentación

### Guías Específicas
```bash
# Detalles del servidor Java
cat Documentación/app_repo/README.md

# Dashboard maestro
cat Documentación/dashboard-v2/README.md

# Configuración de IoT
cat Documentación/iot/

# Scripts de infraestructura
cat Documentación/infrastructure/scripts/
```

---

## 🔐 Seguridad

### ⚠️ IMPORTANTE
1. **secrets.env** contiene credenciales sensibles - NO subir a Git
2. Cambiar todas las contraseñas antes de producción
3. Habilitar HTTPS en servicios web
4. Configurar certificados SSL/TLS válidos
5. Mantener Docker y OS actualizados
6. Revisar logs regularmente en `./logs/`

### Mejoras de Seguridad Recomendadas
```bash
# Deshabilitar root SSH
docker-compose exec ad-dc-primary sed -i 's/#PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config

# Rotar credenciales cada 90 días
# Implementar 2FA en servicios críticos
# Monitorear con Wazuh/Grafana
# Realizar backup diarios
```

---

## 📊 Monitoreo

### Dashboards Disponibles
1. **Zabbix** (172.16.30.20): Monitoreo de hosts y servicios
2. **Grafana** (172.16.30.21): Visualización de métricas
3. **Wazuh** (172.16.30.30): Seguridad e integridad de archivos
4. **HAProxy Stats** (172.16.10.10:8404, solo vía VPN): Panel del balanceador de carga (administración)

### Alertas Configuradas
- CPU > 80%
- Memoria > 85%
- Disco > 90%
- Servicio caído
- Intento de login fallido

---

## 🚀 Escalabilidad Futura

```bash
# Añadir más web servers
# Replicación MySQL
# Balanceo de carga de DNS
# Múltiples DC para AD
# Almacenamiento distribuido
# Caché Redis
# Búsqueda Elasticsearch
```

---

## 📞 Soporte y Contacto

**Autor:** Antonio López Montes  
**Correo:** antonio.lopez@proyecto.local  
**Proyecto:** Ciclo ASIR - 2º Año

---

## 📄 Licencia

Proyecto educativo - Ciclo Formativo ASIR 2026

---

**Última actualización:** Enero 26, 2026  
**Versión:** 1.0.0 - Release Candidato

