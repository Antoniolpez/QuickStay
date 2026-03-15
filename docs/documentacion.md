## Análisis del Contexto y Diagnóstico de Necesidades: Proyecto QuickStay.........................

**Realizado por:** Antonio López Montes


- Análisis del Contexto y Diagnóstico de Necesidades: Proyecto QuickStay.........................
   - Resumen Ejecutivo............................................................................................................................................
   - 1. Análisis del Contexto...................................................................................................................................
      - 1.1. Contexto Socioeconómico y Tendencias del Mercado........................................................
      - 1.2. Ventaja Competitiva y Estrategia de Diferenciación (Modelo de Porter)..................
   - 2. Diagnóstico de Necesidades.....................................................................................................................
      - 2.1. Necesidades del Mercado (El Problema).................................................................................
      - 2.2. Necesidades Técnicas (La Solución Tecnológica).................................................................
   - 3. Integración Tecnológica (Digitalización de QuickStay)................................................................
   - 4. Conclusión y Justificación del Proyecto...............................................................................................
      - Justificación...................................................................................................................................................
- Fase II: Diseño Detallado y Planificación.......................................................................
   - 1. Presentación y Análisis de la Información Técnica........................................................................
      - 1.1. Tecnologías Habilitadoras Digitales...........................................................................................
      - 1.2. Documentación Técnica y Requisitos de Hardware............................................................
         - 1.2.1. Documentación de la Tecnología Elegida.....................................................................
         - 1.2.2. Requisitos de Hardware Específicos...............................................................................
   - 2. Objetivos Específicos (SMART)...............................................................................................................
   - 3. Estudio de Viabilidad Técnica y Análisis de Riesgos......................................................................
      - 3.1. Viabilidad Técnica..............................................................................................................................
      - 3.2. Análisis de Riesgos Técnicos.......................................................................................................
   - 4. Diseño de Arquitectura Detallada.......................................................................................................
      - 4.1. Diagrama de Topología de Red Lógico...................................................................................
      - 4.2. Esquema de Fases del Proyecto.................................................................................................
   - 5. Recursos y Presupuesto Económico...................................................................................................
      - 5.1. Determinación de Recursos y Logística ...................................................
      - 5.2. Presupuesto Económico y de Ejecución...................................................
      - 5.3. Necesidades de Financiación....................................................................
   - 6. Control de Calidad y Planificación de la Evaluación....................................................................
      - 6.1. Aspectos de Control de Calidad................................................................................
      - 6.2. Metodología de Pruebas...............................................................................................................
   - 7. Diagrama de Gantt de las Fases del Proyecto y Asignación de Recursos...
- Fase III: Organización de la Ejecución y Plan de Intervención..............................................................
   - 1. Requisitos Legales y Administrativos...............................................................................
   - 2. Procedimientos de Actuación y Ejecución....................................................................
      - 2.1. Procedimiento de Implementación Base y Hardening.....................................................
      - 2.2. Plan de Pruebas de Aceptación (Checklist de Validación)........................................
      - 2.3. Plan de Contingencia y Rollback (Marcha Atrás)............................................................
   - 3. Plan de Prevención de Riesgos Laborales (PRL)......................................................


### Resumen Ejecutivo............................................................................................................................................

QuickStay es una plataforma digital innovadora de alquiler express de espacios por horas o
días, concebida para responder a la creciente necesidad urbana de espacios privados,
flexibles y accesibles para usos temporales, como reuniones, descanso, estudio o
socialización espontánea.

A diferencia de las plataformas tradicionales de alojamiento (centradas en estancias
nocturnas completas), QuickStay se distingue por su **flexibilidad temporal** y su **enfoque
tecnológico integral (PropTech)**. Aplica tecnologías de **Inteligencia Artificial (IA),
Internet de las Cosas (IoT), Big Data y Ciberseguridad** para ofrecer una experiencia
automatizada, segura y sostenible. Este proyecto combina viabilidad técnica, sostenibilidad
urbana y transformación digital, materializando la infraestructura de red, servidores y
seguridad que hacen posible su funcionamiento.

### 1. Análisis del Contexto...................................................................................................................................

#### 1.1. Contexto Socioeconómico y Tendencias del Mercado........................................................

El entorno actual se caracteriza por una sociedad más digital, móvil y flexible, marcada por
el auge del trabajo remoto y la economía colaborativa. En este contexto, emergen
tendencias clave:

● **Flexibilidad y movilidad:** La sociedad post-COVID prioriza la adaptabilidad del
tiempo y el espacio. QuickStay ofrece una alternativa ágil y económica para quienes
necesitan espacios de uso inmediato, sin los compromisos del alquiler tradicional.

● **Infrautilización de espacios urbanos:** Muchas propiedades permanecen vacías gran
parte del día. QuickStay impulsa su aprovechamiento eficiente, fomentando la
economía circular y la sostenibilidad urbana.

● **Transformación PropTech:** El sector inmobiliario vive una revolución digital.
QuickStay se alinea con esta tendencia mediante la integración de IT y OT (Tecnologías
de la Información y de la Operación), aportando valor mediante automatización,
seguridad y analítica avanzada.

#### 1.2. Ventaja Competitiva y Estrategia de Diferenciación (Modelo de Porter)..................

QuickStay aplica una estrategia de **diferenciación tecnológica** , centrada en la
automatización, la confianza digital y la flexibilidad temporal. Esta estrategia sitúa a
QuickStay como una PropTech sostenible, confiable y automatizada, adaptada al usuario
contemporáneo.

Aspecto QuickStay

```
Competencia (Airbnb,
Booking)
```
**Duración de uso** Por horas o días
(modelo express)

```
Por noches completas
```

**Tecnología** IA, IoT, Big Data,
Ciberseguridad

```
Plataforma de reservas
central tradicional
```
**Control de acceso** Cerraduras inteligentes
y sensores IoT

```
Llaves físicas o
manuales
```
**Seguridad y
reputación**

```
Sistema de verificación
documental + IA
```
```
Reseñas básicas
reputacional
```
**Gestión operativa** Integración IT + OT
(control remoto,
mantenimiento
predictivo)

```
Operaciones manuales
```
### 2. Diagnóstico de Necesidades.....................................................................................................................

#### 2.1. Necesidades del Mercado (El Problema).................................................................................

Los usuarios y propietarios presentan necesidades no cubiertas por los modelos de alquiler
tradicionales:

● **Espacios de uso inmediato:** Los usuarios demandan lugares privados y versátiles sin
estancias mínimas de 24 horas. QuickStay satisface esta necesidad con reservas
express.
● **Seguridad y confianza:** Se requiere una plataforma que garantice protección de datos
(RGPD), identidad verificada y fiabilidad de las propiedades.
● **Agilidad en el proceso:** Los modelos tradicionales son lentos. QuickStay permite
reservar, pagar y acceder a una propiedad en minutos.

● **Rentabilización de espacios:** Los propietarios necesitan opciones para monetizar
espacios ociosos sin comprometer la seguridad.

● **Coste:** Al reducir la instancia de uso, se pueden reducir costes y abarcar varios grupos
en un mismo día.

#### 2.2. Necesidades Técnicas (La Solución Tecnológica).................................................................

La digitalización integral de QuickStay exige una infraestructura sólida, escalable y segura.
Estas medidas garantizan disponibilidad, seguridad y eficiencia, pilares de la
infraestructura digital del proyecto.

Necesidad Solución Técnica

**Infraestructura y escalabilidad** Servidores Linux independientes
(Web y DB), arquitectura con VLANs
por propiedad y topología
segmentada.

**Seguridad y acceso** Firewall (iptables), VPN de
administración segura, cifrado
SSL/TLS, cumplimiento del RGPD.


**Gestión centralizada** Controlador de Dominio (AD-DC)
para usuarios y centralitas IoT
registradas como objetos de red.

**Monitoreo y mantenimiento** Sistema de monitoreo en tiempo
real (CPU, red, IoT).

**Automatización y respaldo** Scripts en Bash para copias de
seguridad periódicas y
mantenimiento automatizado.

### 3. Integración Tecnológica (Digitalización de QuickStay)................................................................

QuickStay se basa en una infraestructura digital propia, sustentada en un servidor Java con
base de datos MySQL, que da servicio a aplicaciones de escritorio y Android mediante
sockets personalizados. La plataforma incorpora:

● **IT (Tecnologías de la Información):** Gestión de datos, reservas y pagos,
comunicación en tiempo real y reputación digital.

● **OT (Tecnologías de la Operación):** Control de acceso mediante cerraduras
inteligentes, sensores IoT y monitoreo de ocupación.

● **Fog, Edge y Mist Computing:** Procesamiento distribuido que reduce latencia y mejora
la respuesta entre servidor, dispositivos y usuarios.

● **Inteligencia Artificial:** Recomendaciones inteligentes, verificación documental,
optimización de precios y chatbots de atención.

● **Ciberseguridad:** Cifrado SHA512, gestión de identidades robusta, autenticación de
doble factor y control de acceso local.

Esta integración tecnológica convierte a QuickStay en un ejemplo de empresa digital
plenamente conectada, coherente con las tendencias de la Industria 4.0.

### 4. Conclusión y Justificación del Proyecto...............................................................................................

QuickStay representa la convergencia entre innovación tecnológica, sostenibilidad urbana y
transformación digital. Su propuesta no se limita a una idea de negocio, sino que constituye
una solución técnica viable y escalable que optimiza recursos y fomenta la economía
colaborativa.

El proyecto propuesto materializa esta visión, aportando una infraestructura profesional
que incluye:

- Arquitectura de red segmentada y segura.
- Servidores optimizados con gestión centralizada.
- Cumplimiento del RGPD y ciberseguridad avanzada.
- Monitoreo y automatización del mantenimiento.


#### Justificación...................................................................................................................................................

1. **Relevancia de mercado:** Responde a la demanda real de flexibilidad y sostenibilidad
    en el alquiler temporal.
2. **Innovación tecnológica:** Fusiona IT y OT aplicando IA, IoT, Big Data y Cloud/Fog
    Computing.
3. **Aplicación práctica del ASIR:** Demuestra competencia profesional en redes,
    seguridad, administración de sistemas y automatización.

**Expansión:** Se podría intentar desarrollar la tecnología túnel que caracteriza a NGROK.


## Fase II: Diseño Detallado y Planificación.......................................................................

**Proyecto:** QuickStay - Plataforma de Alquiler Express **Módulo:** Administración de Sistemas
Informáticos en Red (ASIR) **Autor:** Antonio López Montes

### 1. Presentación y Análisis de la Información Técnica........................................................................

El proyecto QuickStay se fundamenta en una infraestructura digital robusta que soporta
una plataforma de reservas y gestión de espacios temporales. La información técnica se
centra en la arquitectura de red, la seguridad y la automatización, elementos clave para el
éxito de una solución _PropTech_.

#### 1.1. Tecnologías Habilitadoras Digitales...........................................................................................

El proyecto se apoya en tecnologías de vanguardia para garantizar la eficiencia y la
seguridad:

Tecnología Aplicación en QuickStay

**Ciberseguridad** Cifrado SHA512, cumplimiento del RGPD,
gestión de identidades robusta y
prevención de fraudes.
Se estudia posible implementación de
Wazuh y suricata.

**Inteligencia Artificial (IA)** Recomendaciones inteligentes,
optimización de precios, verificación
documental y chatbots de atención.

**Big Data y Análisis de Datos** Predicción de tendencias y análisis de uso
para decisiones estratégicas.

**Internet de las Cosas (IoT)** Control de acceso mediante cerraduras
inteligentes, sensores de ocupación y
mantenimiento predictivo [Análisis
Contexto.pdf, p. 5].

**Computación en la Nube (Fog, Edge,
Mist)**

```
Procesamiento distribuido para reducir la
latencia y mejorar la respuesta entre el
servidor y los dispositivos IoT
```
#### 1.2. Documentación Técnica y Requisitos de Hardware............................................................

##### 1.2.1. Documentación de la Tecnología Elegida.....................................................................

El diseño del proyecto se basa en la robustez y el bajo coste de las soluciones de código
abierto, cumpliendo con los requisitos de viabilidad técnica.

```
● Sistema Operativo (SO): Linux (Ubuntu Server LTS). Elegido por su estabilidad,
seguridad inherente, bajo consumo de recursos y la ausencia de costes de licencia.
Es el estándar de la industria para servidores de aplicaciones y bases de datos.
```

```
● Firewall: nftables. Herramienta nativa de Linux que permite una gestión granular de
las reglas de filtrado de paquetes, esencial para implementar la segmentación de red
(DMZ, Servicios, Gestión) y proteger los servidores de la Zona de Servicios.
● Gestión de Identidades: Controlador de Dominio (AD-DC). Se utilizará una
implementación de código abierto (ej. Samba4) para centralizar la autenticación de
administradores y la gestión de las centralitas IoT, simplificando la aplicación de
políticas de seguridad.
● Monitorización y Seguridad:
● Zabbix/Nagios: Para el monitoreo en tiempo real de métricas de rendimiento (CPU,
RAM, red) y disponibilidad (uptime) de los servidores.
● Wazuh (SIEM/HIDS): Se implementará para la recolección y análisis centralizado de
logs de seguridad, detección de vulnerabilidades y monitorización de la integridad
de los archivos críticos.
● Suricata (IDS/IPS): Se desplegará en el perímetro de la red para la detección y
prevención de intrusiones a nivel de tráfico.
```
##### 1.2.2. Requisitos de Hardware Específicos...............................................................................

El proyecto se implementará sobre una infraestructura de virtualización para optimizar el
uso de recursos. Se requieren tres servidores virtuales (o contenedores) con los siguientes
requisitos mínimos:

Servidor Función Principal Requisitos Mínimos
de Hardware

```
Justificación
```
Servidor Web (DMZ) Acceso público,
Servidor
HTTP/HTTPS.

```
2 vCPU, 4 GB RAM, 50
GB SSD
```
```
Alto tráfico de
peticiones de reserva.
Requiere RAM para
caché y CPU para
cifrado SSL/TLS.
```
Servidor de
Aplicación/DB

```
Servidor Java, MySQL,
Módulo IA.
```
```
4 vCPU, 8 GB RAM,
100 GB SSD
```
```
El módulo de IA y la
BBDD son los
componentes más
intensivos en CPU y
RAM.
```
Servidor de Gestión AD-DC,
Zabbix/Nagios,
Wazuh/Suricata.

```
2 vCPU, 6 GB RAM, 80
GB SSD
```
```
El SIEM (Wazuh) y el
monitoreo (Zabbix)
requieren RAM para
el procesamiento de
logs y la base de
datos interna.
```

### 2. Objetivos Específicos (SMART)...............................................................................................................

Los objetivos de esta fase se centran en la implementación de la infraestructura técnica que
soporta la plataforma QuickStay:

Objetivo Criterio SMART Descripción

**O1.**

**Infraestructura Segura**

```
Específico, Medible,
Alcanzable, Relevante,
Temporal
```
```
Implementar la arquitectura
de red segmentada (VLANs)
y el firewall (nftables) en el
servidor principal en un
plazo de 4 semanas ,
garantizando un nivel de
seguridad A+ en tests de
penetración básicos.
```
**O2.**

**Gestión Centralizada**

```
Específico, Medible,
Alcanzable, Relevante,
Temporal
```
```
Configurar el Controlador
de Dominio (AD-DC) para la
gestión de usuarios y
dispositivos IoT en un plazo
de 2 semanas , permitiendo
la autenticación
centralizada del 100% de
los administradores y
dispositivos.
```
**O3.**

**Automatización de
Mantenimiento**

```
Específico, Medible,
Alcanzable, Relevante,
Temporal
```
```
Desarrollar y programar los
scripts en Bash para copias
de seguridad diarias y
mantenimiento
automatizado en un plazo
de 3 semanas , asegurando
una tasa de éxito del 99%
en las copias de seguridad.
```
### 3. Estudio de Viabilidad Técnica y Análisis de Riesgos......................................................................

#### 3.1. Viabilidad Técnica..............................................................................................................................

El proyecto QuickStay es **técnicamente viable** con las herramientas propuestas. La
infraestructura se basa en tecnologías maduras y de código abierto (Linux, Java, MySQL)
combinadas con soluciones comerciales de IoT (cerraduras inteligentes).

● **Solución Técnica:** Servidores Linux independientes (Web y DB) con arquitectura
segmentada (VLANs).


● **Justificación:** La separación de servicios en servidores independientes y la
segmentación de red minimizan el riesgo de fallos en cascada y facilitan la
escalabilidad horizontal. La elección de Java y MySQL garantiza un rendimiento
robusto para la gestión de reservas en tiempo real.

#### 3.2. Análisis de Riesgos Técnicos.......................................................................................................

Riesgo Probabilidad Impacto Mitigación

**Fallo del Servidor
Principal**

```
Media Alto Implementación de servidores
independientes (Web/DB) y sistema de
respaldo automatizado con scripts
Bash.
```
**Ataque de
Ciberseguridad**

```
Media Alto Uso de cifrado SHA512, firewall
(nftables), VPN de administración y
cumplimiento estricto del RGPD.
```
**Fallo de
Conectividad IoT**

```
Baja Medio Uso de arquitecturas Fog/Edge/Mist
Computing para procesamiento
distribuido y reducción de latencia.
```
**Problemas de
Escalabilidad**

```
Media Medio Diseño de base de datos normalizada y
arquitectura de red segmentada para
facilitar la expansión.
```
### 4. Diseño de Arquitectura Detallada.......................................................................................................

#### 4.1. Diagrama de Topología de Red Lógico...................................................................................

La arquitectura lógica se centra en la segmentación de la red para aislar los servicios
críticos y los dispositivos de operación (OT).



#### 4.2. Esquema de Fases del Proyecto.................................................................................................

El proyecto se divide en fases secuenciales y dependientes para su implementación técnica, desglosando las tareas a nivel granular:

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

### 5. Recursos y Presupuesto Económico...................................................................................................

#### 5.1. Determinación de Recursos y Logística...................................................

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

#### 5.2. Presupuesto Económico y de Ejecución.....................................................

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


#### 5.3. Necesidades de Financiación....................................................................

El proyecto requiere una financiación inicial de **3.300 €** para cubrir los costes de desarrollo
y la infraestructura mínima.

● **Mecanismos de Financiación:** Se buscará financiación a través de **ayudas o
subvenciones** para la incorporación de nuevas tecnologías, especialmente
aquellas destinadas a _startups_ tecnológicas o proyectos de digitalización e innovación
(ej. Fondos FEDER, ENISA).

### 6. Control de Calidad y Planificación de la Evaluación....................................................................

#### 6.1. Aspectos de Control de Calidad................................................................................

Aspecto a Controlar Indicador de Calidad Metodología de Verificación

**Rendimiento** Tiempo de respuesta de la
API de reservas (Objetivo: <
500 ms).

```
Pruebas de carga (ej.
Apache JMeter) y monitoreo
en tiempo real (Zabbix).
```
**Seguridad** Tasa de intentos de acceso
fallidos (Objetivo: 0% en la
VPN de administración).

```
Auditorías de seguridad
periódicas y scans de
vulnerabilidades (ej.
Nessus, suricata y wazuh).
```
**Disponibilidad** Uptime del Servidor Web y
DB (Objetivo: 99.9%).

```
Monitoreo constante y
alertas automáticas del
sistema de monitoreo.
```
**Integración IoT** Tasa de éxito en la
apertura/cierre remoto de
cerraduras (Objetivo:
100%).

```
Pruebas funcionales en las
propiedades piloto.
```
#### 6.2. Metodología de Pruebas...............................................................................................................

Se utilizará una metodología de pruebas **funcionales y no funcionales** antes del
despliegue.

● **Pruebas Funcionales:** Verificación de que el proceso de reserva, pago y acceso
funciona correctamente de principio a fin.

● **Pruebas No Funcionales:** Incluyen las pruebas de rendimiento (carga) y las pruebas
de seguridad (pen-testing) para validar los indicadores de calidad.

### 7. Diagrama de Gantt de las Fases del Proyecto y Asignación de Recursos....................................

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

### 1. Requisitos Legales y Administrativos

Antes de iniciar cualquier despliegue técnico, es obligatorio asegurar el cumplimiento de las normativas de software, los accesos físicos y la protección de datos que este entorno conlleva.

*   **Licencias de Software:**
    *   **Ubuntu Server LTS:** Licencia *Open Source* (GNU GPL). Se asume su uso gratuito en producción.
    *   **MySQL & Samba4:** Licencias GNU GPL. Permitido su uso en entornos corporativos sin coste por licenciamiento.
    *   **IntelliJ IDEA Ultimate:** Uso bajo licencia *GitHub Education* (limitada exclusivamente a desarrollo y validación, requiriendo licencia comercial para modificaciones posteriores fuera del entorno educativo).
*   **Permisos de Acceso y Autorizaciones:**
    *   **Credenciales Administrativas:** Se implementará un mecanismo de control de accesos. Solo el perfil de *Administrador de Sistemas principal* dispondrá de la clave `root`/`admin` inicial, que posteriormente delegará.
*   **Cumplimiento Normativo (RGPD/LOPD):**
    *   La plataforma maneja datos extremadamente sensibles (reservas de usuarios, identificaciones/documentos, control de acceso físico).
    *   Se requiere que los administradores de la BBDD firmen un NDA (Acuerdo de Confidencialidad). Durante el desarrollo y pruebas (Fase 6) se usarán **datos de usuarios falsos o anonimizados**.
    *   Se implementarán políticas de retención de registros o logs para no almacenar IPs o identidades IoT más allá del tiempo legal requerido.

### 2. Procedimientos de Actuación y Ejecución 

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

### 3. Plan de Prevención de Riesgos Laborales (PRL)

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

