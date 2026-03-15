#!/bin/bash
# 04_deploy_monitoring.sh
# Installs Zabbix Agent and configures it for QuickStay
# Usage: sudo ./04_deploy_monitoring.sh <ZABBIX_SERVER_IP> <HOSTNAME>

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <ZABBIX_SERVER_IP> <HOSTNAME>"
    echo "Example: $0 172.16.30.20 webserver1"
    exit 1
fi

SERVER_IP=$1
HOST_NAME=$2

echo "Installing Zabbix Agent..."
apt-get update
apt-get install -y zabbix-agent

echo "Configuring Zabbix Agent..."
# Backup original config
cp /etc/zabbix/zabbix_agentd.conf /etc/zabbix/zabbix_agentd.conf.bak

# Update config
sed -i "s/^Server=127.0.0.1/Server=$SERVER_IP/" /etc/zabbix/zabbix_agentd.conf
sed -i "s/^ServerActive=127.0.0.1/ServerActive=$SERVER_IP/" /etc/zabbix/zabbix_agentd.conf
sed -i "s/^Hostname=Zabbix server/Hostname=$HOST_NAME/" /etc/zabbix/zabbix_agentd.conf

echo "Restarting Zabbix Agent..."
systemctl restart zabbix-agent
systemctl enable zabbix-agent

echo "Zabbix Agent Deployed."
echo "Status:"
systemctl status zabbix-agent --no-pager
