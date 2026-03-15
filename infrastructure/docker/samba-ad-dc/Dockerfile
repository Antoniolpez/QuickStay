FROM ubuntu:22.04

LABEL maintainer="Antonio López Montes"
LABEL description="Samba Active Directory Domain Controller"

ENV DEBIAN_FRONTEND=noninteractive

# Instalar Samba AD-DC y dependencias (incluye herramientas de red y routing)
RUN apt-get update && apt-get install -y \
    samba \
    smbclient \
    winbind \
    libpam-winbind \
    libnss-winbind \
    krb5-user \
    krb5-config \
    dnsutils \
    net-tools \
    iproute2 \
    iputils-ping \
    && rm -rf /var/lib/apt/lists/*

# Copiar script de inicialización
COPY entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/entrypoint.sh

# Exponer puertos necesarios para AD-DC
EXPOSE 53/tcp 53/udp 88/tcp 88/udp 135/tcp 137/udp 138/udp 139/tcp \
       389/tcp 389/udp 445/tcp 464/tcp 464/udp 636/tcp 3268/tcp 3269/tcp

VOLUME ["/var/lib/samba", "/etc/samba"]

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
