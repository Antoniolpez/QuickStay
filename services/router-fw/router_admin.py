#!/usr/bin/env python3
import base64
import datetime
import ipaddress
import time
import os
import re
import shlex
import shutil
import subprocess
import tempfile
from pathlib import Path

from flask import Flask, Response, jsonify, render_template, request

BASE_DIR = Path(__file__).resolve().parent
app = Flask(
    __name__,
    static_folder=str(BASE_DIR / "router_admin_static"),
    static_url_path="/static",
    template_folder=str(BASE_DIR / "router_admin_templates"),
)

ADMIN_USER = os.getenv("ROUTER_ADMIN_USER", "admin")
ADMIN_PASSWORD = os.getenv("ROUTER_ADMIN_PASSWORD", "QuickStay2026!Router")
ALLOWED_CIDRS = [
    c.strip()
    for c in os.getenv(
        "ROUTER_ADMIN_ALLOWED_CIDRS",
        "192.168.0.0/16,172.16.0.0/12,10.0.0.0/8,127.0.0.1/32",
    ).split(",")
    if c.strip()
]
DENY_IPS = {ip.strip() for ip in os.getenv("ROUTER_ADMIN_DENY_IPS", "").split(",") if ip.strip()}
WG_CONF = Path(os.getenv("ROUTER_WG_CONF", "/etc/wireguard/wg0.conf"))
WG_SUBNET = os.getenv("WG_SUBNET", "10.50.0.0/24")
WG_SERVER_IP = os.getenv("WG_SERVER_IP", "10.50.0.1")
WG_ENDPOINT = os.getenv("WG_PUBLIC_ENDPOINT", "192.168.1.40")
WG_PORT = str(os.getenv("WG_SERVER_PORT", "51820"))
WG_DNS = os.getenv("WG_DNS", "172.16.30.10,172.16.30.11")
WG_PEERS_DIR = Path(os.getenv("ROUTER_WG_PEERS_DIR", "/etc/wireguard/peers"))
ACTIVE_SCAN_MAX_NET_SIZE = int(os.getenv("ROUTER_ACTIVE_SCAN_MAX_NET_SIZE", "512"))
ACTIVE_SCAN_GLOBAL_TIMEOUT = int(os.getenv("ROUTER_ACTIVE_SCAN_GLOBAL_TIMEOUT", "22"))
ACTIVE_SCAN_PER_NET_TIMEOUT = int(os.getenv("ROUTER_ACTIVE_SCAN_PER_NET_TIMEOUT", "6"))
ACTIVE_SCAN_SKIP_IFACES = {
    x.strip() for x in os.getenv("ROUTER_ACTIVE_SCAN_SKIP_IFACES", "wg0,lo").split(",") if x.strip()
}


def _run(cmd):
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, check=False)
        return {
            "cmd": " ".join(shlex.quote(p) for p in cmd),
            "code": res.returncode,
            "stdout": res.stdout,
            "stderr": res.stderr,
        }
    except Exception as exc:
        return {
            "cmd": " ".join(shlex.quote(p) for p in cmd),
            "code": 1,
            "stdout": "",
            "stderr": str(exc),
        }


def _run_with_timeout(cmd, timeout_sec=15):
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, check=False, timeout=timeout_sec)
        return {
            "cmd": " ".join(shlex.quote(p) for p in cmd),
            "code": res.returncode,
            "stdout": res.stdout,
            "stderr": res.stderr,
        }
    except subprocess.TimeoutExpired:
        return {
            "cmd": " ".join(shlex.quote(p) for p in cmd),
            "code": 124,
            "stdout": "",
            "stderr": "timeout",
        }
    except Exception as exc:
        return {
            "cmd": " ".join(shlex.quote(p) for p in cmd),
            "code": 1,
            "stdout": "",
            "stderr": str(exc),
        }


def _safe_ipv4(ip_txt):
    try:
        ip_obj = ipaddress.ip_address(ip_txt)
        if ip_obj.version != 4:
            return None
        return str(ip_obj)
    except ValueError:
        return None


def _bytes_to_human(num_bytes):
    kib = float(num_bytes) / 1024.0
    txt = f"{kib:.2f}".replace(".", ",")
    return f"{txt} KiB"


def _collect_devices_from_neigh():
    devices = {}
    out = _run(["ip", "neigh", "show"])
    for line in (out.get("stdout") or "").splitlines():
        parts = line.split()
        if len(parts) < 4:
            continue

        ip_txt = _safe_ipv4(parts[0])
        if not ip_txt:
            continue

        iface = "-"
        mac = "-"
        state = "UNKNOWN"

        if "dev" in parts:
            try:
                iface = parts[parts.index("dev") + 1]
            except Exception:
                iface = "-"

        if "lladdr" in parts:
            try:
                mac = parts[parts.index("lladdr") + 1]
            except Exception:
                mac = "-"

        for token in reversed(parts):
            if token.isupper():
                state = token
                break

        if state in ("FAILED", "INCOMPLETE"):
            continue

        devices[ip_txt] = {
            "ip": ip_txt,
            "mac": mac,
            "iface": iface,
            "state": state,
            "source": "ip-neigh",
        }

    try:
        with open("/proc/net/arp", "r", encoding="utf-8") as f:
            lines = f.read().splitlines()[1:]
    except Exception:
        lines = []

    for line in lines:
        cols = line.split()
        if len(cols) < 6:
            continue
        ip_txt = _safe_ipv4(cols[0])
        if not ip_txt:
            continue
        if ip_txt not in devices:
            mac = cols[3]
            devices[ip_txt] = {
                "ip": ip_txt,
                "mac": mac if mac != "00:00:00:00:00:00" else "-",
                "iface": cols[5],
                "state": "ARP",
                "source": "proc-arp",
            }

    return devices


def _direct_networks_for_scan():
    out = _run(["ip", "-o", "-4", "addr", "show", "scope", "global"])
    nets = []
    seen = set()

    for line in (out.get("stdout") or "").splitlines():
        match = re.search(r"\d+:\s+(\S+)\s+inet\s+(\d+\.\d+\.\d+\.\d+/\d+)", line)
        if not match:
            continue
        iface = match.group(1).split("@", 1)[0]
        cidr = match.group(2)
        try:
            net = ipaddress.ip_interface(cidr).network
        except ValueError:
            continue

        if iface in ACTIVE_SCAN_SKIP_IFACES:
            continue

        if net.num_addresses > ACTIVE_SCAN_MAX_NET_SIZE:
            continue

        key = f"{iface}:{net}"
        if key in seen:
            continue
        seen.add(key)
        nets.append((iface, str(net)))

    return nets


def _collect_devices_active_scan(existing):
    if not shutil.which("nmap"):
        return (
            existing,
            "active requested, nmap not installed",
            "Instala nmap para escaneo activo desde el router.",
            {"nmap_available": False, "scanned_networks": [], "up_hosts_from_nmap": 0, "errors": ["nmap not installed"]},
        )

    devices = dict(existing)
    nets = _direct_networks_for_scan()
    up_hosts_from_nmap = 0
    errors = []
    skipped = []
    scanned = []
    started = time.monotonic()

    if not nets:
        stats = {
            "nmap_available": True,
            "scanned_networks": [],
            "up_hosts_from_nmap": 0,
            "errors": ["no scan networks available"],
            "skipped_networks": [],
            "elapsed_seconds": round(time.monotonic() - started, 2),
            "partial": False,
        }
        return devices, "active scan (nmap)", "No hay redes elegibles para escaneo activo.", stats

    for idx, (iface, network) in enumerate(nets):
        if time.monotonic() - started >= ACTIVE_SCAN_GLOBAL_TIMEOUT:
            skipped.extend([f"{i}:{n} (global-time-budget)" for i, n in nets[idx:]])
            break
        scanned.append(f"{iface}:{network}")
        scan = _run_with_timeout(
            ["nmap", "-sn", "-n", "--max-retries", "1", "--host-timeout", "900ms", "-oG", "-", network],
            timeout_sec=ACTIVE_SCAN_PER_NET_TIMEOUT,
        )
        if scan.get("code") not in (0,):
            err = (scan.get("stderr") or "").strip() or f"scan failed on {network}"
            errors.append(f"{iface}:{network} -> {err}")
        for line in (scan.get("stdout") or "").splitlines():
            if "Status: Up" not in line or "Host:" not in line:
                continue
            match = re.search(r"Host:\s+(\d+\.\d+\.\d+\.\d+)", line)
            if not match:
                continue
            ip_txt = _safe_ipv4(match.group(1))
            if not ip_txt:
                continue
            up_hosts_from_nmap += 1

            cur = devices.get(
                ip_txt,
                {
                    "ip": ip_txt,
                    "mac": "-",
                    "iface": iface,
                    "state": "UP",
                    "source": "nmap-scan",
                },
            )
            cur["iface"] = cur.get("iface") or iface
            cur["state"] = cur.get("state") or "UP"
            if cur.get("source") == "ip-neigh" and cur.get("mac") != "-":
                pass
            else:
                cur["source"] = "nmap-scan"
            devices[ip_txt] = cur

    elapsed = round(time.monotonic() - started, 2)
    note = ""
    if skipped:
        note = f"Escaneo parcial por limite de {ACTIVE_SCAN_GLOBAL_TIMEOUT}s."

    stats = {
        "nmap_available": True,
        "scanned_networks": scanned,
        "up_hosts_from_nmap": up_hosts_from_nmap,
        "errors": errors,
        "skipped_networks": skipped,
        "elapsed_seconds": elapsed,
        "partial": bool(skipped),
    }
    return devices, "active scan (nmap)", note, stats


def _ip_sort_key(ip_txt):
    try:
        return tuple(int(x) for x in ip_txt.split("."))
    except Exception:
        return (999, 999, 999, 999)


def _check_basic_auth():
    auth = request.headers.get("Authorization", "")
    if not auth.startswith("Basic "):
        return False
    token = auth.split(" ", 1)[1].strip()
    try:
        user_pass = base64.b64decode(token).decode("utf-8")
    except Exception:
        return False
    return user_pass == f"{ADMIN_USER}:{ADMIN_PASSWORD}"


def _client_allowed():
    xff = request.headers.get("X-Forwarded-For", "").split(",")[0].strip()
    remote_ip = (request.remote_addr or "").strip()
    candidate_ips = [remote_ip]
    if xff:
        candidate_ips.append(xff)

    for raw in candidate_ips:
        if raw in DENY_IPS:
            return False

    ip_to_check = remote_ip or xff or ""
    try:
        ip_obj = ipaddress.ip_address(ip_to_check)
    except ValueError:
        return False

    for cidr in ALLOWED_CIDRS:
        try:
            if ip_obj in ipaddress.ip_network(cidr, strict=False):
                return True
        except ValueError:
            continue
    return False


def _auth_failed():
    if request.path.startswith("/api/"):
        return jsonify({"error": "unauthorized"}), 401
    return Response("Unauthorized", 401, {"WWW-Authenticate": 'Basic realm="Router Admin"'})


def _forbidden():
    if request.path.startswith("/api/"):
        return jsonify({"error": "forbidden: source IP not allowed"}), 403
    return Response("Forbidden: source IP not allowed", 403)


@app.before_request
def _security_checks():
    if request.path.startswith("/static/"):
        if not _check_basic_auth() or not _client_allowed():
            return _auth_failed()
        return None
    if not _check_basic_auth():
        return _auth_failed()
    if not _client_allowed():
        return _forbidden()
    return None


def _sanitize_rule(rule):
    if not rule or "\n" in rule or "\r" in rule:
        return False
    if len(rule) > 500:
        return False
    if re.search(r"[;&|`$]", rule):
        return False
    return True


def _read_conntrack_lines():
    for path in ("/proc/net/nf_conntrack", "/proc/net/ip_conntrack"):
        try:
            with open(path, "r", encoding="utf-8") as f:
                return f.read().splitlines()
        except Exception:
            continue
    return []


def _collect_port_traffic():
    traffic = {}
    for line in _read_conntrack_lines():
        proto_match = re.search(r"\b(tcp|udp)\b", line)
        if not proto_match:
            continue
        proto = proto_match.group(1)
        dports = [int(x) for x in re.findall(r"\bdport=(\d+)\b", line)]
        sports = [int(x) for x in re.findall(r"\bsport=(\d+)\b", line)]
        bytes_list = [int(x) for x in re.findall(r"\bbytes=(\d+)\b", line)]

        if dports and bytes_list:
            key = (proto, dports[0])
            traffic.setdefault(key, {"rx": 0, "tx": 0})
            traffic[key]["rx"] += bytes_list[0]
        if len(sports) > 1 and len(bytes_list) > 1:
            key = (proto, sports[1])
            traffic.setdefault(key, {"rx": 0, "tx": 0})
            traffic[key]["tx"] += bytes_list[1]
    return traffic


def _collect_open_ports():
    out = _run(["ss", "-lntupH"])
    rows = []
    traffic = _collect_port_traffic()
    seen = set()

    for line in (out.get("stdout") or "").splitlines():
        parts = line.split()
        if len(parts) < 5:
            continue
        proto = parts[0].lower()
        local = parts[4]
        if ":" not in local:
            continue
        try:
            port = int(local.rsplit(":", 1)[1])
        except Exception:
            continue
        process = parts[6] if len(parts) >= 7 else "-"
        key = (proto, port)
        if key in seen:
            continue
        seen.add(key)
        t = traffic.get(key, {"rx": 0, "tx": 0})
        rows.append(
            {
                "proto": proto,
                "port": port,
                "received": _bytes_to_human(t["rx"]),
                "sent": _bytes_to_human(t["tx"]),
                "process": process,
            }
        )

    rows.sort(key=lambda x: (x["proto"], x["port"]))
    return rows


def _peer_names_from_conf():
    names = {}
    if not WG_CONF.exists():
        return names
    current_name = ""
    for line in WG_CONF.read_text(encoding="utf-8").splitlines():
        text = line.strip()
        if text.startswith("#"):
            current_name = text.lstrip("#").strip()
            continue
        if text.lower().startswith("publickey"):
            pub = text.split("=", 1)[1].strip()
            names[pub] = current_name or "peer"
            current_name = ""
    return names


def _wg_dump():
    out = _run(["wg", "show", "all", "dump"])
    if out.get("code") != 0:
        return [], out
    lines = [ln for ln in (out.get("stdout") or "").splitlines() if ln.strip()]
    return lines, out


def _format_handshake(epoch_s):
    try:
        epoch = int(epoch_s)
    except Exception:
        return "-"
    if epoch <= 0:
        return "never"
    dt = datetime.datetime.utcfromtimestamp(epoch)
    delta = datetime.datetime.utcnow() - dt
    sec = int(delta.total_seconds())
    if sec < 60:
        return f"{sec}s ago"
    if sec < 3600:
        return f"{sec // 60}m ago"
    return f"{sec // 3600}h ago"


def _vpn_peers():
    names = _peer_names_from_conf()
    lines, out = _wg_dump()
    if out.get("code") != 0 or not lines:
        return [], out

    peers = []
    for row in lines[1:]:
        cols = row.split("\t")
        if len(cols) < 9:
            continue
        pub = cols[1]
        endpoint = cols[3] if cols[3] and cols[3] != "(none)" else "-"
        allowed = cols[4].split(",")[0] if cols[4] else "-"
        hs = _format_handshake(cols[5])
        rx = _bytes_to_human(int(cols[6] or 0))
        tx = _bytes_to_human(int(cols[7] or 0))
        peers.append(
            {
                "name": names.get(pub, "peer"),
                "public_key": pub,
                "endpoint": endpoint,
                "allowed_ip": allowed,
                "latest_handshake": hs,
                "received": rx,
                "sent": tx,
            }
        )

    peers.sort(key=lambda p: p["allowed_ip"])
    return peers, out


def _next_client_ip():
    net = ipaddress.ip_network(WG_SUBNET, strict=False)
    used = set()
    peers, _ = _vpn_peers()
    for peer in peers:
        try:
            used.add(ipaddress.ip_address(peer["allowed_ip"].split("/")[0]))
        except Exception:
            continue
    used.add(ipaddress.ip_address(WG_SERVER_IP))

    for host in net.hosts():
        if int(host.packed[-1]) < 2:
            continue
        if host not in used:
            return str(host)
    return None


def _wg_interface_public_key():
    lines, out = _wg_dump()
    if out.get("code") != 0 or not lines:
        return None
    cols = lines[0].split("\t")
    return cols[2] if len(cols) > 2 else None


def _persist_client_artifacts(name, client_conf):
    WG_PEERS_DIR.mkdir(parents=True, exist_ok=True)

    conf_path = WG_PEERS_DIR / f"{name}.conf"
    txt_path = WG_PEERS_DIR / f"{name}.qr.txt"
    png_path = WG_PEERS_DIR / f"{name}.qr.png"

    conf_path.write_text(client_conf, encoding="utf-8")

    # Compat path expected by legacy scripts: /etc/wireguard/peer_<name>.conf
    legacy_conf_path = Path("/etc/wireguard") / f"peer_{name}.conf"
    try:
        legacy_conf_path.write_text(client_conf, encoding="utf-8")
    except Exception:
        legacy_conf_path = None

    qr_note = ""

    if shutil.which("qrencode"):
        txt_res = _run(["qrencode", "-t", "ANSIUTF8", "-o", str(txt_path), client_conf])
        png_res = _run(["qrencode", "-t", "PNG", "-o", str(png_path), client_conf])
        if txt_res.get("code") != 0 or png_res.get("code") != 0:
            qr_note = "no se pudo generar QR"
    else:
        qr_note = "qrencode no instalado"

    return {
        "conf": str(conf_path),
        "legacy_conf": str(legacy_conf_path) if legacy_conf_path else "",
        "qr_txt": str(txt_path),
        "qr_png": str(png_path),
        "qr_note": qr_note,
    }


@app.get("/")
def index():
    return Response(
        render_template("index.html"),
        200,
        {
            "Content-Type": "text/html; charset=utf-8",
            "Cache-Control": "no-store, no-cache, must-revalidate, max-age=0",
            "Pragma": "no-cache",
            "Expires": "0",
        },
    )


@app.get("/api/status")
@app.get("/status")
def status():
    return jsonify(
        {
            "ip_forward": _run(["sysctl", "-n", "net.ipv4.ip_forward"]),
            "interfaces": _run(["ip", "-brief", "addr"]),
            "routes": _run(["ip", "route", "show"]),
        }
    )


@app.get("/api/whoami")
def whoami():
    xff = request.headers.get("X-Forwarded-For", "").split(",")[0].strip()
    return jsonify({"remote_addr": request.remote_addr, "x_forwarded_for": xff})


@app.get("/api/nft/ruleset")
@app.get("/nft/ruleset")
def nft_ruleset():
    return jsonify(_run(["nft", "list", "ruleset"]))


@app.get("/api/iptables/filter")
@app.get("/iptables/filter")
def iptables_filter():
    return jsonify(_run(["iptables", "-S"]))


@app.get("/api/routes")
@app.get("/routes")
def routes():
    return jsonify(_run(["ip", "route", "show"]))


@app.get("/api/ports")
def ports():
    return jsonify({"ports": _collect_open_ports()})


@app.get("/api/devices")
def devices():
    active = request.args.get("active", "0") == "1"
    found = _collect_devices_from_neigh()
    mode = "neighbors (arp cache)"
    note = ""
    scan = {"nmap_available": False, "scanned_networks": [], "up_hosts_from_nmap": 0, "errors": []}

    if active:
        found, mode, note, scan = _collect_devices_active_scan(found)

    ordered = [found[k] for k in sorted(found.keys(), key=_ip_sort_key)]
    return jsonify({"mode": mode, "note": note, "count": len(ordered), "devices": ordered, "scan": scan})


@app.get("/api/vpn/peers")
def vpn_peers():
    peers, out = _vpn_peers()
    if out.get("code") != 0:
        return jsonify({"error": out.get("stderr") or "wg error"}), 500
    return jsonify({"count": len(peers), "peers": peers})


@app.post("/api/vpn/add")
def vpn_add_peer():
    payload = request.get_json(silent=True) or {}
    name = str(payload.get("name", "")).strip()
    if not re.fullmatch(r"[a-zA-Z0-9_.-]{3,32}", name):
        return jsonify({"error": "name invalido (3-32, a-zA-Z0-9_.-)"}), 400
    if not WG_CONF.exists():
        return jsonify({"error": f"wg conf no encontrado: {WG_CONF}"}), 500

    client_ip = _next_client_ip()
    if not client_ip:
        return jsonify({"error": "no hay IP libre en la subred VPN"}), 400

    priv = (_run(["wg", "genkey"]).get("stdout") or "").strip()
    if not priv:
        return jsonify({"error": "no se pudo generar private key"}), 500
    pub = (_run(["sh", "-lc", f"printf '%s' {shlex.quote(priv)} | wg pubkey"]).get("stdout") or "").strip()
    psk = (_run(["wg", "genpsk"]).get("stdout") or "").strip()
    if not pub or not psk:
        return jsonify({"error": "no se pudieron generar claves de peer"}), 500

    try:
        with open(WG_CONF, "a", encoding="utf-8") as f:
            f.write("\n[Peer]\n")
            f.write(f"# {name}\n")
            f.write(f"PublicKey = {pub}\n")
            f.write(f"PresharedKey = {psk}\n")
            f.write(f"AllowedIPs = {client_ip}/32\n")
    except Exception as exc:
        return jsonify({"error": f"no se pudo escribir wg conf: {exc}"}), 500

    with tempfile.NamedTemporaryFile("w", delete=False, encoding="utf-8") as tf:
        tf.write(psk + "\n")
        psk_file = tf.name
    try:
        set_res = _run(["wg", "set", "wg0", "peer", pub, "preshared-key", psk_file, "allowed-ips", f"{client_ip}/32"])
    finally:
        try:
            os.unlink(psk_file)
        except Exception:
            pass

    if set_res.get("code") != 0:
        return jsonify({"error": set_res.get("stderr") or "wg set failed"}), 500

    server_pub = _wg_interface_public_key()
    if not server_pub:
        return jsonify({"error": "no se pudo leer public key del servidor"}), 500

    client_conf = (
        "[Interface]\n"
        f"Address = {client_ip}/32\n"
        f"PrivateKey = {priv}\n"
        f"DNS = {WG_DNS}\n\n"
        "[Peer]\n"
        f"PublicKey = {server_pub}\n"
        f"PresharedKey = {psk}\n"
        f"Endpoint = {WG_ENDPOINT}:{WG_PORT}\n"
        "AllowedIPs = 172.16.0.0/16, 10.50.0.0/24\n"
        "PersistentKeepalive = 25\n"
    )

    try:
        artifacts = _persist_client_artifacts(name, client_conf)
    except Exception as exc:
        return jsonify({"error": f"peer creado pero no se pudo exportar artefactos: {exc}", "client_conf": client_conf}), 500

    return jsonify(
        {
            "ok": True,
            "name": name,
            "client_ip": client_ip,
            "client_conf": client_conf,
            "artifacts": artifacts,
        }
    )


@app.post("/api/nft/apply")
@app.post("/nft/apply")
def nft_apply():
    payload = request.get_json(silent=True) or {}
    rule = payload.get("rule", "").strip()
    if not _sanitize_rule(rule):
        return jsonify({"error": "invalid rule format"}), 400
    cmd = ["nft"] + shlex.split(rule)
    result = _run(cmd)
    return jsonify(result), 200 if result.get("code", 1) == 0 else 400


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8443)
