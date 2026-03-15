#!/bin/bash
# 05_simulate_backup.sh
# Simulates Phase 4 Backup Requirements
# 1. Creates Repo Directories
# 2. Generates MySQL Pre/Post Scripts for Consistent Backup

mkdir -p /mnt/backup_local
mkdir -p /mnt/nas_backups

echo "Backup Repositories Created:"
echo " - Local: /mnt/backup_local"
echo " - NAS:   /mnt/nas_backups"

# Create Script Directory
mkdir -p /etc/veeam/scripts

# MySQL Pre-Freeze Script (Lock Tables)
cat <<EOF > /etc/veeam/scripts/mysql_pre_backup.sh
#!/bin/bash
# Phase 4.2.3: Lock Tables for Consistent Backup
mysql -u root -proot -e "FLUSH TABLES WITH READ LOCK;"
echo "MySQL Tables Locked for Backup at \$(date)" >> /var/log/veeam_mysql.log
EOF
chmod +x /etc/veeam/scripts/mysql_pre_backup.sh

# MySQL Post-Thaw Script (Unlock Tables)
cat <<EOF > /etc/veeam/scripts/mysql_post_backup.sh
#!/bin/bash
# Phase 4.2.3: Unlock Tables after Snapshot
mysql -u root -proot -e "UNLOCK TABLES;"
echo "MySQL Tables Unlocked at \$(date)" >> /var/log/veeam_mysql.log
EOF
chmod +x /etc/veeam/scripts/mysql_post_backup.sh

echo "MySQL Pre/Post Scripts generated in /etc/veeam/scripts/"
