# Lista de Tareas - QuickStay Infrastructure

## Pendientes actuales (alto nivel)

- [ ] Cerrar hardening base de la plataforma (credenciales, accesos y cifrado).
- [ ] Consolidar observabilidad y alertas operativas en Zabbix/Grafana/Wazuh.
- [ ] Automatizar backups y validar una restauracion end-to-end.
- [ ] Integrar en despliegue el arranque de maquinas con agente Zabbix preconfigurado.
- [ ] Estandarizar checklists post-despliegue y mantenimiento.

## Pendientes detallados (previos a Zabbix)

- [ ] Gestionar secretos y credenciales (rotacion, almacenamiento y emergencia).
- [ ] Sustituir certificados temporales por TLS real y renovacion automatica.
- [ ] Endurecer acceso administrativo (restricciones de origen, control de intentos, auditoria).
- [ ] Consolidar observabilidad con alertas accionables (caidas, disco, VPN, backup).
- [ ] Automatizar backup de componentes criticos y probar restauracion completa.
- [ ] Revisar hardening de servicios internos y exposicion minima de puertos.
- [ ] Definir validaciones CI/CD basicas para despliegue y post-deploy.

<!-- id: 0 -->
- [x] **Fase 1: Preparación**
  - [x] Reverse Engineering del Schema SQL (`init.sql`) <!-- id: 1 -->
  - [x] Refactorizar Java para `config.properties` <!-- id: 2 -->
- [x] **Fase 2: Infraestructura Base**
  - [x] `01_setup_network.sh`: Configura IP, Gateway y DNS <!-- id: 3 -->
  - [x] `07_deploy_ad.sh`: Despliega Samba AD-DC y DNS <!-- id: 11 -->
  - [x] `09_setup_firewall.sh`: Configura iptables, NAT y ACLs <!-- id: 13 -->
  - [x] `10_setup_dhcp.sh`: DHCP para IoT/VPN <!-- id: 14 -->
  - [x] `12_setup_vpn.sh`: WireGuard VPN <!-- id: 16 -->
- [x] **Fase 3: Aplicación y Servicios**
  - [x] `02_deploy_db.sh`: Instala MySQL y Schema <!-- id: 4 -->
  - [x] `08_setup_web.sh`: Apache DMZ <!-- id: 12 -->
  - [x] `11_setup_lb.sh`: Nginx Load Balancer <!-- id: 15 -->
  - [x] `03_deploy_iot.sh`: Mosquitto Broker <!-- id: 5 -->
  - [x] `14_deploy_security.sh`: Wazuh Manager/Agent <!-- id: 19 -->
  - [x] `04_deploy_monitoring.sh`: Zabbix Agent <!-- id: 7 -->
- [x] **Fase 4: Optimización y Mantenimiento**
  - [x] `05_simulate_backup.sh`: Estructura Veeam <!-- id: 9 -->
  - [x] `15_optimize_perf.sh`: Tuning Kernel/Apache/MySQL <!-- id: 20 -->
  - [x] `16_migrate_blobs.sh`: Optimización Fotos (DB -> Disco) <!-- id: 21 -->
- [x] **Fase 5: Verificación**
  - [x] `06_run_tests.sh`: Tests de conectividad <!-- id: 10 -->
- [x] **Fase 6: Hypervisor Dashboard**
  - [x] Diseño Arquitectura (`dashboard_architecture.md`) <!-- id: 17 -->
  - [x] `13_deploy_dashboard.sh`: Docker Stack <!-- id: 18 -->
- [ ] **Fase 7: Refactorización App (Modo Dios)**
  - [x] `17_update_schema_v2.sh`: Nueva tabla `mensajes` y columnas URL <!-- id: 22 -->
  - [x] Refactor Chat: Migrar de Sockets/Txt a MySQL (`HiloServidor.java`) <!-- id: 23 -->
  - [x] Refactor Fotos: Usar `foto_url` en lugar de Blobs (`MySQL.java`) <!-- id: 24 -->
  - [x] Refactor Logging: `System.out` -> `Log4j` <!-- id: 25 -->
- [ ] **Simulación**
  - [x] `iot_client.py`: Simulador Python <!-- id: 8 -->
