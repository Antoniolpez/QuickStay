#!/bin/bash
# 14_deploy_security.sh
# Phase 3 Guide 7: Full Security Implementation (Wazuh Manager & Agents)
# Usage: sudo ./14_deploy_security.sh

CURRENT_IP=$(hostname -I | cut -d' ' -f1)
SECURITY_SERVER_IP="172.16.30.30"

echo "Detected IP: $CURRENT_IP"

if [[ "$CURRENT_IP" == "$SECURITY_SERVER_IP" ]]; then
    # === INSTALL WAZUH MANAGER (SERVER) ===
    echo "This is the Security Server. Installing Wazuh Manager..."
    
    # Using Docker for robust Manager deployment (Standard architecture)
    apt-get update
    apt-get install -y docker.io docker-compose curl
    
    mkdir -p /opt/wazuh-docker
    cd /opt/wazuh-docker
    
    echo "Downloading Wazuh Docker Compose..."
    # Cloning official single-node template
    curl -sO https://packages.wazuh.com/4.7/docker-compose.yml
    
    echo "Starting Wazuh Manager/Indexer/Dashboard..."
    docker-compose up -d
    
    echo "✅ Wazuh Manager Deployed."
    echo "Access Dashboard at: https://$SECURITY_SERVER_IP (Default user: admin/SecretPassword)"

else
    # === INSTALL WAZUH AGENT (CLIENT) ===
    echo "This is a Client Node. Installing Wazuh Agent..."
    
    curl -s https://packages.wazuh.com/key/GPG-KEY-WAZUH | gpg --no-default-keyring --keyring gnupg-ring:/usr/share/keyrings/wazuh.gpg --import && chmod 644 /usr/share/keyrings/wazuh.gpg
    echo "deb [signed-by=/usr/share/keyrings/wazuh.gpg] https://packages.wazuh.com/4.x/apt/ stable main" | tee -a /etc/apt/sources.list.d/wazuh.list
    
    apt-get update
    apt-get install -y wazuh-agent

    echo "Configuring Agent to connect to $SECURITY_SERVER_IP..."
    sed -i "s/MANAGER_IP/$SECURITY_SERVER_IP/" /var/ossec/etc/ossec.conf
    # Fallback sed if MANAGER_IP placeholder isn't there
    sed -i "s/<address>127.0.0.1<\/address>/<address>$SECURITY_SERVER_IP<\/address>/" /var/ossec/etc/ossec.conf

    echo "Enabling & Starting Agent..."
    systemctl daemon-reload
    systemctl enable wazuh-agent
    systemctl start wazuh-agent
    
    echo "✅ Wazuh Agent Installed and Connected."
fi
