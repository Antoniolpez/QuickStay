#!/bin/bash
# 11_setup_lb.sh
# Phase 3 Guide 5: Load Balancer (Simulating F5 BIG-IP with Nginx)
# Usage: sudo ./11_setup_lb.sh

VIP="172.16.10.100"
WEB1="172.16.10.11"
WEB2="172.16.10.12"

echo "Installing Nginx..."
apt-get update
apt-get install -y nginx

echo "Configuring Nginx Load Balancing Pool..."
cat <<EOF > /etc/nginx/sites-available/quickstay_lb
upstream backend_pool {
    least_conn; # Method: Least Connections
    server $WEB1:80;
    server $WEB2:80;
    # Health checks are passive in standard Nginx, but Plus has active.
    # We rely on passive check: if a server fails, Nginx tries the next.
}

server {
    listen 80;
    server_name quickstay.local;

    location / {
        proxy_pass http://backend_pool;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        
        # Session Persistence (Cookie-based Simulation)
        # Nginx Open Source doesn't support 'sticky cookie' natively without modules 
        # or ip_hash. We use ip_hash as closest approximation for standard install
        # or just standard RR/LeastConn for stateless demo.
    }
}
EOF

# Note: Ideally assign VIP to interface: ip addr add $VIP/24 dev eth0

echo "Enabling LB Config..."
ln -sf /etc/nginx/sites-available/quickstay_lb /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

echo "Restarting Nginx..."
systemctl restart nginx

echo "Load Balancer Configured. Traffic to this host (acting as VIP) will balance to $WEB1 and $WEB2."
