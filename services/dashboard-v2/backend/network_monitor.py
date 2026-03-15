"""
Network Monitor - Real-time packet capture and analysis
Uses tcpdump/scapy to capture and parse network traffic
"""

import asyncio
import subprocess
import re
from typing import List, Dict, Any, Optional
from collections import defaultdict, deque
from datetime import datetime
import psutil


class NetworkMonitor:
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.interface = config['infrastructure']['network']['interface']
        self.capture_process: Optional[subprocess.Popen] = None
        self.packet_queue = deque(maxlen=1000)  # Store last 1000 packets
        self.statistics = defaultdict(int)
        self.filters = {}
        self.running = False
        
    async def start_capture(self):
        """Start capturing network packets (simulated for safety)"""
        self.running = True
        print(f"🔍 Starting network capture on {self.interface}")
        
        # In production, this would use tcpdump or scapy
        # For demo purposes, we'll simulate packet capture
        while self.running:
            await self._simulate_packet_capture()
            await asyncio.sleep(0.1)
    
    def stop_capture(self):
        """Stop packet capture"""
        self.running = False
        if self.capture_process:
            self.capture_process.terminate()
    
    async def _simulate_packet_capture(self):
        """Simulate network packets for demonstration"""
        import random
        
        protocols = ['HTTP', 'HTTPS', 'SSH', 'MYSQL', 'MQTT', 'DNS', 'ICMP']
        servers = self.config['infrastructure']['servers']
        
        # Generate random packets between servers
        for _ in range(random.randint(1, 5)):
            src_server = random.choice(servers)
            dst_server = random.choice(servers)
            
            if src_server['ip'] == dst_server['ip']:
                continue
            
            protocol = random.choice(protocols)
            port_map = {
                'HTTP': 80,
                'HTTPS': 443,
                'SSH': 22,
                'MYSQL': 3306,
                'MQTT': 1883,
                'DNS': 53,
                'ICMP': 0
            }
            
            packet = {
                'timestamp': datetime.now().isoformat(),
                'src_ip': src_server['ip'],
                'dst_ip': dst_server['ip'],
                'src_vlan': src_server['vlan'],
                'dst_vlan': dst_server['vlan'],
                'protocol': protocol,
                'port': port_map.get(protocol, 0),
                'size': random.randint(64, 1500),
                'ttl': random.randint(32, 128)
            }
            
            # Apply filters
            if self._matches_filters(packet):
                self.packet_queue.append(packet)
                self._update_statistics(packet)
    
    def _matches_filters(self, packet: Dict[str, Any]) -> bool:
        """Check if packet matches current filters"""
        if not self.filters:
            return True
        
        if 'protocol' in self.filters:
            if packet['protocol'] not in self.filters['protocol']:
                return False
        
        if 'port' in self.filters:
            if packet['port'] not in self.filters['port']:
                return False
        
        if 'vlan' in self.filters:
            if packet['src_vlan'] not in self.filters['vlan'] and \
               packet['dst_vlan'] not in self.filters['vlan']:
                return False
        
        if 'src_ip' in self.filters:
            if packet['src_ip'] != self.filters['src_ip']:
                return False
        
        if 'dst_ip' in self.filters:
            if packet['dst_ip'] != self.filters['dst_ip']:
                return False
        
        return True
    
    def _update_statistics(self, packet: Dict[str, Any]):
        """Update traffic statistics"""
        self.statistics['total_packets'] += 1
        self.statistics['total_bytes'] += packet['size']
        self.statistics[f"protocol_{packet['protocol']}"] += 1
        self.statistics[f"vlan_{packet['src_vlan']}"] += 1
    
    def update_filters(self, filters: Dict[str, Any]):
        """Update packet filters"""
        self.filters = filters
        print(f"📡 Filters updated: {filters}")
    
    def get_recent_packets(self, limit: int = 10) -> List[Dict[str, Any]]:
        """Get most recent packets"""
        return list(self.packet_queue)[-limit:]
    
    def get_statistics(self) -> Dict[str, Any]:
        """Get aggregated traffic statistics"""
        return {
            'total_packets': self.statistics['total_packets'],
            'total_bytes': self.statistics['total_bytes'],
            'packets_per_second': len(self.packet_queue) / 10 if self.packet_queue else 0,
            'bytes_per_second': sum(p['size'] for p in list(self.packet_queue)[-10:]) if self.packet_queue else 0,
            'protocol_distribution': {
                k.replace('protocol_', ''): v 
                for k, v in self.statistics.items() 
                if k.startswith('protocol_')
            },
            'vlan_distribution': {
                k.replace('vlan_', ''): v 
                for k, v in self.statistics.items() 
                if k.startswith('vlan_')
            }
        }
