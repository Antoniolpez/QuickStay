# Politica de Imagenes - QuickStay

## Objetivo
Definir que servicios se construyen como imagen custom y cuales se mantienen con imagen de fabricante para equilibrar realismo de despliegue y mantenibilidad.

## Estrategia recomendada

- Maquinas simuladas de negocio/infra propia: `custom build`.
- Plataformas complejas de producto (monitoring/SIEM): imagen oficial versionada.

## Custom build (baseline corporativo)

- `router-fw`
- `load-balancer`
- `web-server-1`
- `web-server-2`
- `app-server`
- `mysql-db`
- `ad-dc-primary`
- `mqtt-broker`
- `iot-simulator`

Estas imagenes deben incluir:

- Paquetes y utilidades operativas del rol.
- `zabbix-agent` instalado y arranque via entrypoint.
- Configuracion por rol mediante variables (hostname, metadata, destino activo/pasivo).
- Healthcheck operativo del servicio principal.

## Imagen oficial versionada (upstream)

- `zabbix-server`
- `zabbix-web`
- `zabbix-db`
- `wazuh-manager`
- `wazuh-indexer`
- `wazuh-dashboard`
- `grafana`

Reglas:

- Prohibido `latest` en produccion.
- Fijar tag concreto y actualizar por ventana de cambio.
- Mantener configuracion declarativa por volumen/env y backups.

## Criterios de decision

Usar custom cuando:

- El servicio representa una maquina de la empresa en la maqueta.
- Se requiere baseline corporativo o agente interno por rol.
- El equipo necesita control estricto del runtime.

Usar oficial cuando:

- Es un producto de plataforma con ciclo propio de release.
- El coste de mantener un fork supera el beneficio.
- El upgrade controlado de upstream es prioritario.

## Operacion

- Validar cambios con `docker compose config --quiet`.
- Probar builds custom con `docker compose build <servicio>`.
- Desplegar con `docker compose up -d --build`.
- Verificar salud con `docker compose ps` y healthchecks.
