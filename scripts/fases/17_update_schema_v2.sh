#!/bin/bash
# 17_update_schema_v2.sh
# Phase 7: App Refactoring - Database Schema Upgrade
# Usage: sudo ./17_update_schema_v2.sh

DB_USER="quickstay_app"
DB_PASS="app_password"
DB_NAME="humhouse"
DB_HOST="172.16.20.20"

# Note: Check if running on DB server or remotely. Assuming execution on DB Server for simplicity or client installed.
if [ "$(hostname -I | cut -d' ' -f1)" != "$DB_HOST" ]; then
    echo "Warning: Executing remotely. Ensure mysql-client is installed and has access."
    # For simulation, we assume local execution or access via root
    DB_USER="root"
    DB_PASS="root" # Default simulation pass
fi

echo "Applying Schema Updates..."

cat <<EOF > /tmp/update_v2.sql
USE $DB_NAME;

-- 1. Refactor Images (Add URL columns)
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS foto_url VARCHAR(255) DEFAULT '/images/default_user.png';
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS foto_url VARCHAR(255) DEFAULT '/images/default_prop.png';
ALTER TABLE fotos_propiedad ADD COLUMN IF NOT EXISTS url VARCHAR(255);

-- 2. Refactor Chat (Centralized Messages Table)
CREATE TABLE IF NOT EXISTS mensajes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    remitente VARCHAR(50) NOT NULL,
    destinatario VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    leido BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (remitente) REFERENCES usuario(nom_usuario),
    FOREIGN KEY (destinatario) REFERENCES usuario(nom_usuario)
);

-- Index for faster chat history retrieval
CREATE INDEX idx_chat ON mensajes(remitente, destinatario);

-- 3. Audit Log (For Dashboard Security View)
CREATE TABLE IF NOT EXISTS access_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50),
    accion VARCHAR(100),
    ip_origen VARCHAR(45),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);
EOF

mysql -u$DB_USER -p$DB_PASS < /tmp/update_v2.sql

echo "✅ Database Schema Upgraded to V2 (Chat & URLs)."
