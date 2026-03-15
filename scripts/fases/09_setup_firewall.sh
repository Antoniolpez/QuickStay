#!/bin/bash
# 09_setup_firewall.sh
# Phase 2 Guide 3: Router/Firewall Config with iptables
# Usage: sudo ./09_setup_firewall.sh

echo "Enabling IP Forwarding..."
sysctl -w net.ipv4.ip_forward=1
echo "net.ipv4.ip_forward=1" >> /etc/sysctl.conf

echo "Flushing existing rules..."
iptables -F
iptables -t nat -F
iptables -X

# Default Policies
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT ACCEPT

# 1. Local & State Related
iptables -A INPUT -i lo -j ACCEPT
iptables -A INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT
iptables -A FORWARD -m state --state RELATED,ESTABLISHED -j ACCEPT

# 2. NAT Configuration (Phase 2, Sec 1.2)
# DNAT: Web Server Publication (WAN -> DMZ Web)
iptables -t nat -A PREROUTING -i enp0s3 -p tcp --dport 80 -j DNAT --to-destination 172.16.10.10:80
iptables -t nat -A PREROUTING -i enp0s3 -p tcp --dport 443 -j DNAT --to-destination 172.16.10.10:443

# SNAT: Internal Access to Internet (All LAN -> WAN Masquerade)
iptables -t nat -A POSTROUTING -s 172.16.0.0/16 -o enp0s3 -j MASQUERADE

# 3. Inter-VLAN Rules (FORWARD Chain - Phase 2, Sec 3)

# R1: WAN to DMZ (Web Access)
iptables -A FORWARD -i enp0s3 -o enp0s8 -p tcp -d 172.16.10.10 --dport 80 -j ACCEPT
iptables -A FORWARD -i enp0s3 -o enp0s8 -p tcp -d 172.16.10.10 --dport 443 -j ACCEPT

# R2: DMZ (10) to App (20)
iptables -A FORWARD -s 172.16.10.0/24 -d 172.16.20.0/24 -p tcp --dport 8080 -j ACCEPT

# R3: App (20) to DB (20) - (Intra-VLAN usually switch handled, but if routed:)
iptables -A FORWARD -s 172.16.20.10 -d 172.16.20.20 -p tcp --dport 3306 -j ACCEPT
iptables -A FORWARD -s 172.16.20.11 -d 172.16.20.20 -p tcp --dport 3306 -j ACCEPT

# R4: Services (20) to Management (30) - DNS/LDAP
iptables -A FORWARD -s 172.16.20.0/24 -d 172.16.30.0/24 -p udp --dport 53 -j ACCEPT
iptables -A FORWARD -s 172.16.20.0/24 -d 172.16.30.0/24 -p tcp --dport 389 -j ACCEPT

# R5: IoT (40) to Management (30) - DNS & Zabbix
iptables -A FORWARD -s 172.16.40.0/23 -d 172.16.30.0/24 -p udp --dport 53 -j ACCEPT
iptables -A FORWARD -s 172.16.40.0/23 -d 172.16.30.0/24 -p tcp --dport 10051 -j ACCEPT # Zabbix Trap

# R6: Management (30) to All - SSH
iptables -A FORWARD -s 172.16.30.0/24 -d 172.16.0.0/16 -p tcp --dport 22 -j ACCEPT

# R7: LAN to Internet (Outbound)
iptables -A FORWARD -s 172.16.0.0/16 -o enp0s3 -j ACCEPT

# R8: ICMP (Ping)
iptables -A FORWARD -p icmp -j ACCEPT

echo "Firewall Rules Applied."
# Persist (assuming iptables-persistent is used/installed, otherwise manual save)
iptables-save > /etc/iptables/rules.v4 2>/dev/null || echo "Warning: install iptables-persistent to save across reboots."
