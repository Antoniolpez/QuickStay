"""
QuickStay Master Dashboard v2.0 - Backend API
FastAPI application with WebSocket support for real-time monitoring
"""

from fastapi import FastAPI, WebSocket, WebSocketDisconnect, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import List, Dict, Any
import asyncio
import json
import yaml
from pathlib import Path

from network_monitor import NetworkMonitor
from security_engine import SecurityEngine
from topology_builder import TopologyBuilder

app = FastAPI(title="QuickStay Master Dashboard API", version="2.0.0")

# CORS middleware for frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load configuration
config_path = Path(__file__).parent / "config.yaml"
with open(config_path, "r") as f:
    config = yaml.safe_load(f)

# Initialize modules
network_monitor = NetworkMonitor(config)
security_engine = SecurityEngine(config)
topology_builder = TopologyBuilder(config)

# WebSocket connection manager
class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket):
        self.active_connections.remove(websocket)

    async def broadcast(self, message: dict):
        for connection in self.active_connections:
            try:
                await connection.send_json(message)
            except:
                pass

manager = ConnectionManager()


# REST Endpoints
@app.get("/")
async def root():
    return {
        "name": "QuickStay Master Dashboard API",
        "version": "2.0.0",
        "status": "operational"
    }


@app.get("/api/topology")
async def get_topology():
    """Get network topology (nodes and edges)"""
    return topology_builder.get_topology()


@app.get("/api/servers")
async def get_servers():
    """Get list of all servers with their status"""
    return topology_builder.get_servers_status()


@app.get("/api/server/{server_ip}")
async def get_server_details(server_ip: str):
    """Get detailed metrics for a specific server"""
    server = topology_builder.get_server_by_ip(server_ip)
    if not server:
        raise HTTPException(status_code=404, detail="Server not found")
    
    # Get real-time metrics (simulated for now)
    metrics = {
        "cpu_usage": 45.2,
        "memory_usage": 62.8,
        "disk_usage": 38.5,
        "network_in": 1024 * 512,  # bytes/sec
        "network_out": 1024 * 256,
        "uptime": 86400 * 7,  # seconds
        "services": server.get("services", [])
    }
    
    return {
        **server,
        "metrics": metrics
    }


@app.post("/api/security/attack")
async def execute_attack(attack_config: Dict[str, Any]):
    """
    Execute a security attack/test
    Request body: {
        "type": "port_scan" | "dos_simulation" | "sql_injection" | "network_disruption",
        "target": "172.16.10.20",
        "parameters": {...}
    }
    """
    attack_type = attack_config.get("type")
    target = attack_config.get("target")
    parameters = attack_config.get("parameters", {})
    
    if not attack_type or not target:
        raise HTTPException(status_code=400, detail="Missing attack type or target")
    
    # Execute attack and return job ID
    job_id = await security_engine.execute_attack(attack_type, target, parameters)
    
    return {
        "status": "started",
        "job_id": job_id,
        "attack_type": attack_type,
        "target": target
    }


@app.get("/api/security/attack/{job_id}")
async def get_attack_status(job_id: str):
    """Get status and results of an attack job"""
    result = security_engine.get_attack_result(job_id)
    if not result:
        raise HTTPException(status_code=404, detail="Job not found")
    return result


@app.get("/api/security/history")
async def get_attack_history():
    """Get history of all executed attacks"""
    return security_engine.get_attack_history()


@app.post("/api/scenario/execute")
async def execute_scenario(scenario: Dict[str, Any]):
    """
    Execute a chaos scenario (multiple sequential attacks)
    Request body: {
        "name": "Database Failure Scenario",
        "actions": [
            {"type": "network_disruption", "target": "172.16.20.20", "delay": 0},
            {"type": "service_restart", "target": "172.16.20.20", "service": "mysql", "delay": 10}
        ]
    }
    """
    scenario_id = await security_engine.execute_scenario(scenario)
    return {
        "status": "started",
        "scenario_id": scenario_id,
        "scenario_name": scenario.get("name")
    }


# WebSocket endpoint for real-time network data
@app.websocket("/ws/network")
async def websocket_network(websocket: WebSocket):
    """
    WebSocket endpoint that streams real-time network traffic data
    Messages format: {
        "type": "packet" | "stats" | "alert",
        "data": {...}
    }
    """
    await manager.connect(websocket)
    
    try:
        # Start network monitoring in background
        monitor_task = asyncio.create_task(
            stream_network_data(websocket)
        )
        
        # Listen for client messages (filters, etc.)
        while True:
            data = await websocket.receive_text()
            message = json.loads(data)
            
            # Handle filter updates
            if message.get("type") == "filter":
                filters = message.get("filters", {})
                network_monitor.update_filters(filters)
                await websocket.send_json({
                    "type": "filter_updated",
                    "filters": filters
                })
    
    except WebSocketDisconnect:
        manager.disconnect(websocket)
        monitor_task.cancel()


async def stream_network_data(websocket: WebSocket):
    """Stream network packets and statistics to WebSocket client"""
    while True:
        try:
            # Get latest network data
            packets = network_monitor.get_recent_packets(limit=10)
            stats = network_monitor.get_statistics()
            
            # Send packets
            if packets:
                await websocket.send_json({
                    "type": "packets",
                    "data": packets
                })
            
            # Send statistics every 2 seconds
            await websocket.send_json({
                "type": "stats",
                "data": stats
            })
            
            await asyncio.sleep(0.5)  # 2 updates per second
            
        except Exception as e:
            print(f"Error streaming network data: {e}")
            break


@app.on_event("startup")
async def startup_event():
    """Start background tasks on application startup"""
    # Start network packet capture
    asyncio.create_task(network_monitor.start_capture())
    print("🚀 QuickStay Master Dashboard API started")
    print(f"📡 Monitoring network interface: {config['infrastructure']['network']['interface']}")


@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on application shutdown"""
    network_monitor.stop_capture()
    print("👋 Dashboard API stopped")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="info")
