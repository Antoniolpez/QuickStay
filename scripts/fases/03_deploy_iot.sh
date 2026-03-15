#!/bin/bash
# 03_deploy_iot.sh
# Installs Mosquitto MQTT Broker and Python dependencies for simulation
# Usage: sudo ./03_deploy_iot.sh

echo "Installing Mosquitto MQTT Broker..."
apt-get update
apt-get install -y mosquitto mosquitto-clients python3-pip

echo "Configuring Mosquitto to allow remote access (Development Mode)..."
# Create a config that allows anonymous access for easier simulation testing
cat <<EOF > /etc/mosquitto/conf.d/quickstay.conf
listener 1883
allow_anonymous true
EOF

echo "Restarting Mosquitto..."
systemctl restart mosquitto

echo "Installing Python dependencies for simulator..."
pip3 install paho-mqtt

echo "IoT Infrastructure Deployment Complete."
echo "You can now run the simulator using: python3 ../../iot/iot_client.py"
