FROM mysql:8.0-bookworm

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay MySQL con Zabbix Agent"

RUN apt-get update && apt-get install -y \
    zabbix-agent \
    openssl \
    && rm -rf /var/lib/apt/lists/*

COPY docker-entrypoint-zabbix.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint-zabbix.sh

ENTRYPOINT ["/usr/local/bin/docker-entrypoint-zabbix.sh"]
CMD ["mysqld"]
