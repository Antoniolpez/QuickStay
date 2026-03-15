FROM ubuntu:22.04

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay Router/Firewall with nftables"

# Instalar herramientas de red y firewall
RUN apt-get update && apt-get install -y \
    nftables \
    iproute2 \
    iputils-ping \
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
RUN chmod +x /usr/local/bin/entrypoint.sh

# Exponer puerto SSH (opcional para debug)
EXPOSE 22

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["tail", "-f", "/dev/null"]
