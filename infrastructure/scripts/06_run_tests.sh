#!/bin/bash
# 06_run_tests.sh
# Phase 5: Automated Verification Script
# checks Connectivity, Ports, and Database Access

TARGET_IP="172.16.20.10" # App Server
DB_IP="172.16.20.20"     # DB Server

echo "=== QuickStay Automated Tests (Phase 5) ==="

# 1. Connectivity Check
echo "[TEST] Connectivity to App Server ($TARGET_IP)..."
if ping -c 1 $TARGET_IP &> /dev/null; then
    echo "  [PASS] App Server Reachable"
else
    echo "  [FAIL] App Server Unreachable"
fi

# 2. Port Check (Simulating Nmap)
echo "[TEST] Port 8080 (App) on $TARGET_IP..."
timeout 1 bash -c "cat < /dev/null > /dev/tcp/$TARGET_IP/8080" 2>/dev/null
if [ $? -eq 0 ]; then
   echo "  [PASS] Port 8080 Open"
else
   echo "  [FAIL] Port 8080 Closed (Expected if App not running)"
fi

# 3. Database User Check (Security Compliance)
echo "[TEST] Verifying specific DB users exist..."
# This runs locally assuming we are on the DB server or have client installed
if command -v mysql &> /dev/null; then
    USER_CHECK=$(mysql -u root -proot -e "SELECT User, Host FROM mysql.user WHERE User='quickstay_app';" 2>/dev/null | grep "172.16.20.10")
    if [ ! -z "$USER_CHECK" ]; then
        echo "  [PASS] User 'quickstay_app'@'172.16.20.10' found."
    else
        echo "  [FAIL] User 'quickstay_app'@'172.16.20.10' NOT found."
    fi
else
    echo "  [SKIP] MySQL client not found locally."
fi

# 4. Backup Script Existence
echo "[TEST] Verifying Backup Scripts..."
if [ -f "/etc/veeam/scripts/mysql_pre_backup.sh" ]; then
    echo "  [PASS] MySQL Pre-Backup Script exists."
else
    echo "  [FAIL] MySQL Pre-Backup Script missing."
fi

echo "=== Test Run Complete ==="
