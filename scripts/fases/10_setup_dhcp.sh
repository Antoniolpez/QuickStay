#!/bin/bash
# 10_setup_dhcp.sh
# Phase 2 Guide 4: DHCP Server for VLANs 40 (IoT) and 50 (VPN)
# Usage: sudo ./10_setup_dhcp.sh

echo "Installing DHCP Server..."
apt-get update
apt-get install -y isc-dhcp-server

echo "Configuring DHCP Scopes..."
cat <<EOF > /etc/dhcp/dhcpd.conf
default-lease-time 600;
max-lease-time 7200;
authoritative;

# Global Options
option domain-name "quickstay.local";
option domain-name-servers 172.16.30.10, 172.16.30.11;

# VLAN 40: IoT Devices
subnet 172.16.40.0 netmask 255.255.254.0 {
    range 172.16.40.100 172.16.41.250;
    option routers 172.16.40.1;
}

# VLAN 50: Admin VPN
subnet 172.16.50.0 netmask 255.255.255.192 {
    range 172.16.50.10 172.16.50.60;
    option routers 172.16.50.1;
}
EOF

echo "Setting Interface (Adjust INTERFACESv4 in /etc/default/isc-dhcp-server manually to match your VLAN interface, e.g., eth0 or vlan40)"

echo "Restarting DHCP Server..."
systemctl restart isc-dhcp-server 2>/dev/null || echo "Note: DHCP server might fail to start if interfaces are not yet configured. Check /etc/default/isc-dhcp-server"

echo "DHCP Server Configured."
