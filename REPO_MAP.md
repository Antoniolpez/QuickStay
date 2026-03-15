# Mapa del Repositorio – QuickStay

Este documento resume la organización del proyecto para localizar rápido cada pieza (infraestructura, app, dashboards, IoT y documentación).

---

## 1. Raíz del proyecto

- `README.md` → Visión general de la infraestructura QuickStay.
- `README_DESPLIEGUE.md` → Guía de despliegue maestro en la VM.
- `QUICKSTART.md` → Instalación rápida (3 pasos).
- `ARQUITECTURA.md` → Descripción de la topología y componentes.
- `docker-compose.yml` → Definición completa de todos los servicios Docker.
- `deploy-all.sh` → Script maestro de despliegue (build + up + checks).
- `setup-vm.sh` → Preparación de la VM (Docker, paquetes base).
- `setup-wireguard-vpn.sh` → Helper para levantar WireGuard y generar `wireguard/peer_admin1.conf`.
- `verify.sh` → Comprobaciones automáticas post-despliegue.
- `secrets.env` → Variables y credenciales (no se sube a Git público).

---

## 2. Infraestructura (servicios Docker)

Carpeta: `infrastructure/`

- `docker/` → Dockerfiles de infra:
  - `router-fw/` → Router/Firewall (nftables).
  - `haproxy/` → Balanceador de carga HTTP.
  - `web-server/` → Apache/PHP (capa web).
  - `samba-ad-dc/` → AD-DC (Samba).
- `config/` → Configs de servicios:
  - `haproxy/` → `haproxy.cfg` (backends, health-checks).
  - `mysql/` → `my.cnf` y ajustes MySQL.
  - `mosquitto/` → `mosquitto.conf` y `passwd`.
  - `nftables/` → reglas de firewall del router.
- `web-content/` → Código PHP/HTML de la web pública QuickStay.

Los volúmenes de logs se montan en `logs/` (creados por `deploy-all.sh`).

---

## 3. Aplicación, Dashboard e IoT

Carpeta: `Documentación/`

- `app_repo/` → Backend Java (Maven) de QuickStay.
  - `Dockerfile`, `pom.xml`, `src/`.
- `dashboard-v2/` → Dashboard maestro (monitorización/presentación).
  - `backend/` → FastAPI (Python).
  - `frontend/` → React + Vite.
- `iot/` → Cliente/simulador IoT en Python.
  - `iot_client.py`, `Dockerfile`.

Estos componentes se construyen y arrancan a través de `docker-compose.yml` y `deploy-all.sh`.

---

## 4. Documentación por fases

Carpeta: `Documentación/`

- `Fase 1/` → Diseño técnico avanzado y plan de implementación.
- `Fase 2/` → Guías de red avanzada y servicios base.
- `Fase 3/` → Capa web redundante, app/BD e IoT/monitoreo/seguridad.
- `Fase 4/` → Backup, recuperación y optimización.
- `Fase 5/` → Pruebas finales y documentación cierre (incluye checklist final).
- `indice.md` → Índice maestro de toda la documentación.
- `walkthrough.md` → Recorrido guiado por las fases y scripts.
- `task.md` → Lista de tareas / scripts (`01_setup_network.sh`, `12_setup_vpn.sh`, etc.).

---

## 5. Scripts de infraestructura “clásica”

Carpeta: `Documentación/infrastructure/scripts/`

- Scripts para despliegues no Docker (guías de red, AD, VPN, etc.).
- Ejemplos:
  - `01_setup_network.sh` → Red base, IP, gateway, DNS.
  - `12_setup_vpn.sh` → Configuración de WireGuard a nivel de SO.
  - `15_optimize_perf.sh` → Tuning de rendimiento (kernel, Apache, MySQL).

Estos scripts acompañan a la documentación de las fases como referencia didáctica.

---

## 6. Directorios generados en tiempo de ejecución

- `logs/` → Logs de router, HAProxy, web, app, MySQL, Zabbix, Wazuh, MQTT, dashboard…
- `wireguard/` → Configuración y claves de WireGuard (servidor + peers).
- `ssh/` → Claves SSH para administración (si se generan).
- `backups/` → Carpeta para simulación de copias de seguridad.

Estos directorios se crean y rellenan durante los scripts de despliegue y pruebas.

---

## 7. Cómo orientarse rápido

1. ¿Infraestructura Docker? → `infrastructure/` + `docker-compose.yml`.
2. ¿Código de aplicación / dashboard / IoT? → `Documentación/app_repo`, `Documentación/dashboard-v2`, `Documentación/iot`.
3. ¿Guías en PDF/Markdown por fases? → `Documentación/Fase X/` + `Documentación/indice.md`.
4. ¿Scripts sueltos de SO/hypervisor? → `Documentación/infrastructure/scripts/`.
5. ¿Checklist final de defensa? → `Documentación/Fase 5/12.- Checklist_Final_QuickStay.md`.
