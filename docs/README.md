# QuickStay - Infraestructura Completa

![Status](https://img.shields.io/badge/Status-Production%20Ready-success)
![Docker](https://img.shields.io/badge/Docker-Required-blue)
![License](https://img.shields.io/badge/License-Educational-yellow)

## 📋 Descripción

Infraestructura completa de nivel empresarial para la plataforma **QuickStay** (Alquiler Express de Propiedades). Implementa segmentación VLAN, servicios redundantes, monitoreo, seguridad y acceso VPN, todo containerizado con Docker.

**Autor:** Antonio López Montes  
**Proyecto:** ASIR - Administración de Sistemas Informáticos en Red  
**Año:** 2025/2026

---

## 🏗️ Arquitectura

### Topología de Red

```
Internet (WAN)
     │
     ├─ Router/Firewall (nftables)
     │
     ├─────────────┬──────────────┬──────────────┬──────────────┬──────────────┐
     │             │              │              │              │              │
VLAN 10 DMZ    VLAN 20 App   VLAN 30 Mgmt  VLAN 40 IoT   VLAN 50 VPN
172.16.10/24   172.16.20/24  172.16.30/24  172.16.40/23  172.16.50/26
     │             │              │              │              │
 Load Balancer  App Server    AD-DC (Samba)   MQTT Broker   WireGuard
 Web Server 1   MySQL DB      Zabbix          IoT Devices   Admin Access
 Web Server 2                 Wazuh/SIEM
                              Grafana
                              Dashboard
```

---

## 🚀 Despliegue Rápido

### Requisitos VM

- **SO:** Ubuntu Server 22.04 LTS
- **CPU:** 4+ cores  
- **RAM:** 16+ GB  
- **Disco:** 80+ GB

### Instalación

```bash
# 1. En la VM, copiar el proyecto
cd ~
# Copiar ProyectoFinal/ a la VM

# 2. Ejecutar despliegue (instala Docker si falta)
cd ProyectoFinal
chmod +x deploy-all.sh
./deploy-all.sh
```

---

## 🔐 Acceso

### Público (Internet)
```
http://localhost  → Web QuickStay
```

### VPN (Administradores) ⭐
```bash
# 1. Instalar WireGuard en tu PC
sudo apt install wireguard  # Linux

# Endpoint VPN actual
# 192.168.1.40:51820 (publicado por router-fw)

# 2. Copiar config desde VM
scp usuario@vm-ip:~/ProyectoFinal/wireguard/config/peer_admin1.conf ~/

# 3. Conectar
sudo wg-quick up ~/peer_admin1.conf

# 4. Acceder servicios internos
AD-DC:     172.16.30.10
MySQL:     172.16.20.20:3306
Zabbix:    http://172.16.30.20
Wazuh:     https://172.16.30.30:5601
Grafana:   http://172.16.30.21:3000

# Ver estado VPN en servidor
docker-compose exec router-fw wg show
```

---

## 📊 Credenciales (ver `secrets.env`)

| Servicio | Usuario | Password |
|----------|---------|----------|
| AD-DC | Administrator | QuickStay2026!Admin |
| MySQL | quickstay_app | QuickStay2026!AppDB |
| Zabbix | admin | QuickStay2026!Zabbix |
| Wazuh | admin | QuickStay2026!Wazuh |
| Grafana | admin | QuickStay2026!Grafana |

---

## 🛠️ Comandos Útiles

```bash
# Ver estado
docker-compose ps

# Logs en vivo
docker-compose logs -f

# Reiniciar servicio
docker-compose restart [servicio]

# Acceder a contenedor
docker-compose exec mysql-db bash

# Detener todo
docker-compose down
```

---

## 📁 Estructura

```
ProyectoFinal/
├── deploy-all.sh              # Script maestro
├── docker-compose.yml         # Infraestructura completa
├── secrets.env                # Credenciales
├── infrastructure/            # Configs y Dockerfiles
├── Documentación/             # Código app + docs
├── wireguard/                 # Configs VPN (generado)
└── logs/                      # Logs (generado)
```

---

## 📚 Documentación Detallada

Ver carpetas `Documentación/Fase X/` para guías completas de cada componente.

---

**© 2026 Antonio López Montes - Proyecto ASIR** 🏠✨
