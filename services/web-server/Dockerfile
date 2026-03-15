FROM php:8.2-apache

LABEL maintainer="Antonio López Montes"
LABEL description="QuickStay Web Server (Apache + PHP)"

# Instalar extensiones necesarias
RUN apt-get update && apt-get install -y \
    libpng-dev \
    libjpeg-dev \
    libfreetype6-dev \
    libzip-dev \
    zabbix-agent \
    openssl \
    zip \
    unzip \
    git \
    && docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install gd zip pdo pdo_mysql mysqli \
    && rm -rf /var/lib/apt/lists/*

# Habilitar módulos de Apache
RUN a2enmod rewrite headers ssl

# Configurar VirtualHost
COPY vhost.conf /etc/apache2/sites-available/000-default.conf
COPY entrypoint.sh /usr/local/bin/web-entrypoint.sh
RUN chmod +x /usr/local/bin/web-entrypoint.sh

# Crear directorio para logs
RUN mkdir -p /var/log/apache2

EXPOSE 80 443

ENTRYPOINT ["/usr/local/bin/web-entrypoint.sh"]
CMD ["apache2-foreground"]
