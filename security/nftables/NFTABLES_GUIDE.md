# nftables - Guía de Referencia

## Introducción a nftables

**nftables** es el successor moderno de iptables. Ofrece una sintaxis más coherente, mejor rendimiento y mayor flexibilidad. En QuickStay, reemplaza a iptables para el firewall del router.

## Ventajas de nftables sobre iptables

✓ Sintaxis unificada (no hay múltiples herramientas como iptables, ip6tables, arptables)
✓ Mejor rendimiento (menos overhead)
✓ Reglas más claras y estructuradas
✓ Transacciones atómicas (cambios se aplican de una sola vez)
✓ Mejor manejo de memoria

## Conceptos Base

### Tabla (Table)
Contiene toda la lógica del firewall. En QuickStay: `inet quickstay`

### Cadena (Chain)
Ordena las reglas. Tipos:
- `filter`: Filtrado de paquetes (INPUT, OUTPUT, FORWARD)
- `nat`: Traducción de direcciones
- `mangle`: Modificación de paquetes

### Regla (Rule)
Condición + acción. Ejemplo:
```nft
nft add rule inet quickstay forward iifname "eth1" oifname "eth0" accept
```

### Hook
Punto de enganche en el pipeline de red:
- `input`: Paquetes destino a la máquina local
- `output`: Paquetes originados localmente
- `forward`: Paquetes que atraviesan la máquina
- `prerouting`: Antes de tomar decisión de enrutamiento
- `postrouting`: Después de tomar decisión de enrutamiento

## Comandos Comunes en QuickStay

### Ver la configuración completa
```bash
docker-compose exec router-fw nft list ruleset
```

### Ver solo la tabla quickstay
```bash
docker-compose exec router-fw nft list table inet quickstay
```

### Ver solo las reglas de una cadena (forward)
```bash
docker-compose exec router-fw nft list chain inet quickstay forward
```

### Ver estadísticas de reglas
```bash
docker-compose exec router-fw nft list chain inet quickstay forward -a
```

### Agregar una nueva regla
```bash
docker-compose exec router-fw nft add rule inet quickstay forward iifname "eth1" oifname "eth2" accept
```

### Insertar regla al inicio
```bash
docker-compose exec router-fw nft insert rule inet quickstay forward iifname "eth1" oifname "eth2" accept
```

### Eliminar una regla específica
```bash
docker-compose exec router-fw nft delete rule inet quickstay forward handle <HANDLE>
```

Para obtener el handle:
```bash
docker-compose exec router-fw nft --handle list chain inet quickstay forward
```

### Eliminar toda una cadena
```bash
docker-compose exec router-fw nft flush chain inet quickstay forward
```

### Resetear completamente el firewall
```bash
docker-compose exec router-fw nft flush ruleset
```

## Estructura de Reglas en QuickStay

### Formato General
```
nft add rule [table] [chain] [condición] [acción]
```

### Ejemplos Prácticos

#### 1. Permitir tráfico desde una VLAN a otra por puerto específico
```nft
nft add rule inet quickstay forward iifname "eth1" oifname "eth2" \
    ip saddr 172.16.10.0/24 ip daddr 172.16.20.0/24 \
    tcp dport 3306 accept comment "DMZ to App MySQL"
```

#### 2. Bloquear tráfico específico
```nft
nft add rule inet quickstay forward iifname "eth1" oifname "eth3" drop
```

#### 3. Permitir con contador para estadísticas
```nft
nft add rule inet quickstay forward iifname "eth1" oifname "eth2" \
    counter accept comment "Count DMZ traffic"
```

#### 4. MASQUERADE (NAT) para salida a Internet
```nft
nft add rule inet quickstay postrouting oifname "eth0" \
    ip saddr 172.16.0.0/16 counter masquerade
```

#### 5. Permitir basado en estado de conexión
```nft
nft add rule inet quickstay forward ct state established,related accept
```

## Operadores Comunes

| Operador | Significado | Ejemplo |
|----------|-------------|---------|
| `iifname` | Interface entrante | `iifname "eth0"` |
| `oifname` | Interface saliente | `oifname "eth1"` |
| `ip saddr` | IP origen | `ip saddr 192.168.1.0/24` |
| `ip daddr` | IP destino | `ip daddr 10.0.0.0/8` |
| `tcp dport` | Puerto TCP destino | `tcp dport 22` |
| `tcp sport` | Puerto TCP origen | `tcp sport 1024-65535` |
| `ct state` | Estado conexión | `ct state established,related` |

## Conjuntos (Sets)

### Definir un conjunto de puertos
```nft
nft add set inet quickstay web_ports { type inet_service; }
nft add element inet quickstay web_ports { 80, 443 }
```

### Usar el conjunto
```nft
nft add rule inet quickstay forward tcp dport @web_ports accept
```

## Debugging y Troubleshooting

### 1. Ver logs de paquetes rechazados
```bash
docker-compose logs router-fw | grep DROP
```

### 2. Verificar estado de interfaces
```bash
docker-compose exec router-fw ip addr show
docker-compose exec router-fw ip link show
```

### 3. Probar conectividad entre contenedores
```bash
docker-compose exec router-fw ping 172.16.10.10  # DMZ
docker-compose exec router-fw ping 172.16.20.10  # App
```

### 4. Verificar IP Forwarding habilitado
```bash
docker-compose exec router-fw sysctl net.ipv4.ip_forward
```

### 5. Ver rutas de red
```bash
docker-compose exec router-fw ip route show
```

## Matriz de Acceso QuickStay

Current rules in `infrastructure/docker/router-fw/entrypoint.sh`:

### VLAN 10 (DMZ)
- ✓ A Internet: HTTP/HTTPS (80, 443)
- ✓ A App (20): Puertos 8080, 1234, 3306
- ✓ Dentro de DMZ: Tráfico interno

### VLAN 20 (App)
- ✓ A Internet: Todo
- ✓ A Management (30): SSH, LDAP, DNS
- ✓ A IoT (40): MQTT (1883, 8883)

### VLAN 30 (Management)
- ✓ A Internet: Todo
- ✓ A App (20): Monitoreo (10051, 9200, 9300)
- ✓ A IoT (40): Todo
- ✓ A DMZ (10): Admin (22, 445)

### VLAN 40 (IoT)
- ✓ A Management (30): Todo
- ✓ A Internet: HTTP/HTTPS (80, 443)
- ✓ Dentro de IoT: Tráfico interno

### VLAN 50 (VPN)
- ✓ A todas las VLANs: Acceso completo (admin)
- ✓ Tráfico de retorno desde todas las VLANs

## Performance Tuning

### 1. Usar conjuntos en lugar de múltiples reglas
```nft
# ❌ Ineficiente
nft add rule inet quickstay forward tcp dport 80 accept
nft add rule inet quickstay forward tcp dport 443 accept
nft add rule inet quickstay forward tcp dport 8080 accept

# ✓ Eficiente
nft add set inet quickstay web_ports { type inet_service; }
nft add element inet quickstay web_ports { 80, 443, 8080 }
nft add rule inet quickstay forward tcp dport @web_ports accept
```

### 2. Agregar comentarios para claridad
```nft
nft add rule inet quickstay forward iifname "eth1" oifname "eth2" \
    tcp dport 3306 accept comment "DMZ to App MySQL"
```

### 3. Usar counters solo cuando sea necesario
```nft
nft add rule inet quickstay forward counter accept  # Overhead bajo
```

## Recursos Adicionales

- Documentación oficial: https://wiki.nftables.org/
- Guía de migración desde iptables: https://wiki.nftables.org/wiki-nftables/index.php/Moving_from_iptables_to_nftables
- Man page: `man nft`

## Referencias del Proyecto

Archivo de entrypoint: `infrastructure/docker/router-fw/entrypoint.sh`
Dockerfile del router: `infrastructure/docker/router-fw/Dockerfile`
Configuración general: `infrastructure/config/nftables/`
