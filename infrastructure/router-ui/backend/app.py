#!/usr/bin/env python3
"""
Router Management Dashboard API
Frontend para monitoreo y control del Router Firewall (nftables)
"""

from flask import Flask, jsonify, request, send_from_directory
from flask_cors import CORS
import subprocess
import json
import re
import socket
import os
from datetime import datetime
import logging

app = Flask(__name__, static_folder="static", static_url_path="")
CORS(app)

# Configuración de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============================================================================
# UTILIDADES
# ============================================================================

def run_command(cmd, shell=False):
    """Ejecutar comando y retornar output"""
    try:
        if isinstance(cmd, str) and not shell:
            cmd = cmd.split()
        result = subprocess.run(cmd, capture_output=True, text=True, shell=shell)
        return result.stdout.strip(), result.returncode
    except Exception as e:
        logger.error(f"Error ejecutando comando: {e}")
        return "", 1

def parse_nftables_rules():
    """Parsear reglas de nftables"""
    output, _ = run_router_command("nft list ruleset")
    if not output:
        return []
    
    rules = []
    current_table = None
    current_chain = None
    
    for line in output.split('\n'):
        line = line.strip()
        
        if line.startswith('table inet'):
            current_table = line.split()[-1]
        elif line.startswith('chain '):
            parts = line.split()
            current_chain = parts[1]
        elif line.startswith('rule'):
            rules.append({
                'table': current_table,
                'chain': current_chain,
                'rule': line
            })
    
    return rules

def get_interface_stats():
    """Obtener estadísticas de interfaces"""
    output, _ = run_router_command("ip -s link")
    interfaces = {}
    
    current_iface = None
    for line in output.split('\n'):
        if line and not line.startswith(' '):
            iface_match = re.match(r'^(\d+):\s+([^:]+):', line)
            if iface_match:
                current_iface = iface_match.group(2)
                interfaces[current_iface] = {
                    'name': current_iface,
                    'status': 'UP' if 'UP' in line else 'DOWN',
                    'rx_bytes': 0,
                    'tx_bytes': 0,
                    'rx_packets': 0,
                    'tx_packets': 0
                }
        elif current_iface and 'RX:' in line:
            parts = line.split()
            if len(parts) >= 2:
                interfaces[current_iface]['rx_bytes'] = int(parts[1]) if parts[1].isdigit() else 0
        elif current_iface and 'TX:' in line:
            parts = line.split()
            if len(parts) >= 2:
                interfaces[current_iface]['tx_bytes'] = int(parts[1]) if parts[1].isdigit() else 0
    
    return list(interfaces.values())

def get_vlans():
    """Obtener configuración de VLANs"""
    output, _ = run_router_command("ip -d link show type vlan")
    vlans = []
    
    for line in output.split('\n'):
        if 'vlan' in line.lower():
            vlan_match = re.search(r'id\s+(\d+)', line)
            if vlan_match:
                vlan_id = vlan_match.group(1)
                vlans.append({
                    'id': vlan_id,
                    'name': line.split()[0].rstrip(':') if ':' in line else f'vlan{vlan_id}'
                })
    
    return vlans

def get_routes():
    """Obtener tabla de enrutamiento"""
    output, _ = run_router_command("ip route show")
    routes = []
    
    for line in output.split('\n'):
        if line.strip():
            parts = line.split()
            if len(parts) >= 2:
                routes.append({
                    'destination': parts[0],
                    'gateway': parts[2] if 'via' in parts else 'direct',
                    'metric': parts[4] if 'metric' in parts else '0',
                    'route': line
                })
    
    return routes

def get_network_stats():
    """Obtener estadísticas generales de red"""
    output, _ = run_router_command("ss -s")
    stats = {
        'timestamp': datetime.now().isoformat(),
        'tcp_connections': 0,
        'tcp_established': 0,
        'udp_connections': 0
    }
    
    for line in output.split('\n'):
        if 'TCP' in line and 'estab' in line:
            match = re.search(r'(\d+)\s+estab', line)
            if match:
                stats['tcp_established'] = int(match.group(1))
    
    return stats

# ============================================================================
# RUTAS API
# ============================================================================

@app.route('/', methods=['GET'])
def index():
    """Servir el dashboard web"""
    static_dir = app.static_folder or "static"
    index_path = os.path.join(static_dir, "index.html")
    if os.path.exists(index_path):
        return send_from_directory(static_dir, "index.html")
    return jsonify({'error': 'UI not found'}), 404

@app.route('/api/health', methods=['GET'])
def health():
    """Health check"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/dashboard', methods=['GET'])
def dashboard():
    """Dashboard principal con todos los datos"""
    return jsonify({
        'status': 'ok',
        'timestamp': datetime.now().isoformat(),
        'interfaces': get_interface_stats(),
        'vlans': get_vlans(),
        'network_stats': get_network_stats(),
        'routes_count': len(get_routes())
    })

@app.route('/api/interfaces', methods=['GET'])
def interfaces():
    """Listado detallado de interfaces"""
    return jsonify({
        'interfaces': get_interface_stats(),
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/interfaces/<iface>', methods=['GET'])
def interface_detail(iface):
    """Detalles de una interfaz específica"""
    stats = get_interface_stats()
    iface_data = next((i for i in stats if i['name'] == iface), None)
    
    if not iface_data:
        return jsonify({'error': f'Interface {iface} not found'}), 404
    
    # Obtener información adicional
    output, _ = run_command(f"ip addr show {iface}")
    
    return jsonify({
        **iface_data,
        'details': output,
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/vlans', methods=['GET'])
def vlans():
    """Listado de VLANs"""
    return jsonify({
        'vlans': get_vlans(),
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/routing', methods=['GET'])
def routing():
    """Tabla de enrutamiento"""
    return jsonify({
        'routes': get_routes(),
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/nftables', methods=['GET'])
def nftables():
    """Listado completo de reglas nftables"""
    chain_filter = request.args.get('chain')
    
    output, _ = run_command("nft list ruleset -j")
    
    try:
        rules_data = json.loads(output) if output else {'nftables': []}
    except:
        rules_data = {'nftables': []}
    
    if chain_filter:
        filtered = []
        for item in rules_data.get('nftables', []):
            if 'rule' in item and chain_filter in str(item):
                filtered.append(item)
        rules_data['nftables'] = filtered
    
    return jsonify({
        **rules_data,
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/nftables/tables', methods=['GET'])
def nftables_tables():
    """Tablas de nftables"""
    output, _ = run_command("nft list tables")
    tables = [line.strip().replace('table inet ', '') for line in output.split('\n') if 'table' in line]
    
    return jsonify({
        'tables': tables,
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/nftables/rules', methods=['GET'])
def nftables_rules():
    """Reglas nftables parseadas"""
    table = request.args.get('table', 'quickstay')
    
    rules = parse_nftables_rules()
    if table:
        rules = [r for r in rules if r.get('table') == table]
    
    return jsonify({
        'rules': rules,
        'total': len(rules),
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/nftables/stats', methods=['GET'])
def nftables_stats():
    """Estadísticas de reglas nftables"""
    output, _ = run_command("nft list ruleset")
    
    table_count = len(re.findall(r'table inet', output))
    chain_count = len(re.findall(r'chain ', output))
    rule_count = len(re.findall(r'rule ', output))
    
    return jsonify({
        'tables': table_count,
        'chains': chain_count,
        'rules': rule_count,
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/network-stats', methods=['GET'])
def network_stats():
    """Estadísticas de red generales"""
    return jsonify({
        **get_network_stats()
    })

@app.route('/api/logs', methods=['GET'])
def logs():
    """Logs de nftables/router"""
    lines = request.args.get('lines', 50, type=int)
    output, _ = run_command(f"tail -n {lines} /var/log/syslog")
    
    # Filtrar logs relevantes del router
    router_logs = [line for line in output.split('\n') 
                   if 'nft' in line or 'router' in line or 'iptables' in line]
    
    return jsonify({
        'logs': router_logs[-50:],  # Últimos 50
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/config', methods=['GET'])
def config():
    """Configuración actual del router"""
    return jsonify({
        'hostname': socket.gethostname(),
        'vlans': get_vlans(),
        'interfaces': [i['name'] for i in get_interface_stats()],
        'routes': len(get_routes()),
        'firewall_rules': len(parse_nftables_rules()),
        'timestamp': datetime.now().isoformat()
    })

# ============================================================================
# RUTAS DE CONTROL (requieren autenticación en producción)
# ============================================================================

@app.route('/api/control/reload-rules', methods=['POST'])
def reload_rules():
    """Recargar reglas nftables"""
    output, code = run_router_command("nft -f /etc/nftables.conf")
    
    return jsonify({
        'success': code == 0,
        'message': output if code != 0 else 'Rules reloaded successfully',
        'timestamp': datetime.now().isoformat()
    }), 200 if code == 0 else 400

@app.route('/api/control/interface/<iface>/up', methods=['POST'])
def interface_up(iface):
    """Habilitar interfaz"""
    output, code = run_router_command(f"ip link set {iface} up")
    
    return jsonify({
        'success': code == 0,
        'message': f"Interface {iface} brought up" if code == 0 else output,
        'timestamp': datetime.now().isoformat()
    }), 200 if code == 0 else 400

@app.route('/api/control/interface/<iface>/down', methods=['POST'])
def interface_down(iface):
    """Deshabilitar interfaz"""
    output, code = run_router_command(f"ip link set {iface} down")
    
    return jsonify({
        'success': code == 0,
        'message': f"Interface {iface} brought down" if code == 0 else output,
        'timestamp': datetime.now().isoformat()
    }), 200 if code == 0 else 400

# ============================================================================
# ERROR HANDLERS
# ============================================================================

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Not found'}), 404

@app.errorhandler(500)
def server_error(error):
    logger.error(f"Server error: {error}")
    return jsonify({'error': 'Internal server error'}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
