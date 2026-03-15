"""
Topology Builder - Network topology generator
Builds network graph from configuration and diagrams
"""

from typing import Dict, Any, List
import psutil
import random


class TopologyBuilder:
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.servers = config['infrastructure']['servers']
        self.vlans = config['infrastructure']['network']['vlans']
        
    def get_topology(self) -> Dict[str, Any]:
        """
        Build network topology with nodes and edges
        Returns: {
            "nodes": [...],  # Servers, routers, switches
            "edges": [...]   # Network connections
        }
        """
        nodes = []
        edges = []
        
        # Add VLAN nodes
        for vlan in self.vlans:
            nodes.append({
                'id': f"vlan_{vlan['id']}",
                'type': 'vlan',
                'label': vlan['name'],
                'vlan_id': vlan['id'],
                'subnet': vlan['subnet'],
                'group': vlan['id']
            })
        
        # Add server nodes
        for server in self.servers:
            nodes.append({
                'id': server['ip'],
                'type': server['type'],
                'label': server['name'],
                'ip': server['ip'],
                'vlan': server['vlan'],
                'services': server['services'],
                'status': self._get_server_status(server),
                'group': server['vlan']
            })
            
            # Connect server to its VLAN
            edges.append({
                'id': f"{server['ip']}_to_vlan_{server['vlan']}",
                'source': server['ip'],
                'target': f"vlan_{server['vlan']}",
                'type': 'network_link',
                'bandwidth': '1Gbps'
            })
        
        # Add inter-VLAN connections (through router)
        router_id = 'router_main'
        nodes.append({
            'id': router_id,
            'type': 'router',
            'label': 'Main Router',
            'ip': '172.16.0.1',
            'status': 'online',
            'group': 0
        })
        
        for vlan in self.vlans:
            edges.append({
                'id': f"router_to_vlan_{vlan['id']}",
                'source': router_id,
                'target': f"vlan_{vlan['id']}",
                'type': 'trunk_link',
                'bandwidth': '10Gbps'
            })
        
        # Add application-specific connections
        # Web servers -> Load balancer
        lb = next((s for s in self.servers if s['type'] == 'load_balancer'), None)
        if lb:
            web_servers = [s for s in self.servers if s['type'] == 'web']
            for web in web_servers:
                edges.append({
                    'id': f"{lb['ip']}_to_{web['ip']}",
                    'source': lb['ip'],
                    'target': web['ip'],
                    'type': 'app_link',
                    'protocol': 'HTTP'
                })
        
        # App server -> Database
        app_server = next((s for s in self.servers if s['type'] == 'application'), None)
        db_server = next((s for s in self.servers if s['type'] == 'database'), None)
        if app_server and db_server:
            edges.append({
                'id': f"{app_server['ip']}_to_{db_server['ip']}",
                'source': app_server['ip'],
                'target': db_server['ip'],
                'type': 'app_link',
                'protocol': 'MySQL'
            })
        
        return {
            'nodes': nodes,
            'edges': edges,
            'layout': 'force-directed',  # or 'hierarchical'
            'metadata': {
                'total_nodes': len(nodes),
                'total_edges': len(edges),
                'vlan_count': len(self.vlans),
                'server_count': len(self.servers)
            }
        }
    
    def _get_server_status(self, server: Dict[str, Any]) -> str:
        """Get server status (online/warning/offline)"""
        # In production, this would ping the server or check monitoring
        # For demo, randomly assign status with bias toward online
        statuses = ['online'] * 8 + ['warning'] * 1 + ['offline'] * 1
        return random.choice(statuses)
    
    def get_servers_status(self) -> List[Dict[str, Any]]:
        """Get list of all servers with current status"""
        servers_status = []
        
        for server in self.servers:
            servers_status.append({
                **server,
                'status': self._get_server_status(server),
                'metrics': {
                    'cpu': random.uniform(10, 90),
                    'memory': random.uniform(20, 80),
                    'disk': random.uniform(15, 70),
                    'uptime': random.randint(3600, 86400 * 30)
                }
            })
        
        return servers_status
    
    def get_server_by_ip(self, ip: str) -> Dict[str, Any]:
        """Get server details by IP address"""
        for server in self.servers:
            if server['ip'] == ip:
                return {
                    **server,
                    'status': self._get_server_status(server)
                }
        return None
    
    def get_vlan_topology(self, vlan_id: int) -> Dict[str, Any]:
        """Get topology for a specific VLAN"""
        vlan = next((v for v in self.vlans if v['id'] == vlan_id), None)
        if not vlan:
            return None
        
        vlan_servers = [s for s in self.servers if s['vlan'] == vlan_id]
        
        return {
            'vlan': vlan,
            'servers': vlan_servers,
            'server_count': len(vlan_servers)
        }
