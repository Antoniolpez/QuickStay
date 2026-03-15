import { useState } from 'react'
import { motion } from 'framer-motion'
import { Activity, Shield, Network, Settings, Database } from 'lucide-react'
import NetworkMap from './components/NetworkMap'
import SecurityPanel from './components/SecurityPanel'
import MonitoringDashboard from './components/MonitoringDashboard'
import ScenarioBuilder from './components/ScenarioBuilder'

type Tab = 'network' | 'security' | 'monitoring' | 'scenarios'

function App() {
    const [activeTab, setActiveTab] = useState<Tab>('network')

    const tabs = [
        { id: 'network' as Tab, label: 'Red en Tiempo Real', icon: Network },
        { id: 'security' as Tab, label: 'Seguridad & Ataques', icon: Shield },
        { id: 'monitoring' as Tab, label: 'Monitorización', icon: Activity },
        { id: 'scenarios' as Tab, label: 'Escenarios de Caos', icon: Database },
    ]

    return (
        <div className="min-h-screen bg-dark-bg cyber-grid">
            {/* Header */}
            <header className="glass border-b border-cyber-purple/30">
                <div className="container mx-auto px-6 py-4">
                    <div className="flex items-center justify-between">
                        <motion.div
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            className="flex items-center gap-4"
                        >
                            <div className="w-12 h-12 bg-gradient-to-br from-cyber-purple to-cyber-pink rounded-lg flex items-center justify-center glow">
                                <Settings className="w-6 h-6 animate-spin-slow" />
                            </div>
                            <div>
                                <h1 className="text-2xl font-bold bg-gradient-to-r from-cyber-purple to-cyber-pink bg-clip-text text-transparent">
                                    QuickStay Master Dashboard
                                </h1>
                                <p className="text-sm text-gray-400">Plataforma de Presentación Interactiva v2.0</p>
                            </div>
                        </motion.div>

                        <div className="flex items-center gap-4">
                            <div className="flex items-center gap-2">
                                <div className="w-2 h-2 bg-cyber-green rounded-full animate-pulse"></div>
                                <span className="text-sm text-gray-400">Infraestructura Activa</span>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* Tab Navigation */}
            <nav className="glass border-b border-white/10">
                <div className="container mx-auto px-6">
                    <div className="flex gap-2">
                        {tabs.map((tab) => {
                            const Icon = tab.icon
                            const isActive = activeTab === tab.id

                            return (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`
                    relative px-6 py-4 flex items-center gap-2 transition-all
                    ${isActive
                                            ? 'text-white'
                                            : 'text-gray-400 hover:text-white'
                                        }
                  `}
                                >
                                    <Icon className="w-5 h-5" />
                                    <span className="font-medium">{tab.label}</span>

                                    {isActive && (
                                        <motion.div
                                            layoutId="activeTab"
                                            className="absolute bottom-0 left-0 right-0 h-0.5 bg-gradient-to-r from-cyber-purple to-cyber-pink"
                                            transition={{ type: 'spring', stiffness: 300, damping: 30 }}
                                        />
                                    )}
                                </button>
                            )
                        })}
                    </div>
                </div>
            </nav>

            {/* Main Content */}
            <main className="container mx-auto px-6 py-8">
                <motion.div
                    key={activeTab}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -20 }}
                    transition={{ duration: 0.3 }}
                >
                    {activeTab === 'network' && <NetworkMap />}
                    {activeTab === 'security' && <SecurityPanel />}
                    {activeTab === 'monitoring' && <MonitoringDashboard />}
                    {activeTab === 'scenarios' && <ScenarioBuilder />}
                </motion.div>
            </main>
        </div>
    )
}

export default App
