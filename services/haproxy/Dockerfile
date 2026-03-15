FROM haproxy:2.8

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay HAProxy Load Balancer"

# Copiar configuración personalizada
COPY infrastructure/config/haproxy/haproxy.cfg /usr/local/etc/haproxy/haproxy.cfg
COPY infrastructure/docker/haproxy/entrypoint.sh /usr/local/bin/lb-entrypoint.sh

USER root

RUN apt-get update && apt-get install -y \
	zabbix-agent \
	openssl \
	&& rm -rf /var/lib/apt/lists/* && chmod +x /usr/local/bin/lb-entrypoint.sh

# Exponer puertos
EXPOSE 80 443 8404

ENTRYPOINT ["/usr/local/bin/lb-entrypoint.sh"]
CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy.cfg"]
