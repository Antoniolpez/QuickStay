# QuickStay Master Dashboard v2.0

**Plataforma de Presentación Interactiva de Nivel Enterprise**

Una aplicación web de última generación para presentar y controlar la infraestructura QuickStay en tiempo real, con capacidades de visualización de red, monitorización de tráfico y testing de seguridad.

![Dashboard Preview](https://img.shields.io/badge/Status-Ready-success)
![Version](https://img.shields.io/badge/Version-2.0.0-blue)
![Stack](https://img.shields.io/badge/Stack-React%20%2B%20FastAPI-purple)

---

## 🚀 Características

### 🌐 Visualización de Red en Tiempo Real
- **Mapa topológico interactivo** con nodos (servidores, routers, switches)
- **Animación de paquetes** circulando por los enlaces en tiempo real
- **Filtros avanzados**: protocolo, puerto, VLAN, IP origen/destino
- **Estados visuales dinámicos**: colores según salud del nodo
- **Zoom & Pan**: navegación fluida por toda la infraestructura

### 🔒 Panel de Seguridad y Ataques
- **Catálogo de ataques** con configuración interactiva
- **Consola en vivo** mostrando output de herramientas
- **Historial de ataques** con resultados archivados
- **Tipos de ataque**: Port Scan, DoS, SQL Injection, Network Disruption

### 📊 Monitorización Integrada
- **Dashboards embebidos** (Grafana)
- **Acceso remoto** (Guacamole)
- **Métricas en vivo** por servidor (CPU, RAM, Disco, Red)
- **Lista de servidores** con estado en tiempo real

### 🎨 Diseño Premium
- **Tema Cyberpunk/Tech** con gradientes neón
- **Animaciones fluidas** (60 FPS) con Framer Motion
- **Efectos glassmorphism** en paneles
- **Tipografía**: Inter + JetBrains Mono
- **Responsive design** para proyector/pantallas grandes

---

## 📦 Stack Tecnológico

**Frontend:**
- React 18 + TypeScript
- Cytoscape.js (visualización de grafos)
- D3.js (visualización de datos)
- Framer Motion (animaciones)
- Tailwind CSS
- Socket.io Client (WebSockets)

**Backend:**
- FastAPI (Python)
- WebSockets (datos en tiempo real)
- Scapy (manipulación de paquetes)
- Python-nmap (port scanning)
- Paramiko (SSH para control remoto)

---

## 🛠️ Instalación y Uso

### Opción 1: Con Docker (Recomendado)

```bash
# Clonar/navegar al directorio
cd dashboard-v2

# Levantar servicios
docker-compose up -d --build

# Acceder a:
# Frontend: http://localhost:5173
# Backend API: http://localhost:8000
# API Docs: http://localhost:8000/docs
```

### Opción 2: Desarrollo Local

**Backend:**
```bash
cd backend
python3 -m venv venv
source venv/bin/activate  # En Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

---

## 📸 Capturas de Pantalla

### Red en Tiempo Real
Mapa interactivo con nodos y enlaces, animación de paquetes circulando.

### Panel de Seguridad
Biblioteca de ataques, configuración y consola en vivo.

### Monitorización
Dashboards integrados y métricas de servidores.

### Escenarios de Caos
Constructor visual de secuencias de ataques.

---

## 🎯 Casos de Uso

1. **Presentación de Proyecto**: Mostrar la infraestructura completa en acción
2. **Demos en Vivo**: Ejecutar ataques y mostrar resiliencia del sistema
3. **Testing de Seguridad**: Validar configuraciones de firewall y seguridad
4. **Chaos Engineering**: Crear y ejecutar escenarios de fallo
5. **Monitorización**: Supervisar estado de todos los componentes

---

## 🔐 Seguridad

- Todas las operaciones de ataque están **simuladas** por defecto
- Requiere autenticación para operaciones destructivas (próximamente)
- Los ataques solo se ejecutan contra IPs en lista blanca (config.yaml)
- Logs completos de todas las acciones ejecutadas

---

## 📝 Configuración

Edita `backend/config.yaml` para personalizar:
- Interfaz de red a monitorizar
- Lista de servidores y VLANs
- URLs de Grafana/Guacamole
- Rutas SSH y scripts de ataque permitidos

---

## 🤝 Contribución

Este proyecto es parte del **Proyecto Integrado - 2º ASIR** de Antonio López Montes.

---

## 📄 Licencia

© 2025/2026 Antonio López Montes - Proyecto QuickStay

---

## 🆘 Solución de Problemas

**El WebSocket no conecta:**
- Verifica que el backend esté corriendo en puerto 8000
- Revisa la configuración de proxy en `vite.config.ts`

**La captura de paquetes no funciona:**
- Por defecto usa modo simulado (seguro)
- Para captura real, ejecutar backend con sudo y configurar tcpdump

**Las animaciones van lentas:**
- Reduce el número de paquetes por segundo en `network_monitor.py`
- Desactiva filtros complejos

---

**Creado con ❤️ para revolucionar la presentación de infraestructuras**
