#!/bin/bash
# 08_setup_web.sh
# Configures Apache Web Server for QuickStay DMZ (Fase 3 / Guia 5)
# Usage: sudo ./08_setup_web.sh

echo "Installing Apache2..."
apt-get update
apt-get install -y apache2

echo "Enabling Modules..."
a2enmod rewrite headers

echo "Configuring VirtualHost for quickstay.local..."
cat <<EOF > /etc/apache2/sites-available/quickstay.conf
<VirtualHost *:80>
    ServerAdmin admin@quickstay.local
    ServerName quickstay.local
    ServerAlias www.quickstay.local
    DocumentRoot /var/www/html/quickstay
    ErrorLog \${APACHE_LOG_DIR}/error.log
    CustomLog \${APACHE_LOG_DIR}/access.log combined
    
    # Simple Health Check Endpoint
    <Location /health>
        Require all granted
        SetHandler server-status
    </Location>
</VirtualHost>
EOF

echo "Deploying Demo Content..."
mkdir -p /var/www/html/quickstay
echo "<h1>Welcome to QuickStay</h1><p>Connected to App Server via 172.16.20.10</p>" > /var/www/html/quickstay/index.html
chown -R www-data:www-data /var/www/html/quickstay

echo "Enabling Site..."
a2ensite quickstay.conf
a2dissite 000-default.conf

echo "Restarting Apache..."
systemctl restart apache2

echo "Web Server Configured."
