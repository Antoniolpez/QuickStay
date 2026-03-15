import { useState } from 'react'
import { motion } from 'framer-motion'
import { Plus, Play, Trash2, Save } from 'lucide-react'

interface Action {
    id: string
    type: string
    target: string
    delay: number
    parameters?: any
}

export default function ScenarioBuilder() {
    const [scenarioName, setScenarioName] = useState('')
    const [actions, setActions] = useState<Action[]>([])
    const [executing, setExecuting] = useState(false)

    const addAction = () => {
        setActions([
            ...actions,
            {
                id: Date.now().toString(),
                type: 'port_scan',
                target: '172.16.10.20',
                delay: 0,
            },
        ])
    }

    const removeAction = (id: string) => {
        setActions(actions.filter((a) => a.id !== id))
    }

    const updateAction = (id: string, field: string, value: any) => {
        setActions(
            actions.map((a) => (a.id === id ? { ...a, [field]: value } : a))
        )
    }

    const executeScenario = async () => {
        if (actions.length === 0) return

        setExecuting(true)

        try {
            const response = await fetch('/api/scenario/execute', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: scenarioName || 'Unnamed Scenario',
                    actions: actions.map((a) => ({
                        type: a.type,
                        target: a.target,
                        delay: a.delay,
                        parameters: a.parameters || {},
                    })),
                }),
            })

            const data = await response.json()
            alert(`Escenario iniciado: ${data.scenario_id}`)
        } catch (error) {
            alert('Error ejecutando escenario')
        } finally {
            setExecuting(false)
        }
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="glass rounded-lg p-6">
                <h3 className="text-2xl font-bold mb-4">Constructor de Escenarios de Caos</h3>
                <p className="text-gray-400">
                    Crea secuencias de acciones para simular fallos complejos en la infraestructura
                </p>
            </div>

            {/* Scenario Configuration */}
            <div className="glass rounded-lg p-6">
                <div className="mb-4">
                    <label className="block text-sm text-gray-400 mb-2">Nombre del Escenario</label>
                    <input
                        type="text"
                        value={scenarioName}
                        onChange={(e) => setScenarioName(e.target.value)}
                        placeholder="Ej: Fallo Completo de Base de Datos"
                        className="w-full bg-dark-elevated text-white rounded px-4 py-2"
                    />
                </div>

                {/* Actions Timeline */}
                <div className="space-y-4">
                    <div className="flex items-center justify-between">
                        <h4 className="font-semibold">Acciones ({actions.length})</h4>
                        <button
                            onClick={addAction}
                            className="flex items-center gap-2 px-4 py-2 bg-cyber-purple/20 hover:bg-cyber-purple/30 rounded-lg transition"
                        >
                            <Plus className="w-4 h-4" />
                            Añadir Acción
                        </button>
                    </div>

                    {actions.length === 0 ? (
                        <div className="text-center py-12 text-gray-500">
                            <p>No hay acciones. Añade una para empezar.</p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {actions.map((action, index) => (
                                <motion.div
                                    key={action.id}
                                    initial={{ opacity: 0, x: -20 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    className="bg-dark-elevated rounded-lg p-4"
                                >
                                    <div className="flex items-start gap-4">
                                        <div className="flex items-center justify-center w-8 h-8 rounded-full bg-cyber-purple/20 text-cyber-purple font-bold">
                                            {index + 1}
                                        </div>

                                        <div className="flex-1 grid grid-cols-3 gap-4">
                                            <div>
                                                <label className="block text-xs text-gray-400 mb-1">Tipo de Ataque</label>
                                                <select
                                                    value={action.type}
                                                    onChange={(e) => updateAction(action.id, 'type', e.target.value)}
                                                    className="w-full bg-dark-bg text-white rounded px-3 py-2 text-sm"
                                                >
                                                    <option value="port_scan">Port Scan</option>
                                                    <option value="dos_simulation">DoS Simulation</option>
                                                    <option value="sql_injection">SQL Injection</option>
                                                    <option value="network_disruption">Network Disruption</option>
                                                    <option value="vulnerability_scan">Vulnerability Scan</option>
                                                </select>
                                            </div>

                                            <div>
                                                <label className="block text-xs text-gray-400 mb-1">IP Objetivo</label>
                                                <input
                                                    type="text"
                                                    value={action.target}
                                                    onChange={(e) => updateAction(action.id, 'target', e.target.value)}
                                                    className="w-full bg-dark-bg text-white rounded px-3 py-2 text-sm font-mono"
                                                />
                                            </div>

                                            <div>
                                                <label className="block text-xs text-gray-400 mb-1">Delay (segundos)</label>
                                                <input
                                                    type="number"
                                                    value={action.delay}
                                                    onChange={(e) =>
                                                        updateAction(action.id, 'delay', parseInt(e.target.value) || 0)
                                                    }
                                                    className="w-full bg-dark-bg text-white rounded px-3 py-2 text-sm"
                                                />
                                            </div>
                                        </div>

                                        <button
                                            onClick={() => removeAction(action.id)}
                                            className="p-2 hover:bg-red-500/20 text-red-500 rounded transition"
                                        >
                                            <Trash2 className="w-4 h-4" />
                                        </button>
                                    </div>
                                </motion.div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Actions */}
                <div className="mt-6 flex gap-4">
                    <motion.button
                        onClick={executeScenario}
                        disabled={executing || actions.length === 0}
                        whileHover={{ scale: 1.02 }}
                        whileTap={{ scale: 0.98 }}
                        className={`
              flex-1 py-3 rounded-lg font-semibold flex items-center justify-center gap-2
              ${executing || actions.length === 0
                                ? 'bg-gray-600 cursor-not-allowed'
                                : 'bg-gradient-to-r from-cyber-purple to-cyber-pink hover:opacity-90'
                            }
            `}
                    >
                        <Play className="w-5 h-5" />
                        {executing ? 'Ejecutando...' : 'Ejecutar Escenario'}
                    </motion.button>

                    <button
                        disabled={actions.length === 0}
                        className="px-6 py-3 bg-dark-elevated hover:bg-white/10 rounded-lg flex items-center gap-2 transition disabled:opacity-50"
                    >
                        <Save className="w-5 h-5" />
                        Guardar
                    </button>
                </div>
            </div>

            {/* Predefined Scenarios */}
            <div className="glass rounded-lg p-6">
                <h4 className="font-semibold mb-4">Escenarios Predefinidos</h4>
                <div className="grid grid-cols-2 gap-4">
                    {[
                        {
                            name: 'Caída Completa DMZ',
                            description: 'Simula caída de toda la VLAN DMZ',
                            actions: 2,
                        },
                        {
                            name: 'Ataque DDoS Coordinado',
                            description: 'DoS en múltiples servicios simultáneamente',
                            actions: 4,
                        },
                        {
                            name: 'Fallo de Base de Datos',
                            description: 'Corrupción de BD + reinicio de servicios',
                            actions: 3,
                        },
                        {
                            name: 'Test de Resiliencia Completo',
                            description: 'Prueba todos los sistemas de failover',
                            actions: 8,
                        },
                    ].map((scenario, i) => (
                        <button
                            key={i}
                            className="text-left bg-dark-elevated hover:bg-dark-elevated/80 rounded-lg p-4 transition"
                        >
                            <p className="font-semibold mb-1">{scenario.name}</p>
                            <p className="text-sm text-gray-400 mb-2">{scenario.description}</p>
                            <span className="text-xs text-cyber-purple">{scenario.actions} acciones</span>
                        </button>
                    ))}
                </div>
            </div>
        </div>
    )
}
