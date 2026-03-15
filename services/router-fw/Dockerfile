FROM ubuntu:22.04

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay Router/Firewall with nftables"

# Instalar herramientas de red y firewall
RUN apt-get update && apt-get install -y \
    nftables \
    haproxy \
    python3 \
    python3-flask \
    python3-gunicorn \
    python3-requests \
    zabbix-agent \
    openssl \
    qrencode \
    wireguard-tools \
    iproute2 \
    iputils-ping \
    nmap \
    net-tools \
    tcpdump \
    openssh-server \
    vim \
    && rm -rf /var/lib/apt/lists/*

# Habilitar IP forwarding
RUN echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf && \
    echo "net.ipv4.conf.all.forwarding=1" >> /etc/sysctl.conf

# Copiar script de inicialización del firewall
COPY entrypoint.sh /usr/local/bin/
COPY router_admin.py /usr/local/bin/
COPY router_admin_templates /usr/local/bin/router_admin_templates
COPY router_admin_static /usr/local/bin/router_admin_static
RUN chmod +x /usr/local/bin/entrypoint.sh /usr/local/bin/router_admin.py

# Exponer puerto SSH (opcional para debug)
EXPOSE 22

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["tail", "-f", "/dev/null"]
