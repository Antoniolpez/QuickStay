FROM ubuntu:22.04

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay MQTT Broker machine with Zabbix agent"

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    mosquitto \
    mosquitto-clients \
    zabbix-agent \
    openssl \
    && rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /usr/local/bin/mqtt-entrypoint.sh
RUN chmod +x /usr/local/bin/mqtt-entrypoint.sh

EXPOSE 1883 8883

ENTRYPOINT ["/usr/local/bin/mqtt-entrypoint.sh"]
CMD ["mosquitto", "-c", "/etc/mosquitto/mosquitto.conf"]
