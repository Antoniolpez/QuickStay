import { motion } from 'framer-motion'
import { Activity, Server, Database as DbIcon } from 'lucide-react'

export default function MonitoringDashboard() {
    return (
        <div className="space-y-6">
            {/* Quick Stats */}
            <div className="grid grid-cols-4 gap-4">
                {[
                    { label: 'Servidores Online', value: '9/10', icon: Server, color: 'cyber-green' },
                    { label: 'CPU Promedio', value: '45%', icon: Activity, color: 'cyber-blue' },
                    { label: 'Memoria Usada', value: '62%', icon: Activity, color: 'cyber-purple' },
                    { label: 'Bases de Datos', value: '2', icon: DbIcon, color: 'cyber-pink' },
                ].map((stat, i) => (
                    <motion.div
                        key={i}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: i * 0.1 }}
                        className="glass rounded-lg p-6"
                    >
                        <div className="flex items-center justify-between mb-2">
                            <span className="text-sm text-gray-400">{stat.label}</span>
                            <stat.icon className={`w-5 h-5 text-${stat.color}`} />
                        </div>
                        <p className="text-3xl font-bold">{stat.value}</p>
                    </motion.div>
                ))}
            </div>

            {/* Grafana Embeds */}
            <div className="grid grid-cols-2 gap-6">
                <div className="glass rounded-lg overflow-hidden">
                    <div className="bg-dark-elevated px-4 py-3 border-b border-white/10">
                        <h3 className="font-semibold">Grafana - System Metrics</h3>
                    </div>
                    <div className="aspect-video bg-dark-bg flex items-center justify-center">
                        <iframe
                            src="http://localhost:3000/d/system-metrics"
                            className="w-full h-full"
                            title="Grafana System Metrics"
                        />
                        <div className="absolute text-gray-500">
                            <p>Grafana Dashboard</p>
                            <p className="text-sm">http://localhost:3000</p>
                        </div>
                    </div>
                </div>

                <div className="glass rounded-lg overflow-hidden">
                    <div className="bg-dark-elevated px-4 py-3 border-b border-white/10">
                        <h3 className="font-semibold">Guacamole - Remote Access</h3>
                    </div>
                    <div className="aspect-video bg-dark-bg flex items-center justify-center">
                        <iframe
                            src="http://localhost:8080/guacamole"
                            className="w-full h-full"
                            title="Guacamole Remote Access"
                        />
                        <div className="absolute text-gray-500">
                            <p>Guacamole Access</p>
                            <p className="text-sm">http://localhost:8080</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Server List */}
            <div className="glass rounded-lg p-6">
                <h3 className="text-lg font-bold mb-4">Estado de Servidores</h3>
                <div className="space-y-3">
                    {[
                        { name: 'Load Balancer', ip: '172.16.10.10', status: 'online', cpu: 34, mem: 45 },
                        { name: 'Web Server 1', ip: '172.16.10.20', status: 'online', cpu: 52, mem: 38 },
                        { name: 'App Server', ip: '172.16.20.10', status: 'online', cpu: 68, mem: 72 },
                        { name: 'MySQL Primary', ip: '172.16.20.20', status: 'online', cpu: 45, mem: 81 },
                        { name: 'Zabbix Server', ip: '172.16.30.20', status: 'warning', cpu: 23, mem: 56 },
                    ].map((server, i) => (
                        <div key={i} className="bg-dark-elevated rounded-lg p-4 flex items-center justify-between">
                            <div className="flex items-center gap-4">
                                <div
                                    className={`w-3 h-3 rounded-full ${server.status === 'online' ? 'bg-cyber-green' : 'bg-yellow-500'
                                        } animate-pulse`}
                                />
                                <div>
                                    <p className="font-semibold">{server.name}</p>
                                    <p className="text-sm text-gray-400 font-mono">{server.ip}</p>
                                </div>
                            </div>
                            <div className="flex items-center gap-6 text-sm">
                                <div>
                                    <span className="text-gray-400">CPU:</span>
                                    <span className="ml-2 font-mono">{server.cpu}%</span>
                                </div>
                                <div>
                                    <span className="text-gray-400">MEM:</span>
                                    <span className="ml-2 font-mono">{server.mem}%</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
