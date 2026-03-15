# QuickStay - Guía de Automatización e Infraestructura

Este documento resume la implementación técnica del proyecto QuickStay.

## Arquitectura Implementada

Se ha seguido estrictamente el diseño de las 5 Fases, utilizando IPs de la red `172.16.0.0/16`.

| VLAN | ID | Rango | Uso |
| :--- | :--- | :--- | :--- |
| **DMZ** | 10 | `172.16.10.0/24` | Web Servers, Load Balancer |
| **App** | 20 | `172.16.20.0/24` | Java App, MySQL Database |
| **Mgmt** | 30 | `172.16.30.0/24` | AD-DC, Zabbix, Wazuh, Backup |
| **IoT** | 40 | `172.16.40.0/23` | MQTT Brokers, Sensores |
| **VPN** | 50 | `172.16.50.0/26` | Acceso Remoto Admin |

## Scripts de Automatización

Todos los scripts se encuentran en la carpeta `infrastructure/scripts/`.

### Matriz de Cumplimiento (Ordenada por Guía)
| Fase | Guía | Elemento | Implementación (Scripts) | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **2** | **3 (Red)** | Interfaces (`enp0s3/8`) | `01_setup_network.sh` | ✅ Aligned |
| **2** | **3 (Red)** | VLANs (10, 20, 30...) | `01_setup_network.sh` | ✅ Aligned |
| **2** | **3 (Red)** | Firewall / NAT / ACLs | `09_setup_firewall.sh` | ✅ Aligned |
| **2** | **3 (Red)** | VPN Acceso Remoto | `12_setup_vpn.sh` | ✅ Aligned |
| **2** | **4 (Base)** | AD-DC / DNS / Kerberos | `07_deploy_ad.sh` | ✅ Aligned |
| **2** | **4 (Base)** | DHCP (IoT/VPN) | `10_setup_dhcp.sh` | ✅ Aligned |
| **3** | **5 (Web)** | Balanceador (F5/VIP) | `11_setup_lb.sh` | ✅ Aligned |
| **3** | **5 (Web)** | Web Server DMZ | `08_setup_web.sh` | ✅ Aligned |
| **3** | **6 (App/DB)** | Usuario DB (`quickstay_app`) | `02_deploy_db.sh` | ✅ Aligned |
| **3** | **6 (App/DB)** | Schema (`humhouse` vs `quickstay_db`) | `02_deploy_db.sh` + `init.sql` | ✅ Aligned |
| **3** | **6/7 (Mon)** | Agente Zabbix | `04_deploy_monitoring.sh` | ✅ Aligned |
| **3** | **7 (Sec)** | Wazuh Manager/Agent | `14_deploy_security.sh` | ✅ Aligned |
| **4** | **8 (Backup)** | Repositorios y Scripts Pre/Post | `05_simulate_backup.sh` | ✅ Aligned |
| **4** | **9 (Perf)** | Tuning Apache/MySQL/Kernel | `15_optimize_perf.sh` | ✅ Aligned |
| **5** | **10 (Tests)** | Pruebas Integrales | `06_run_tests.sh` | ✅ Aligned |
| **6** | **Hypervisor** | Dashboard Maestro (Docker) | `13_deploy_dashboard.sh` | ✅ Aligned |

## 6. Análisis vs Diagrama de Arquitectura
Comparativa final contra el diseño visual:

*   **Tengo:** Router/Firewall (`09`), Balanceador (`11`), Web (`08`), App/DB (`MySQL/Java`), AD-DC (`07`), IoT (`03`), VPN (`12`), Seguridad (`14`).
*   **Simulado/Implícito:**
    *   **Switch L3:** Funcionalidad cubierta por el Firewall (`09`) haciendo routing Inter-VLAN.
    *   **Alta Disponibilidad:** Scripts reutilizables para Nodos Secundarios.
