#!/usr/bin/env python3
"""
QuickStay - Zabbix Auto-Provisioning Script

- Lee la topología desde Documentación/dashboard-v2/backend/config.yaml
- Obtiene la URL de la API Zabbix desde ese mismo archivo
- Lee la contraseña de admin de Zabbix desde secrets.env
- Crea automáticamente los hosts para cada servidor definido en config.yaml
- Asocia la plantilla "Template Module ICMP Ping" para tener ping/uptime
- Si existe un host de hipervisor (<hostname>-hypervisor), le asocia
  la plantilla "Template OS Linux by Zabbix agent".

Este script está pensado para ejecutarse desde la raíz del proyecto
(ProyectoFinal/) y se llama automáticamente desde deploy-complete.sh.

No hace fallar el despliegue si la API de Zabbix no responde; simplemente
escribe un mensaje y termina con código de salida 0.
"""

import json
import os
import sys
import time
from pathlib import Path
from typing import Any, Dict, List
import urllib.request
import urllib.error

try:
    import yaml  # type: ignore
except ImportError:
    print("[ZABBIX-PROVISION] PyYAML no está instalado; omitiendo auto-configuración.")
    sys.exit(0)

BASE_DIR = Path(__file__).resolve().parent.parent
CONFIG_PATH = BASE_DIR / "Documentación" / "dashboard-v2" / "backend" / "config.yaml"
SECRETS_PATH = BASE_DIR / "secrets.env"


def load_yaml_config() -> Dict[str, Any]:
    if not CONFIG_PATH.is_file():
        print(f"[ZABBIX-PROVISION] No se encuentra config.yaml en {CONFIG_PATH}")
        sys.exit(0)
    with CONFIG_PATH.open("r", encoding="utf-8") as f:
        return yaml.safe_load(f)


def load_secrets() -> Dict[str, str]:
    secrets: Dict[str, str] = {}
    if not SECRETS_PATH.is_file():
        print(f"[ZABBIX-PROVISION] No se encuentra secrets.env en {SECRETS_PATH}")
        return secrets
    with SECRETS_PATH.open("r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                continue
            key, value = line.split("=", 1)
            secrets[key.strip()] = value.strip()
    return secrets


def zbx_request(url: str, method: str, params: Dict[str, Any] | None, auth: str | None, req_id: int = 1, retries: int = 10) -> Any:
    """
    Realizar una solicitud a la API de Zabbix con reintentos automáticos.
    
    :param url: URL de la API de Zabbix
    :param method: Método de la API
    :param params: Parámetros del método
    :param auth: Token de autenticación (puede ser None para login)
    :param req_id: ID de la solicitud (para debugging)
    :param retries: Número de intentos antes de fallar
    :return: Resultado de la API
    :raises RuntimeError: Si falla después de todos los reintentos
    """
    payload: Dict[str, Any] = {
        "jsonrpc": "2.0",
        "method": method,
        "params": params or {},
        "id": req_id,
        "auth": auth,
    }
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/json-rpc"})

    last_error = None
    for attempt in range(1, retries + 1):
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                resp_data = resp.read().decode("utf-8")
            break
        except (urllib.error.URLError, Exception) as e:
            last_error = e
            if attempt < retries:
                wait_time = 2 ** (attempt - 1)  # Backoff exponencial: 1s, 2s, 4s, 8s, 16s
                print(f"[ZABBIX-PROVISION] Intento {attempt}/{retries} falló, reintentando en {wait_time}s...", file=sys.stderr)
                time.sleep(wait_time)
            else:
                raise RuntimeError(f"No se puede conectar a la API de Zabbix tras {retries} intentos: {last_error}")

    try:
        parsed = json.loads(resp_data)
    except json.JSONDecodeError as e:
        raise RuntimeError(f"Respuesta JSON inválida de Zabbix: {e}")

    if "error" in parsed:
        raise RuntimeError(f"Error Zabbix API: {parsed['error']}")

    return parsed.get("result")


def ensure_hostgroup(zbx_url: str, auth: str, name: str) -> str:
    result = zbx_request(
        zbx_url,
        "hostgroup.get",
        {"filter": {"name": [name]}},
        auth,
    )
    if result:
        return result[0]["groupid"]

    # Crear grupo
    created = zbx_request(
        zbx_url,
        "hostgroup.create",
        {"name": name},
        auth,
    )
    return created["groupids"][0]


def get_template_id(zbx_url: str, auth: str, template_name: str) -> str | None:
    result = zbx_request(
        zbx_url,
        "template.get",
        {"filter": {"host": [template_name]}},
        auth,
    )
    if result:
        return result[0]["templateid"]
    return None


def ensure_hosts_from_config(zbx_url: str, auth: str, groupid: str, servers: List[Dict[str, Any]], icmp_template_id: str) -> None:
    created_count = 0
    for server in servers:
        name = server.get("name") or server.get("ip")
        ip = server.get("ip")
        vlan = server.get("vlan")
        srv_type = server.get("type", "server")

        if not ip:
            continue

        host_key = (name or ip).replace(" ", "_")

        existing = zbx_request(
            zbx_url,
            "host.get",
            {"filter": {"host": [host_key]}},
            auth,
        )
        if existing:
            print(f"[ZABBIX-PROVISION] Host ya existe, se omite: {host_key}")
            continue

        params = {
            "host": host_key,
            "name": name,
            "interfaces": [
                {
                    "type": 1,  # Agent (usamos IP para ping/simple checks)
                    "main": 1,
                    "useip": 1,
                    "ip": ip,
                    "dns": "",
                    "port": "10050",
                }
            ],
            "groups": [{"groupid": groupid}],
            "templates": [{"templateid": icmp_template_id}],
            "tags": [
                {"tag": "vlan", "value": str(vlan)},
                {"tag": "role", "value": srv_type},
            ],
        }

        res = zbx_request(zbx_url, "host.create", params, auth)
        created_ids = res.get("hostids", [])
        if created_ids:
            print(f"[ZABBIX-PROVISION] Host creado: {host_key} ({ip})")
            created_count += 1

    print(f"[ZABBIX-PROVISION] Hosts creados desde config.yaml: {created_count}")


def attach_linux_template_to_hypervisor(zbx_url: str, auth: str) -> None:
    """Si existe un host <hostname>-hypervisor, le asocia Template OS Linux."""
    try:
        nodename = os.uname().nodename
    except AttributeError:
        nodename = os.environ.get("HOSTNAME", "hypervisor")

    host_name = f"{nodename}-hypervisor"

    # Buscar host
    hosts = zbx_request(
        zbx_url,
        "host.get",
        {"filter": {"host": [host_name]}},
        auth,
    )
    if not hosts:
        # No hay host de hipervisor registrado, salir silenciosamente
        return

    hostid = hosts[0]["hostid"]

    linux_tpl = get_template_id(zbx_url, auth, "Template OS Linux by Zabbix agent")
    if not linux_tpl:
        print("[ZABBIX-PROVISION] Plantilla 'Template OS Linux by Zabbix agent' no encontrada, se omite.")
        return

    # Añadir plantilla al host
    zbx_request(
        zbx_url,
        "host.update",
        {
            "hostid": hostid,
            "templates": [{"templateid": linux_tpl}],
        },
        auth,
    )
    print(f"[ZABBIX-PROVISION] Plantilla OS Linux asociada a host {host_name}")


def main() -> None:
    try:
        cfg = load_yaml_config()
        secrets = load_secrets()

        monitoring_cfg = cfg.get("monitoring", {})
        zbx_url = monitoring_cfg.get("zabbix_api")
        if not zbx_url:
            print("[ZABBIX-PROVISION] No se ha definido monitoring.zabbix_api en config.yaml")
            sys.exit(0)

        zbx_user = "admin"
        zbx_pass = secrets.get("ZABBIX_ADMIN_PASSWORD", "admin")

        # Login
        print(f"[ZABBIX-PROVISION] Conectando a Zabbix API en {zbx_url}...")
        auth = zbx_request(
            zbx_url,
            "user.login",
            {"user": zbx_user, "password": zbx_pass},
            auth=None,
        )
        print("[ZABBIX-PROVISION] Login correcto en Zabbix API")

        # Grupo principal
        groupid = ensure_hostgroup(zbx_url, auth, "QuickStay Infrastructure")

        # Plantilla ICMP
        icmp_tpl = get_template_id(zbx_url, auth, "Template Module ICMP Ping")
        if not icmp_tpl:
            print("[ZABBIX-PROVISION] Plantilla 'Template Module ICMP Ping' no encontrada; omitiendo auto-configuración.")
            sys.exit(0)

        servers = cfg.get("infrastructure", {}).get("servers", [])
        ensure_hosts_from_config(zbx_url, auth, groupid, servers, icmp_tpl)

        # Intentar asociar plantilla Linux al hipervisor (si existe)
        attach_linux_template_to_hypervisor(zbx_url, auth)

        print("[ZABBIX-PROVISION] Auto-configuración de Zabbix completada.")

    except Exception as exc:  # noqa: BLE001
        print(f"[ZABBIX-PROVISION] Error durante la auto-configuración de Zabbix: {exc}")
        # No queremos romper el despliegue completo
        sys.exit(0)


if __name__ == "__main__":
    main()
