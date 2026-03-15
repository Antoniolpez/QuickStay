#!/bin/bash
# 02_deploy_db.sh
# Installs MySQL Server and initializes QuickStay Database
# Usage: sudo ./02_deploy_db.sh

DB_ROOT_PASS="root" # CAUTION: Hardcoded for dev/demo environment
DB_USER="ProyectoFinal"
DB_PASS="root"
DB_NAME="humhouse"
SCHEMA_FILE="../db/init.sql"

echo "Installing MySQL Server..."
apt-get update
# Non-interactive install setting root password
echo "mysql-server mysql-server/root_password password $DB_ROOT_PASS" | debconf-set-selections
echo "mysql-server mysql-server/root_password_again password $DB_ROOT_PASS" | debconf-set-selections
apt-get install -y mysql-server

echo "Configuring MySQL to listen on all interfaces (0.0.0.0)..."
sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
systemctl restart mysql

echo "Waiting for MySQL to start..."
sleep 5

echo "Creating Database and Users (Compliant with Phase 3 Guide)..."
mysql -u root -p"$DB_ROOT_PASS" <<EOF
CREATE DATABASE IF NOT EXISTS $DB_NAME;

-- App User (Redundant App Servers)
CREATE USER IF NOT EXISTS 'quickstay_app'@'172.16.20.10' IDENTIFIED BY 'app_password';
CREATE USER IF NOT EXISTS 'quickstay_app'@'172.16.20.11' IDENTIFIED BY 'app_password'; -- Redundancy
GRANT SELECT, INSERT, UPDATE, DELETE ON $DB_NAME.* TO 'quickstay_app'@'172.16.20.10';
GRANT SELECT, INSERT, UPDATE, DELETE ON $DB_NAME.* TO 'quickstay_app'@'172.16.20.11';

-- Monitor User (Zabbix Server)
CREATE USER IF NOT EXISTS 'monitor'@'172.16.30.20' IDENTIFIED BY 'monitor_password';
GRANT REPLICATION CLIENT, PROCESS ON *.* TO 'monitor'@'172.16.30.20';
GRANT SELECT ON performance_schema.* TO 'monitor'@'172.16.30.20';

FLUSH PRIVILEGES;
EOF

echo "Importing Schema from $SCHEMA_FILE..."
if [ -f "$SCHEMA_FILE" ]; then
    mysql -u root -p"$DB_ROOT_PASS" $DB_NAME < "$SCHEMA_FILE"
    echo "Schema imported successfully."
else
    echo "ERROR: Schema file $SCHEMA_FILE not found!"
    exit 1
fi

echo "Database Deployment Complete!"
