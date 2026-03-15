# Componentes Retirados y Decisiones de Limpieza

Fecha de actualizacion: 2026-03-08

## 1) Servicio retirado: `wireguard-vpn` (contenedor dedicado)

Estado:
- Retirado del `docker-compose.yml` principal.
- WireGuard pasa a ejecutarse en `router-fw` (mismo endpoint `UDP/51820`).

Motivos:
- Evitar conflictos de ownership de `51820/udp` entre contenedores.
- Eliminar rutas/NAT legacy (DNAT al contenedor VPN) que complicaban el troubleshooting.
- Reducir complejidad operativa y tiempo de despliegue.

Impacto:
- Menos consumo de recursos (un contenedor menos).
- Menor superficie de fallo en la ruta de ingreso VPN.
- Arquitectura mas coherente: todo el edge (80/443/51820) en `router-fw`.

## 2) Configuracion retirada en `router-fw`

Se elimino la ruta legacy:
- DNAT de `51820` hacia `172.16.50.10`.
- Regla forward "Allow WAN to VPN" dependiente de contenedor separado.
- Masquerade "Masquerade WAN to WG" para trafico DNAT legacy.

Estado actual:
- `router-fw` levanta `wg0` localmente desde `/etc/wireguard/wg0.conf`.
- `router-fw` publica `51820:51820/udp`.

## 3) Como reactivar el modo legacy (solo si es estrictamente necesario)

No recomendado. Si se necesita por compatibilidad:
1. Restaurar bloque `wireguard-vpn` en `docker-compose.yml`.
2. Devolver `51820/udp` al servicio legacy o definir ownership unico.
3. Restaurar reglas DNAT/forward legacy en `entrypoint.sh`.

## 4) Verificacion rapida del estado actual

```bash
docker ps --format '{{.Names}} {{.Ports}}' | grep -E 'quickstay-(router-fw|vpn)'
docker exec quickstay-router-fw wg show
docker exec quickstay-router-fw nft list chain inet quickstay input | grep 51820
```
