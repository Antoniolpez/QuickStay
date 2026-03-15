import { useState } from 'react'
import { motion } from 'framer-motion'
import { Shield, Terminal, Play, Loader, CheckCircle, XCircle, History } from 'lucide-react'

interface Attack {
    id: string
    name: string
    type: string
    description: string
    severity: 'low' | 'medium' | 'high'
}

const ATTACKS: Attack[] = [
    {
        id: 'port_scan',
        name: 'Port Scan Completo',
        type: 'port_scan',
        description: 'Escaneo de puertos abiertos usando nmap',
        severity: 'low',
    },
    {
        id: 'dos',
        name: 'Simulación DoS',
        type: 'dos_simulation',
        description: 'Ataque de denegación de servicio controlado',
        severity: 'high',
    },
    {
        id: 'sqli',
        name: 'SQL Injection Test',
        type: 'sql_injection',
        description: 'Prueba de vulnerabilidades SQL injection',
        severity: 'medium',
    },
    {
        id: 'vlan_down',
        name: 'Caída de VLAN DMZ',
        type: 'network_disruption',
        description: 'Simular caída de VLAN DMZ',
        severity: 'high',
    },
    {
        id: 'vuln_scan',
        name: 'Vulnerability Scan',
        type: 'vulnerability_scan',
        description: 'Escaneo completo de vulnerabilidades',
        severity: 'medium',
    },
]

export default function SecurityPanel() {
    const [selectedAttack, setSelectedAttack] = useState<Attack | null>(null)
    const [targetIp, setTargetIp] = useState('172.16.10.20')
    const [parameters, setParameters] = useState<any>({})
    const [consoleOutput, setConsoleOutput] = useState<string[]>([])
    const [activeJob, setActiveJob] = useState<any>(null)
    const [history, setHistory] = useState<any[]>([])
    const [showHistory, setShowHistory] = useState(false)

    const executeAttack = async () => {
        if (!selectedAttack) return

        setConsoleOutput([])
        setConsoleOutput(prev => [...prev, `➜ Ejecutando ${selectedAttack.name}...`])
        setConsoleOutput(prev => [...prev, `➜ Target: ${targetIp}`])

        try {
            const response = await fetch('/api/security/attack', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    type: selectedAttack.type,
                    target: targetIp,
                    parameters,
                }),
            })

            const data = await response.json()
            setActiveJob(data)

            // Poll for results
            pollJobStatus(data.job_id)
        } catch (error) {
            setConsoleOutput(prev => [...prev, `✗ Error: ${error}`])
        }
    }

    const pollJobStatus = async (jobId: string) => {
        const interval = setInterval(async () => {
            try {
                const response = await fetch(`/api/security/attack/${jobId}`)
                const job = await response.json()

                // Update console with new output
                if (job.output && job.output.length > consoleOutput.length - 2) {
                    setConsoleOutput(prev => [
                        ...prev.slice(0, 2),
                        ...job.output,
                    ])
                }

                if (job.status !== 'running') {
                    clearInterval(interval)
                    setActiveJob(null)

                    if (job.status === 'completed') {
                        setConsoleOutput(prev => [
                            ...prev,
                            '',
                            '✓ Ataque completado exitosamente',
                            `Resultados: ${JSON.stringify(job.result, null, 2)}`,
                        ])
                    }

                    // Add to history
                    setHistory(prev => [job, ...prev])
                }
            } catch (error) {
                clearInterval(interval)
                setConsoleOutput(prev => [...prev, `✗ Error polling status: ${error}`])
            }
        }, 1000)
    }

    return (
        <div className="grid grid-cols-3 gap-6">
            {/* Attack Library */}
            <div className="col-span-1 space-y-4">
                <div className="glass rounded-lg p-4">
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="text-lg font-bold flex items-center gap-2">
                            <Shield className="w-5 h-5 text-cyber-purple" />
                            Biblioteca de Ataques
                        </h3>
                    </div>

                    <div className="space-y-2">
                        {ATTACKS.map((attack) => (
                            <motion.button
                                key={attack.id}
                                onClick={() => setSelectedAttack(attack)}
                                whileHover={{ scale: 1.02 }}
                                whileTap={{ scale: 0.98 }}
                                className={`
                  w-full text-left p-4 rounded-lg transition
                  ${selectedAttack?.id === attack.id
                                        ? 'bg-cyber-purple/30 border-2 border-cyber-purple'
                                        : 'bg-dark-elevated hover:bg-dark-elevated/80 border-2 border-transparent'
                                    }
                `}
                            >
                                <div className="flex items-start justify-between">
                                    <div>
                                        <p className="font-semibold">{attack.name}</p>
                                        <p className="text-sm text-gray-400 mt-1">{attack.description}</p>
                                    </div>
                                    <span
                                        className={`
                      text-xs px-2 py-1 rounded
                      ${attack.severity === 'low' ? 'bg-cyber-green/20 text-cyber-green' : ''}
                      ${attack.severity === 'medium' ? 'bg-yellow-500/20 text-yellow-500' : ''}
                      ${attack.severity === 'high' ? 'bg-red-500/20 text-red-500' : ''}
                    `}
                                    >
                                        {attack.severity.toUpperCase()}
                                    </span>
                                </div>
                            </motion.button>
                        ))}
                    </div>
                </div>

                {/* History Button */}
                <button
                    onClick={() => setShowHistory(!showHistory)}
                    className="w-full glass rounded-lg p-4 flex items-center justify-between hover:bg-white/5 transition"
                >
                    <span className="flex items-center gap-2">
                        <History className="w-5 h-5" />
                        Historial de Ataques
                    </span>
                    <span className="text-sm text-gray-400">{history.length}</span>
                </button>
            </div>

            {/* Attack Configuration & Console */}
            <div className="col-span-2 space-y-6">
                {/* Configuration */}
                <div className="glass rounded-lg p-6">
                    <h3 className="text-lg font-bold mb-4">Configuración del Ataque</h3>

                    {selectedAttack ? (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm text-gray-400 mb-2">Ataque Seleccionado</label>
                                <p className="font-semibold text-cyber-purple">{selectedAttack.name}</p>
                            </div>

                            <div>
                                <label className="block text-sm text-gray-400 mb-2">IP Objetivo</label>
                                <input
                                    type="text"
                                    value={targetIp}
                                    onChange={(e) => setTargetIp(e.target.value)}
                                    placeholder="172.16.x.x"
                                    className="w-full bg-dark-elevated text-white rounded px-4 py-2 font-mono"
                                />
                            </div>

                            {selectedAttack.type === 'dos_simulation' && (
                                <div>
                                    <label className="block text-sm text-gray-400 mb-2">Intensidad</label>
                                    <select
                                        onChange={(e) => setParameters({ ...parameters, intensity: e.target.value })}
                                        className="w-full bg-dark-elevated text-white rounded px-4 py-2"
                                    >
                                        <option value="low">Baja</option>
                                        <option value="medium">Media</option>
                                        <option value="high">Alta</option>
                                    </select>
                                </div>
                            )}

                            <motion.button
                                onClick={executeAttack}
                                disabled={!!activeJob}
                                whileHover={{ scale: 1.02 }}
                                whileTap={{ scale: 0.98 }}
                                className={`
                  w-full py-3 rounded-lg font-semibold flex items-center justify-center gap-2
                  ${activeJob
                                        ? 'bg-gray-600 cursor-not-allowed'
                                        : 'bg-gradient-to-r from-cyber-purple to-cyber-pink hover:opacity-90'
                                    }
                `}
                            >
                                {activeJob ? (
                                    <>
                                        <Loader className="w-5 h-5 animate-spin" />
                                        Ejecutando...
                                    </>
                                ) : (
                                    <>
                                        <Play className="w-5 h-5" />
                                        Ejecutar Ataque
                                    </>
                                )}
                            </motion.button>
                        </div>
                    ) : (
                        <p className="text-center text-gray-400 py-8">
                            Selecciona un ataque de la biblioteca
                        </p>
                    )}
                </div>

                {/* Console Output */}
                <div className="glass rounded-lg overflow-hidden">
                    <div className="bg-dark-elevated px-4 py-3 flex items-center gap-2 border-b border-white/10">
                        <Terminal className="w-4 h-4 text-cyber-green" />
                        <span className="font-mono text-sm">Terminal de Salida</span>
                    </div>
                    <div className="p-4 font-mono text-sm h-96 overflow-y-auto bg-black/50">
                        {consoleOutput.length === 0 ? (
                            <p className="text-gray-500">Esperando ejecución de ataque...</p>
                        ) : (
                            consoleOutput.map((line, i) => (
                                <motion.div
                                    key={i}
                                    initial={{ opacity: 0, x: -10 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    className="mb-1"
                                >
                                    {line}
                                </motion.div>
                            ))
                        )}
                    </div>
                </div>

                {/* History Panel */}
                {showHistory && history.length > 0 && (
                    <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        className="glass rounded-lg p-6"
                    >
                        <h3 className="text-lg font-bold mb-4">Historial de Ataques Ejecutados</h3>
                        <div className="space-y-3">
                            {history.slice(0, 5).map((job, i) => (
                                <div key={i} className="bg-dark-elevated rounded-lg p-4">
                                    <div className="flex items-center justify-between mb-2">
                                        <span className="font-semibold">{job.attack_type}</span>
                                        {job.status === 'completed' ? (
                                            <CheckCircle className="w-5 h-5 text-cyber-green" />
                                        ) : (
                                            <XCircle className="w-5 h-5 text-red-500" />
                                        )}
                                    </div>
                                    <p className="text-sm text-gray-400">Target: {job.target}</p>
                                    <p className="text-xs text-gray-500 mt-1">{job.completed_at}</p>
                                </div>
                            ))}
                        </div>
                    </motion.div>
                )}
            </div>
        </div>
    )
}
