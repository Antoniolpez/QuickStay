#!/bin/bash
set -e

start_zabbix_agent() {
    if [ "${ZABBIX_AGENT_ENABLE:-1}" != "1" ]; then
        return 0
    fi

    if ! command -v zabbix_agentd >/dev/null 2>&1; then
        return 0
    fi

    if [ -z "${ZABBIX_AGENT_SERVER:-}" ]; then
        echo "[WARN] ZABBIX_AGENT_SERVER vacio; se omite agente en app"
        return 0
    fi

    mkdir -p /etc/zabbix /tmp
    local host="${ZABBIX_AGENT_HOSTNAME:-$(hostname)}"
    local active="${ZABBIX_AGENT_ACTIVE_SERVER:-${ZABBIX_AGENT_SERVER}}"
    local meta="${ZABBIX_AGENT_METADATA:-quickstay-app}"

    cat >/etc/zabbix/zabbix_agentd.conf <<EOF
PidFile=/tmp/zabbix_agentd.pid
LogFile=/tmp/zabbix_agentd.log
LogFileSize=5
Server=${ZABBIX_AGENT_SERVER}
ServerActive=${active}
Hostname=${host}
HostMetadata=${meta}
Timeout=10
TLSConnect=unencrypted
TLSAccept=unencrypted
EOF

    zabbix_agentd -c /etc/zabbix/zabbix_agentd.conf || echo "[WARN] no se pudo iniciar zabbix_agentd en app"
}

start_zabbix_agent

echo "==> Iniciando QuickStay Application Server"

# Configurar conexión a base de datos desde variables de entorno
cat > /app/config.properties <<EOC
# Database Configuration
db.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
db.user=${DB_USER}
db.password=${DB_PASSWORD}

# IoT Configuration
mqtt.broker=tcp://${MQTT_BROKER_HOST}:${MQTT_BROKER_PORT}
EOC

echo "==> Configuración generada"
echo "==> Base de datos: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "==> Usuario DB: ${DB_USER}"

# Esperar a que MySQL esté disponible
echo "==> Esperando disponibilidad de MySQL..."
while ! nc -z ${DB_HOST} ${DB_PORT}; do
    sleep 2
    echo "   Esperando MySQL en ${DB_HOST}:${DB_PORT}..."
done
echo "==> MySQL disponible"

# Ejecutar servidor Java
echo "==> Iniciando servidor Java..."
APP_JAR="/app/app.jar"
if [ ! -f "$APP_JAR" ]; then
    APP_JAR="$(find /app -maxdepth 1 -type f -name '*.jar' | head -n 1)"
fi

if [ -z "$APP_JAR" ] || [ ! -f "$APP_JAR" ]; then
    echo "[ERROR] No se encontró JAR de aplicación en /app"
    exit 1
fi

echo "==> JAR detectado: $APP_JAR"
exec java ${JAVA_OPTS} -Dlog4j.configurationFile=/app/log4j2.xml -cp "/app:/app/lib/*:$APP_JAR" ProyectoFinal.Servidor.Servidor
