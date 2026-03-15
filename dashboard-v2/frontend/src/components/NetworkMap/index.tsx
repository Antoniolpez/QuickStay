import { useEffect, useRef, useState } from 'react'
import { motion } from 'framer-motion'
import cytoscape from 'cytoscape'
import dagre from 'cytoscape-dagre'
import { Filter, ZoomIn, ZoomOut, Maximize2 } from 'lucide-react'

// Register dagre layout
cytoscape.use(dagre)

interface NetworkFilters {
    protocol: string[]
    port: number[]
    vlan: number[]
    srcIp: string
    dstIp: string
}

export default function NetworkMap() {
    const cyRef = useRef<HTMLDivElement>(null)
    const [cy, setCy] = useState<cytoscape.Core | null>(null)
    const [selectedNode, setSelectedNode] = useState<any>(null)
    const [filters, setFilters] = useState<NetworkFilters>({
        protocol: [],
        port: [],
        vlan: [],
        srcIp: '',
        dstIp: '',
    })
    const [showFilters, setShowFilters] = useState(false)
    const [stats, setStats] = useState({
        totalPackets: 0,
        packetsPerSec: 0,
        bytesPerSec: 0,
    })

    useEffect(() => {
        if (!cyRef.current) return

        // Fetch topology from backend
        fetch('/api/topology')
            .then(res => res.json())
            .then(data => {
                initializeCytoscape(data)
            })
            .catch(err => {
                console.error('Error loading topology:', err)
                // Use demo data
                initializeCytoscape(getDemoTopology())
            })

        // WebSocket for real-time packets
        const ws = new WebSocket('ws://localhost:8000/ws/network')

        ws.onmessage = (event) => {
            const message = JSON.parse(event.data)

            if (message.type === 'packets') {
                animatePackets(message.data)
            } else if (message.type === 'stats') {
                setStats(message.data)
            }
        }

        ws.onerror = () => {
            console.log('WebSocket connection failed, using demo mode')
        }

        return () => {
            ws.close()
        }
    }, [])

    const initializeCytoscape = (data: any) => {
        const cyInstance = cytoscape({
            container: cyRef.current,
            elements: [
                ...data.nodes.map((node: any) => ({
                    data: { ...node },
                })),
                ...data.edges.map((edge: any) => ({
                    data: { ...edge },
                })),
            ],
            style: [
                {
                    selector: 'node',
                    style: {
                        'label': 'data(label)',
                        'text-valign': 'center',
                        'text-halign': 'center',
                        'background-color': (ele: any) => getNodeColor(ele.data('status')),
                        'border-width': 2,
                        'border-color': (ele: any) => getNodeBorderColor(ele.data('type')),
                        'width': 60,
                        'height': 60,
                        'font-size': 12,
                        'color': '#ffffff',
                        'text-outline-width': 2,
                        'text-outline-color': '#0a0a0f',
                    },
                },
                {
                    selector: 'node[type="router"]',
                    style: {
                        'shape': 'diamond',
                        'width': 80,
                        'height': 80,
                    },
                },
                {
                    selector: 'node[type="vlan"]',
                    style: {
                        'shape': 'roundrectangle',
                        'width': 120,
                        'height': 50,
                        'background-color': '#1a1a24',
                        'border-style': 'dashed',
                    },
                },
                {
                    selector: 'edge',
                    style: {
                        'width': 3,
                        'line-color': '#3b82f6',
                        'target-arrow-color': '#3b82f6',
                        'target-arrow-shape': 'triangle',
                        'curve-style': 'bezier',
                        'opacity': 0.7,
                    },
                },
                {
                    selector: '.animated-packet',
                    style: {
                        'line-color': '#a855f7',
                        'width': 5,
                        'opacity': 1,
                    },
                },
            ],
            layout: {
                name: 'dagre',
                rankDir: 'TB',
                nodeSep: 50,
                rankSep: 100,
            },
        })

        // Node click handler
        cyInstance.on('tap', 'node', (event) => {
            const node = event.target
            setSelectedNode(node.data())

            // Fetch detailed metrics
            if (node.data('ip')) {
                fetch(`/api/server/${node.data('ip')}`)
                    .then(res => res.json())
                    .then(data => setSelectedNode(data))
                    .catch(err => console.error(err))
            }
        })

        setCy(cyInstance)
    }

    const getNodeColor = (status: string) => {
        switch (status) {
            case 'online': return '#10b981'
            case 'warning': return '#f59e0b'
            case 'offline': return '#ef4444'
            default: return '#6b7280'
        }
    }

    const getNodeBorderColor = (type: string) => {
        switch (type) {
            case 'router': return '#a855f7'
            case 'load_balancer': return '#ec4899'
            case 'web': return '#3b82f6'
            case 'database': return '#10b981'
            case 'security': return '#ef4444'
            default: return '#6b7280'
        }
    }

    const animatePackets = (packets: any[]) => {
        if (!cy) return

        packets.forEach((packet) => {
            const edge = cy.edges().filter((e: any) => {
                const src = e.source().data('ip')
                const dst = e.target().data('ip')
                return src === packet.src_ip && dst === packet.dst_ip
            })

            if (edge.length > 0) {
                edge.addClass('animated-packet')
                setTimeout(() => {
                    edge.removeClass('animated-packet')
                }, 500)
            }
        })
    }

    const getDemoTopology = () => {
        return {
            nodes: [
                { id: 'router', type: 'router', label: 'Router', status: 'online' },
                { id: '172.16.10.10', type: 'load_balancer', label: 'Load Balancer', ip: '172.16.10.10', status: 'online', vlan: 10 },
                { id: '172.16.10.20', type: 'web', label: 'Web 1', ip: '172.16.10.20', status: 'online', vlan: 10 },
                { id: '172.16.20.10', type: 'application', label: 'App Server', ip: '172.16.20.10', status: 'online', vlan: 20 },
                { id: '172.16.20.20', type: 'database', label: 'MySQL', ip: '172.16.20.20', status: 'online', vlan: 20 },
            ],
            edges: [
                { id: 'e1', source: 'router', target: '172.16.10.10' },
                { id: 'e2', source: '172.16.10.10', target: '172.16.10.20' },
                { id: 'e3', source: '172.16.10.20', target: '172.16.20.10' },
                { id: 'e4', source: '172.16.20.10', target: '172.16.20.20' },
            ],
        }
    }

    return (
        <div className="space-y-6">
            {/* Controls */}
            <div className="glass rounded-lg p-4 flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button
                        onClick={() => setShowFilters(!showFilters)}
                        className="flex items-center gap-2 px-4 py-2 bg-cyber-purple/20 hover:bg-cyber-purple/30 rounded-lg transition"
                    >
                        <Filter className="w-4 h-4" />
                        <span>Filtros</span>
                    </button>

                    {/* Stats */}
                    <div className="flex items-center gap-6 text-sm">
                        <div>
                            <span className="text-gray-400">Paquetes totales:</span>
                            <span className="ml-2 font-mono text-cyber-green">{stats.totalPackets}</span>
                        </div>
                        <div>
                            <span className="text-gray-400">Paquetes/s:</span>
                            <span className="ml-2 font-mono text-cyber-blue">{stats.packetsPerSec.toFixed(1)}</span>
                        </div>
                    </div>
                </div>

                <div className="flex items-center gap-2">
                    <button
                        onClick={() => cy?.zoom(cy.zoom() + 0.1)}
                        className="p-2 hover:bg-white/10 rounded"
                    >
                        <ZoomIn className="w-4 h-4" />
                    </button>
                    <button
                        onClick={() => cy?.zoom(cy.zoom() - 0.1)}
                        className="p-2 hover:bg-white/10 rounded"
                    >
                        <ZoomOut className="w-4 h-4" />
                    </button>
                    <button
                        onClick={() => cy?.fit()}
                        className="p-2 hover:bg-white/10 rounded"
                    >
                        <Maximize2 className="w-4 h-4" />
                    </button>
                </div>
            </div>

            {/* Filters Panel */}
            {showFilters && (
                <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    className="glass rounded-lg p-6"
                >
                    <h3 className="text-lg font-semibold mb-4">Filtros de Tráfico</h3>
                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-sm text-gray-400 mb-2">Protocolo</label>
                            <select className="w-full bg-dark-elevated text-white rounded px-3 py-2">
                                <option>Todos</option>
                                <option>HTTP</option>
                                <option>HTTPS</option>
                                <option>SSH</option>
                                <option>MYSQL</option>
                                <option>MQTT</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm text-gray-400 mb-2">Puerto</label>
                            <input
                                type="number"
                                placeholder="80, 443, 22..."
                                className="w-full bg-dark-elevated text-white rounded px-3 py-2"
                            />
                        </div>
                        <div>
                            <label className="block text-sm text-gray-400 mb-2">VLAN</label>
                            <select className="w-full bg-dark-elevated text-white rounded px-3 py-2">
                                <option>Todas</option>
                                <option>DMZ (10)</option>
                                <option>App (20)</option>
                                <option>Mgmt (30)</option>
                                <option>IoT (40)</option>
                            </select>
                        </div>
                    </div>
                </motion.div>
            )}

            {/* Network Visualization */}
            <div className="glass rounded-lg overflow-hidden">
                <div ref={cyRef} className="w-full h-[600px] bg-dark-bg" />
            </div>

            {/* Node Details Panel */}
            {selectedNode && (
                <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="glass rounded-lg p-6"
                >
                    <h3 className="text-xl font-bold mb-4">{selectedNode.label || selectedNode.name}</h3>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <p className="text-sm text-gray-400">IP Address</p>
                            <p className="font-mono text-cyber-blue">{selectedNode.ip}</p>
                        </div>
                        <div>
                            <p className="text-sm text-gray-400">Estado</p>
                            <p className="font-semibold text-cyber-green">{selectedNode.status}</p>
                        </div>
                        {selectedNode.metrics && (
                            <>
                                <div>
                                    <p className="text-sm text-gray-400">CPU</p>
                                    <p className="font-mono">{selectedNode.metrics.cpu_usage?.toFixed(1)}%</p>
                                </div>
                                <div>
                                    <p className="text-sm text-gray-400">Memoria</p>
                                    <p className="font-mono">{selectedNode.metrics.memory_usage?.toFixed(1)}%</p>
                                </div>
                            </>
                        )}
                    </div>
                </motion.div>
            )}
        </div>
    )
}
