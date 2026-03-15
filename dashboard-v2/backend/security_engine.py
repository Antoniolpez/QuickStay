"""
Security Engine - Orchestration of security attacks and tests
Executes penetration testing tools in controlled manner
"""

import asyncio
import subprocess
import uuid
from typing import Dict, Any, List, Optional
from datetime import datetime
from collections import defaultdict


class SecurityEngine:
    def __init__(self, config: Dict[str, Any]):
        self.config = config
        self.attack_jobs = {}
        self.attack_history = []
        self.scenario_jobs = {}
    
    async def execute_attack(self, attack_type: str, target: str, parameters: Dict[str, Any]) -> str:
        """Execute a security attack and return job ID"""
        job_id = str(uuid.uuid4())
        
        job_info = {
            'job_id': job_id,
            'attack_type': attack_type,
            'target': target,
            'parameters': parameters,
            'status': 'running',
            'started_at': datetime.now().isoformat(),
            'output': [],
            'result': None
        }
        
        self.attack_jobs[job_id] = job_info
        
        # Execute attack in background
        asyncio.create_task(self._run_attack(job_id))
        
        return job_id
    
    async def _run_attack(self, job_id: str):
        """Run the actual attack (simulated for safety)"""
        job = self.attack_jobs[job_id]
        attack_type = job['attack_type']
        target = job['target']
        
        try:
            if attack_type == 'port_scan':
                await self._port_scan(job)
            elif attack_type == 'dos_simulation':
                await self._dos_simulation(job)
            elif attack_type == 'sql_injection':
                await self._sql_injection_test(job)
            elif attack_type == 'network_disruption':
                await self._network_disruption(job)
            elif attack_type == 'vulnerability_scan':
                await self._vulnerability_scan(job)
            else:
                job['status'] = 'error'
                job['result'] = {'error': f'Unknown attack type: {attack_type}'}
            
            job['completed_at'] = datetime.now().isoformat()
            
            # Add to history
            self.attack_history.append({
                'job_id': job_id,
                'attack_type': attack_type,
                'target': target,
                'completed_at': job['completed_at'],
                'status': job['status'],
                'summary': self._generate_summary(job)
            })
            
        except Exception as e:
            job['status'] = 'error'
            job['result'] = {'error': str(e)}
            job['completed_at'] = datetime.now().isoformat()
    
    async def _port_scan(self, job: Dict[str, Any]):
        """Simulate nmap port scan"""
        target = job['target']
        
        job['output'].append(f"Starting Nmap 7.94 at {datetime.now().strftime('%Y-%m-%d %H:%M')}")
        await asyncio.sleep(0.5)
        
        # Simulate scanning common ports
        common_ports = [22, 80, 443, 3306, 5432, 8080, 8443]
        open_ports = []
        
        for port in common_ports:
            job['output'].append(f"Scanning port {port}...")
            await asyncio.sleep(0.2)
            
            # Randomly determine if port is open (for demo)
            import random
            if random.random() > 0.5:
                open_ports.append(port)
                service = {22: 'SSH', 80: 'HTTP', 443: 'HTTPS', 3306: 'MySQL', 8080: 'HTTP-Alt'}.get(port, 'unknown')
                job['output'].append(f"PORT {port}/tcp OPEN {service}")
        
        job['status'] = 'completed'
        job['result'] = {
            'target': target,
            'open_ports': open_ports,
            'total_scanned': len(common_ports),
            'scan_time': '2.45 seconds'
        }
    
    async def _dos_simulation(self, job: Dict[str, Any]):
        """Simulate DoS attack (controlled)"""
        target = job['target']
        intensity = job['parameters'].get('intensity', 'low')
        
        job['output'].append(f"⚠️  DoS Simulation started against {target}")
        job['output'].append(f"Intensity: {intensity}")
        await asyncio.sleep(1)
        
        job['output'].append("Sending 1000 requests...")
        await asyncio.sleep(1)
        job['output'].append("Sending 2000 requests...")
        await asyncio.sleep(1)
        job['output'].append("Sending 5000 requests...")
        await asyncio.sleep(1)
        
        job['output'].append("✓ Simulation completed")
        job['status'] = 'completed'
        job['result'] = {
            'target': target,
            'requests_sent': 8000,
            'target_response': 'degraded',
            'average_latency_increase': '340%'
        }
    
    async def _sql_injection_test(self, job: Dict[str, Any]):
        """Simulate SQL injection vulnerability test"""
        target = job['target']
        
        job['output'].append(f"🔍 Testing SQL Injection on {target}")
        await asyncio.sleep(0.5)
        
        job['output'].append("Testing: ' OR '1'='1")
        await asyncio.sleep(0.5)
        job['output'].append("Testing: '; DROP TABLE--")
        await asyncio.sleep(0.5)
        job['output'].append("Testing: UNION SELECT NULL,NULL--")
        await asyncio.sleep(0.5)
        
        job['output'].append("✓ Tests completed")
        job['status'] = 'completed'
        job['result'] = {
            'target': target,
            'vulnerable': False,
            'tests_run': 12,
            'findings': 'No SQL injection vulnerabilities detected'
        }
    
    async def _network_disruption(self, job: Dict[str, Any]):
        """Simulate network disruption (VLAN down, etc.)"""
        target = job['target']
        action = job['parameters'].get('action', 'interface_down')
        
        job['output'].append(f"⚠️  Network disruption: {action} on {target}")
        await asyncio.sleep(1)
        
        if action == 'vlan_down':
            job['output'].append(f"Bringing down VLAN interface...")
            await asyncio.sleep(1)
            job['output'].append(f"✓ VLAN is now DOWN")
        elif action == 'interface_down':
            job['output'].append(f"Bringing down network interface...")
            await asyncio.sleep(1)
            job['output'].append(f"✓ Interface is now DOWN")
        
        job['status'] = 'completed'
        job['result'] = {
            'target': target,
            'action': action,
            'status': 'disrupted',
            'restoration_command': 'sudo ifconfig <interface> up'
        }
    
    async def _vulnerability_scan(self, job: Dict[str, Any]):
        """Simulate vulnerability scanning"""
        target = job['target']
        
        job['output'].append(f"🔎 Vulnerability scan on {target}")
        await asyncio.sleep(1)
        
        vulnerabilities = [
            {'id': 'CVE-2023-12345', 'severity': 'MEDIUM', 'service': 'Apache 2.4.52'},
            {'id': 'CVE-2023-54321', 'severity': 'LOW', 'service': 'OpenSSL 1.1.1'},
        ]
        
        for vuln in vulnerabilities:
            job['output'].append(f"Found: {vuln['id']} ({vuln['severity']}) in {vuln['service']}")
            await asyncio.sleep(0.5)
        
        job['status'] = 'completed'
        job['result'] = {
            'target': target,
            'vulnerabilities': vulnerabilities,
            'severity_summary': {'high': 0, 'medium': 1, 'low': 1}
        }
    
    def _generate_summary(self, job: Dict[str, Any]) -> str:
        """Generate human-readable summary of attack"""
        attack_type = job['attack_type']
        result = job.get('result', {})
        
        if attack_type == 'port_scan':
            open_ports = result.get('open_ports', [])
            return f"Found {len(open_ports)} open ports"
        elif attack_type == 'dos_simulation':
            return f"DoS simulation completed, target response: {result.get('target_response', 'unknown')}"
        elif attack_type == 'sql_injection':
            vulnerable = result.get('vulnerable', False)
            return f"SQL Injection test: {'VULNERABLE' if vulnerable else 'SECURE'}"
        elif attack_type == 'network_disruption':
            return f"Network disruption: {result.get('status', 'unknown')}"
        else:
            return "Attack completed"
    
    def get_attack_result(self, job_id: str) -> Optional[Dict[str, Any]]:
        """Get attack job result by ID"""
        return self.attack_jobs.get(job_id)
    
    def get_attack_history(self) -> List[Dict[str, Any]]:
        """Get history of all attacks"""
        return self.attack_history
    
    async def execute_scenario(self, scenario: Dict[str, Any]) -> str:
        """Execute a chaos scenario (multiple sequential attacks)"""
        scenario_id = str(uuid.uuid4())
        actions = scenario.get('actions', [])
        
        scenario_info = {
            'scenario_id': scenario_id,
            'name': scenario.get('name', 'Unnamed Scenario'),
            'status': 'running',
            'started_at': datetime.now().isoformat(),
            'actions': actions,
            'results': []
        }
        
        self.scenario_jobs[scenario_id] = scenario_info
        
        # Execute scenario in background
        asyncio.create_task(self._run_scenario(scenario_id))
        
        return scenario_id
    
    async def _run_scenario(self, scenario_id: str):
        """Execute scenario actions sequentially"""
        scenario = self.scenario_jobs[scenario_id]
        
        for action in scenario['actions']:
            delay = action.get('delay', 0)
            if delay > 0:
                await asyncio.sleep(delay)
            
            # Execute action
            job_id = await self.execute_attack(
                action['type'],
                action['target'],
                action.get('parameters', {})
            )
            
            # Wait for completion
            while self.attack_jobs[job_id]['status'] == 'running':
                await asyncio.sleep(0.5)
            
            scenario['results'].append({
                'action': action['type'],
                'job_id': job_id,
                'result': self.attack_jobs[job_id]['result']
            })
        
        scenario['status'] = 'completed'
        scenario['completed_at'] = datetime.now().isoformat()
