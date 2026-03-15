import re
import os

filepath = '/home/jefe/COMPARTIDA_WINDOWS/quickstay/documentacion.md'

# Read the file
with open(filepath, 'r', encoding='utf-8') as f:
    text = f.read()

# Replace Phase 4.2
s4_2 = r"""#### 4.2. Esquema de Fases del Proyecto.................................................................................................

El proyecto se divide en fases secuenciales y dependientes para su implementación técnica, desglosando las tareas a nivel granular (RA 3.a):

| Fase / Sub-tarea | Descripción y Lógica de Dependencia | Perfil Técnico |
| :--- | :--- | :--- |
| **Fase 1: Diseño y Planificación** | Definición de arquitectura, viabilidad y documentación. | Administrador de Sistemas |
| **Fase 2: Implementación Servidores** | | |
| 2.1. Instalación S.O. Linux | Instalar Ubuntu Server en 3 VMs. *(Bloquea a 2.2)* | Administrador de Sistemas |
| 2.2. Configuración Red y VLANs | Configuración de interfaces y segmentación de red. *(Depende de 2.1)* | Administrador de Sistemas |
| 2.3. Despliegue AD-DC | Instalar/configurar controlador de dominio Samba4. *(Depende de 2.2)* | Administrador de Sistemas |
| 2.4. Reglas Firewall (nftables) | Cierre perimetral y políticas DMZ. *(Depende de 2.2)* | Administrador de Sistemas |
| **Fase 3: Desarrollo App y BBDD** | | |
| 3.1. Instalación MySQL | Configuración del motor de BBDD. *(Depende de 2.2)* | SysAdmin / Desarrollador |
| 3.2. Estructura Lógica BBDD | Creación de esquemas (tablas, relaciones). *(Depende de 3.1)* | Desarrollador / DBA |
| 3.3. Desarrollo Servidor Java | Lógica backend e integración de sockets. *(Depende de 3.2)* | Desarrollador Java |
| **Fase 4: Integración OT y Seguridad** | | |
| 4.1. Union IoT a Dominio | Integrar centralitas al AD-DC. *(Depende de 2.3)* | SysAdmin / Técnico IoT |
| 4.2. Cifrado y VPN | Setup de SSL/TLS y acceso externo (OpenVPN). *(Depende de 2.4)* | Administrador de Sistemas |
| **Fase 5: Automatización y Monitoreo** | | |
| 5.1. Scripts Bash de Backup | Programación de tareas (cron). *(Depende de 2.1)* | Administrador de Sistemas |
| 5.2. Despliegue Zabbix/Wazuh | Instalación de server y agentes HIDS. *(Depende de 2.2)* | Administrador de Sistemas |
| **Fase 6: Pruebas y Despliegue** | Ejecución del Plan de Pruebas y Checklist. *(Depende de todas)* | Equipo de QA / SysAdmin |

"""
text = re.sub(r'#### 4\.2\. Esquema de Fases del Proyecto\.\.\..*?(?=### 5\. Recursos)', s4_2, text, flags=re.DOTALL)

# Replace 5.1
s5_1 = r"""#### 5.1. Determinación de Recursos y Logística (RA 2.e, RA 3.b)...................................................

Se especifican los recursos de hardware, software y humanos con su asignación específica a las tareas correspondientes:

| Recurso / Perfil | Tipo | Tareas Asignadas | Justificación y Logística |
| :--- | :--- | :--- | :--- |
| **Administrador de Sistemas** | Perfil Humano | Fases 1, 2, 4.2, 5 | Rol principal encargado del despliegue base, redes, seguridad y normativas. |
| **Desarrollador (Java/DBA)** | Perfil Humano | Fase 3 | Encargado del código y la arquitectura de la base de datos MySQL. |
| **Servidores (x3 VMs)** | Hardware | Tareas 2.1 a 6 | Nodos para Web (DMZ), BBDD/App y Gestión (AD-DC, Monitoreo). |
| **Ubuntu Server LTS** | Software (OS) | Tarea 2.1 | SO de los servidores, provisto mediante imagen ISO. |
| **MySQL & Samba4** | Software | Tareas 2.3, 3.1 | Motor de BBDD y controlador de dominio, ambos Open Source. |
| **Zabbix, Wazuh, Suricata** | Software | Tarea 5.2 | Stack de monitoreo y ciberseguridad. |
| **IntelliJ IDEA Ultimate** | Licencia IDE | Tarea 3.3 | Herramienta de desarrollo solicitada bajo licencia Github Education. |
| **Dispositivos IoT (Centralita)** | Hardware | Tarea 4.1 | Hardware in-situ para simular el control de acceso en las propiedades. |

"""
text = re.sub(r'#### 5\.1\. Determinación de Recursos Materiales.+?(?=#### 5\.2\. Presupuesto)', s5_1, text, flags=re.DOTALL)

# Replace 5.2
s5_2 = r"""#### 5.2. Presupuesto Económico y de Ejecución (RA 2.f, RA 3.g).....................................................

El presupuesto incluye la inversión material (CAPEX) y los costes operativos de la ejecución y mano de obra (OPEX).

| Concepto de Gasto | Tipo de Coste | Coste Estimado (€) | Detalles y Justificación |
| :--- | :--- | :--- | :--- |
| **Hardware Servidores (Virtualización)** | Inversión (Material) | 1.500 € | Servidor físico potente para desplegar las 3 VMs de la plataforma. |
| **Licencias de Software** | Inversión (Software) | 0 € | Uso de tecnologías Open Source (Ubuntu, MySQL, Zabbix). |
| **Dispositivos IoT (Piloto)** | Inversión (Material) | 500 € | Adquisición de cerraduras/sensores para 5 propiedades iniciales. |
| **Mano de Obra (Implementación)** | Operativo (Ejecución) | 2.640 € | 110 horas de SysAdmin/Dev a 24€/hora (Cálculo horas de ejecución para la puesta en marcha). Esto asume roles internos costeados a mercado. |
| **Costes de Despliegue y Hosting** | Operativo (Ejecución) | 300 € | Dominio anual, cloud contingencia, consumo eléctrico adicional de pruebas. |
| **Marketing Lanzamiento** | Operativo | 1.000 € | Campaña inicial de captación en redes sociales y material gráfico. |
| **TOTAL ESTIMADO** | | **5.940 €** | Coste global para el diseño, ejecución técnica y puesta en marcha del MVP. |

"""
text = re.sub(r'#### 5\.2\. Presupuesto Económico \(RA 2\.f\).*?(?=#### 5\.3\. Necesidades)', s5_2, text, flags=re.DOTALL)

# Replace 7 and append Fase III
s7 = r"""### 7. Diagrama de Gantt de las Fases del Proyecto y Asignación de Recursos (RA 3.f)....................................

El cronograma detalla las 11 semanas de ejecución del MVP, ilustrando la dependencia temporal y el perfil responsable en cada sub-tarea:

| Semana | Fase / Sub-tareas Principales | Perfil Responsable | Dependencias |
| :--- | :--- | :--- | :--- |
| **S1-S2** | **Fase 1:** Diseño, arquitectura y documentación técnica. | Administrador de Sistemas | Ninguna |
| **S3** | **Fase 2:** Instalación OS (2.1) y Config. Red/VLANs (2.2). | Administrador de Sistemas | Bloquea a S4 |
| **S4** | **Fase 2:** Despliegue AD-DC (2.3) y Firewall (2.4). | Administrador de Sistemas | Depende de S3 |
| **S5-S6** | **Fase 3:** Setup MySQL (3.1) y Estructura BBDD (3.2). | DBA / Desarrollador | Depende de S3 |
| **S7** | **Fase 3:** Desarrollo Java Socket App (3.3). | Desarrollador Java | Depende de S6 |
| **S8** | **Fase 4:** Integración IoT a AD (4.1) y SSL/VPN (4.2). | SysAdmin / Técnico IoT | Depende de S4 |
| **S9** | **Fase 5:** Scripts automatizados de Backup (5.1). | Administrador de Sistemas | Depende de S3 |
| **S10** | **Fase 5:** Monitorización Zabbix/Wazuh (5.2). | Administrador de Sistemas | Depende de S3 |
| **S11** | **Fase 6:** QA, Pruebas de Carga/Seguridad y Despliegue final. | Equipo de Pruebas | Depende de S10 |


---

## Fase III: Organización de la Ejecución y Plan de Intervención

A continuación se detalla la documentación técnica asociada a la ejecución real de las tareas planificadas, estableciendo requisitos legales, procedimientos de actuación y medidas preventivas (PRL).

### 1. Requisitos Legales y Administrativos (RA 3.c)

Antes de iniciar cualquier despliegue técnico, es obligatorio asegurar el cumplimiento de las normativas de software, los accesos físicos y la protección de datos que este entorno conlleva.

*   **Licencias de Software:**
    *   **Ubuntu Server LTS:** Licencia *Open Source* (GNU GPL). Se asume su uso gratuito en producción.
    *   **MySQL & Samba4:** Licencias GNU GPL. Permitido su uso en entornos corporativos sin coste por licenciamiento.
    *   **IntelliJ IDEA Ultimate:** Uso bajo licencia *GitHub Education* (limitada exclusivamente a desarrollo y validación, requiriendo licencia comercial para modificaciones posteriores fuera del entorno educativo).
*   **Permisos de Acceso y Autorizaciones:**
    *   **Acceso Físico a Servidores / CPD:** El Jefe de Proyecto (o el gerente inmobiliario responsable en las propiedades piloto IoT) autorizará formalmente el acceso a los armarios de red o salas de servidores.
    *   **Credenciales Administrativas:** Se implementará un mecanismo de control de accesos. Solo el perfil de *Administrador de Sistemas principal* dispondrá de la clave `root`/`admin` inicial, que posteriormente delegará.
*   **Cumplimiento Normativo (RGPD/LOPD):**
    *   La plataforma maneja datos extremadamente sensibles (reservas de usuarios, identificaciones/documentos, control de acceso físico).
    *   Se requiere que los administradores de la BBDD firmen un NDA (Acuerdo de Confidencialidad). Durante el desarrollo y pruebas (Fase 6) se usarán **datos de usuarios falsos o anonimizados**.
    *   Se implementarán políticas de retención de registros o logs para no almacenar IPs o identidades IoT más allá del tiempo legal requerido.

### 2. Procedimientos de Actuación y Ejecución (RA 3.d)

En este apartado se describe **cómo** llevar a cabo de forma secuencial las configuraciones críticas base, así como sus mecanismos de prueba y de marcha atrás.

#### 2.1. Procedimiento de Implementación Base y Hardening (Ejemplo Servidor DMZ - Fase 2)
Para el despliegue del nodo Web/DMZ, el SysAdmin deberá ejecutar estos pasos:
1.  **Instalación SO:** Desplegar imagen ISO Ubuntu Server 24.04 LTS en la VM pública. Crear una cuenta de nivel bajo llamada `quick_admin` (no utilizar usuario nativo root).
2.  **Configuración de Red Estática:** Acudir a `/etc/netplan/00-installer-config.yaml` y definir la configuración estática con el Gateway de red pública. Aplicar con `netplan apply`.
3.  **Endurecimiento (Hardening) de SSH:** 
    *   Desactivar login de `root` (`PermitRootLogin no` en `/etc/ssh/sshd_config`).
    *   Cambiar puerto SSH por defecto a un puerto no estándar (ej. puerto 2222) para evadir bots de búsqueda pasiva.
4.  **Cierre Perimetral Inicial (nftables):**
    *   Se generará un script de reglas predeterminado `DROP` (descartar todo). Formular explícitamente permisos *solo* para puertos entrantes HTTP (80), HTTPS (443) y al nuevo puerto SSH limitando su acceso a IPs de la red de Gestión.

#### 2.2. Plan de Pruebas de Aceptación (Checklist de Validación)
Para verificar la correcta ejecución de cada módulo:
* [ ] **Cierre de Red:** Ping desde DMZ a zona DB resulta en `timeout` (fallo explícito por el firewall).
* [ ] **VPN Administrativa:** Conexión desde el cliente remoto, verificando entrega de IP dentro del túnel y ping al Gateway de Gestión.
* [ ] **Registro IoT:** La centralita de prueba logra autenticarse contra el controlador de dominio AD-DC.
* [ ] **Recuperación (Backup):** El script automatizado `cron` genera un archivo `.tar.gz` íntegro y sin errores en el directorio prefijado.

#### 2.3. Plan de Contingencia y Rollback (Marcha Atrás)
Si la instalación o configuración de una sub-tarea (p.e. actualización de esquemas SQL en producción, o instalación de políticas de dominio en Samba4) falla de manera irrecuperable en ese momento:
1.  **Activación de Aislamiento:** Apagar inmediatamente la vNIC (interfaz de red) de la máquina virtual afectada si se sospecha brecha de red para aislar el error.
2.  **Restauración a Snapshot Inicial:** La metodología indica la creación obligatoria de un *Snapshot* previo a configuraciones mayores (el cual servirá como punto de restauración a su estado prístino). Se ejecutará la reversión del mismo, anulando los cambios temporales fallidos.
3.  **Diagnóstico y Acción:** Proceder al volcado y lectura de `/var/log/syslog` en entornos emulados (o el propio servidor temporal) para trazar el evento fatídico (Post-mortem) antes de la siguiente iteración de despliegue.

### 3. Plan de Prevención de Riesgos Laborales (PRL) (RA 3.e)

Durante esta fase, los riesgos no recaen sobre el inmueble alquilado (*que sería competencia ajena*), sino sobre el personal técnico encargado de la ejecución informática en despachos o en los CPD (Data Centers).

*   **Riesgos Físicos, Ergonómicos y Visuales:**
    *   *Riesgo Evaluado:* Riesgo importante de desarrollar trastornos musculoesqueléticos (ej. síndrome del túnel carpiano por teclado mecánico, afecciones cervicales) debidos al uso prolongado de las Pantallas (PVD) por parte de operadores logrando la monitorización en vivo o programando el código.
    *   *Medidas Preventivas en Ejecución:* Disposición óptima del equipo: El borde superior del monitor debe coincidir en horizontal con la línea de los ojos o ligeramente por debajo; proveer al usuario de soporte lumbar; aplicar pausas mecánicas de 10 min por cada hora de operación contínua del equipo informático.
*   **Riesgos Eléctricos Directos en Infraestructura:**
    *   *Riesgo Evaluado:* Riesgo de contacto eléctrico (directo o indirecto) durante el montaje físico en racks del servidor de virtualización y en la manipulación in-situ de cableado de potencia en las cerraduras IoT.
    *   *Medidas Preventivas en Ejecución:* Prohibición total de manipulación empírica de regletas o elementos PDU bajo tensión en CPDs o salas de TI. Aplicación de pulsera de descarga antiestática (ESD) antes de interactuar y empleo de calzado disipativo.
*   **Riesgos Psicosociales (Estrés Agudo o *Burnout*):**
    *   *Riesgo Evaluado:* Durante las Fases 3 y 6, es habitual que la presión por fechas de entrega ajustadas frente a fallas técnicas, desencadene cuadros de estrés al *SysAdmin* o Desarrollador.
    *   *Medidas Preventivas en Ejecución:* Ceñirse exclusivamente a la temporalización del Gantt sin aceptar requerimientos *ad hoc* o no estipulados; establecer tiempos de reacción en cortes reales para el equipo; rechazar de forma estandarizada los turnos intempestivos prolongados (*crunch*), priorizando la calidad y no la velocidad.

"""
text = re.sub(r'### 7\. Diagrama de Gantt de las Fases del Proyecto.*', s7, text, flags=re.DOTALL)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(text)

print("Exito")
