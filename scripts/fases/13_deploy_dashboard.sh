#!/bin/bash
# 13_deploy_dashboard.sh
# Phase 6: Hypervisor Dashboard Deployment (Docker Stack)
# Usage: sudo ./13_deploy_dashboard.sh

DASHBOARD_DIR="/opt/quickstay-dashboard"

echo "Installing Docker and Docker Compose..."
apt-get update
apt-get install -y docker.io docker-compose

echo "Preparing Dashboard Directory: $DASHBOARD_DIR"
mkdir -p $DASHBOARD_DIR
mkdir -p $DASHBOARD_DIR/grafana_data
mkdir -p $DASHBOARD_DIR/guacamole_home

# Generate Docker Compose File
cat <<EOF > $DASHBOARD_DIR/docker-compose.yml
version: '3'
services:
  # 1. Grafana (Visual Intelligence)
  grafana:
    image: grafana/grafana:latest
    container_name: quickstay-grafana
    ports:
      - "3000:3000"
    volumes:
      - ./grafana_data:/var/lib/grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    networks:
      - quickstay-net

  # 2. Guacamole (Remote Access)
  guacd:
    image: guacamole/guacd
    networks:
      - quickstay-net
  
  guacamole:
    image: guacamole/guacamole
    container_name: quickstay-guacamole
    ports:
      - "8080:8080"
    environment:
      - GUACD_HOSTNAME=guacd
      - MYSQL_HOSTNAME=172.16.20.20 # Using Infrastructure DB if possible, or local sqlite for demo
      - MYSQL_DATABASE=guacamole_db
      - MYSQL_USER=guacamole_user
      - MYSQL_PASSWORD=guacamole_pass
    depends_on:
      - guacd
    networks:
      - quickstay-net

  # 3. Control API (Chaos & Mgmt)
  control-api:
    build: ./api
    container_name: quickstay-api
    ports:
      - "8000:8000"
    volumes:
      - /root/.ssh:/root/.ssh:ro # Mount host SSH keys to execute commands on infra
    networks:
      - quickstay-net

networks:
  quickstay-net:
    driver: bridge

EOF

# Generate Control API Stub
mkdir -p $DASHBOARD_DIR/api
cat <<EOF > $DASHBOARD_DIR/api/Dockerfile
FROM python:3.9-slim
WORKDIR /app
COPY . .
RUN pip install fastapi uvicorn fabric
CMD ["uvicorn", "main:app", "--host", "0.0.0.0"]
EOF

cat <<EOF > $DASHBOARD_DIR/api/main.py
from fastapi import FastAPI
import subprocess

app = FastAPI()

@app.post("/api/chaos/network_down")
def network_down(target_ip: str):
    # Simulation: Execute SSH command to drop interface
    # In real scenario: subprocess.run(["ssh", target_ip, "ifconfig eth0 down"])
    return {"status": "success", "message": f"Network interface on {target_ip} disabled (SIMULATED)"}

@app.post("/api/server/restart")
def server_restart(service_name: str, target_ip: str):
    # Simulation
    return {"status": "success", "message": f"Service {service_name} on {target_ip} restarting..."}
EOF

echo "Building and Deploying Stack..."
cd $DASHBOARD_DIR
docker-compose up -d --build

echo "Hypervisor Dashboard Deployed."
echo " - Grafana: http://localhost:3000 (admin/admin)"
echo " - Guacamole: http://localhost:8080 (default: guacadmin/guacadmin)"
echo " - API: http://localhost:8000/docs"
