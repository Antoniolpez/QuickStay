#!/bin/bash
# 15_optimize_perf.sh
# Phase 4 Guide 9: Performance Tuning & Optimization
# Usage: sudo ./15_optimize_perf.sh

echo "Applying Kernel Network Tuning (Sysctl)..."
cat <<EOF >> /etc/sysctl.conf
# QuickStay High Performance Tuning
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 8192
net.ipv4.tcp_tw_reuse = 1
net.ipv4.ip_local_port_range = 1024 65535
vm.swappiness = 10
EOF
sysctl -p

if dpkg -l | grep -q apache2; then
    echo "Optimizing Apache Web Server..."
    # Switching to Event MPM for high concurrency
    a2dismod mpm_prefork
    a2dismod mpm_worker
    a2enmod mpm_event
    
    # Tuning KeepAlive
    sed -i 's/KeepAliveTimeout 5/KeepAliveTimeout 2/' /etc/apache2/apache2.conf
    
    # Event MPM Config (Simulated)
    echo "
    <IfModule mpm_event_module>
        StartServers 3
        MinSpareThreads 75
        MaxSpareThreads 250
        ThreadsPerChild 25
        MaxRequestWorkers 400
        MaxConnectionsPerChild 0
    </IfModule>" > /etc/apache2/conf-available/tuning.conf
    a2enconf tuning
    
    systemctl restart apache2
    echo "✅ Apache Optimized."
fi

if dpkg -l | grep -q mysql-server; then
    echo "Optimizing MySQL Database..."
    # Tuning InnoDB Buffer Pool (Critical for DB performance)
    # Assuming 4GB+ RAM for DB Server, setting modest 1G buffer for demo
    echo "[mysqld]
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2 # Perf > Durability for transient data
query_cache_type = 0 # Deprecated in MySQL 8 but ensuring it's off
query_cache_size = 0
max_connections = 500" > /etc/mysql/mysql.conf.d/tuning.cnf
    
    systemctl restart mysql
    echo "✅ MySQL Optimized."
fi

echo "Performance Tuning Applied."
