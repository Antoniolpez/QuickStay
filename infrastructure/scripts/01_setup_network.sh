#!/bin/bash
# 01_setup_network.sh
# Configures Static IP address for QuickStay Servers (Ubuntu/Debian)
# Usage: sudo ./01_setup_network.sh <INTERFACE> <IP_ADDRESS> <GATEWAY>
# Example: sudo ./01_setup_network.sh eth0 172.16.20.20 172.16.20.1

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <INTERFACE> <IP_ADDRESS> <GATEWAY>"
    echo "Example: $0 eth0 172.16.20.20 172.16.20.1"
    exit 1
fi

INTERFACE=$1
IP_ADDRESS=$2
GATEWAY=$3
DNS_SERVER="172.16.30.10" # AD-DC DNS
NETMASK="24"

echo "Configuring network for $INTERFACE..."

# Detect Netplan (Ubuntu 18.04+)
if command -v netplan &> /dev/null; then
    echo "Netplan detected. Creating configuration..."
    cat <<EOF > /etc/netplan/99-quickstay-static.yaml
network:
  version: 2
  renderer: networkd
  ethernets:
    $INTERFACE:
      dhcp4: no
      addresses:
        - $IP_ADDRESS/$NETMASK
      routes:
        - to: default
          via: $GATEWAY
      nameservers:
        addresses: [$DNS_SERVER, 8.8.8.8]
EOF
    chmod 600 /etc/netplan/99-quickstay-static.yaml
    netplan apply
    echo "Netplan configuration applied."
else
    # Fallback to /etc/network/interfaces (Debian/Older Ubuntu)
    echo "Legacy networking detected. Updating /etc/network/interfaces..."
    
    # Backup
    cp /etc/network/interfaces /etc/network/interfaces.bak
    
    cat <<EOF >> /etc/network/interfaces

# QuickStay Static Config
auto $INTERFACE
iface $INTERFACE inet static
    address $IP_ADDRESS
    netmask 255.255.255.0
    gateway $GATEWAY
    dns-nameservers $DNS_SERVER 8.8.8.8
EOF
    echo "Configuration appended to /etc/network/interfaces. Restarting networking..."
    systemctl restart networking
fi

echo "Network configuration complete. Current IP:"
ip addr show $INTERFACE
