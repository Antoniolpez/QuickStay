FROM haproxy:2.8-alpine

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay HAProxy Load Balancer"

# La configuración de HAProxy se monta en tiempo de ejecución
# desde ./infrastructure/config/haproxy/haproxy.cfg (ver docker-compose.yml)

# Exponer puertos
EXPOSE 80 443 8404

CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy.cfg"]
