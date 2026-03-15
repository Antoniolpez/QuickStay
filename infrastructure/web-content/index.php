<?php
// Detección simple del estado del servidor Java y otros servicios
$javaHost = getenv('APP_SERVER_HOST') ?: '172.16.20.10';
$javaPort = intval(getenv('APP_SERVER_PORT') ?: 1234);

function check_tcp($host, $port, $timeout = 1.5) {
    $start = microtime(true);
    $fp = @fsockopen($host, $port, $errno, $errstr, $timeout);
    if (!$fp) {
        return [false, null];
    }
    $latency = round((microtime(true) - $start) * 1000);
    fclose($fp);
    return [true, $latency];
}

[$javaUp, $javaLatency] = check_tcp($javaHost, $javaPort);
[$dbUp, $dbLatency]     = check_tcp('172.16.20.20', 3306);
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QuickStay - Plataforma de Alquiler Express</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: radial-gradient(circle at top left, #764ba2 0%, #667eea 40%, #111827 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            color: #111827;
        }
        .shell {
            width: 100%;
            max-width: 1100px;
        }
        .card {
            background: #ffffff;
            border-radius: 20px;
            box-shadow: 0 24px 80px rgba(15,23,42,0.45);
            padding: 2.5rem 3rem;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
            gap: 1rem;
        }
        .brand {
            display: flex;
            align-items: center;
            gap: 1rem;
        }
        .brand-logo {
            width: 48px;
            height: 48px;
            border-radius: 16px;
            background: linear-gradient(135deg, #4f46e5, #ec4899);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #ffffff;
            font-weight: 700;
            font-size: 1.4rem;
        }
        .brand-title {
            font-size: 1.8rem;
            font-weight: 700;
            color: #111827;
        }
        .brand-subtitle {
            font-size: 0.95rem;
            color: #6b7280;
        }
        .badge-env {
            padding: 0.35rem 0.85rem;
            border-radius: 999px;
            font-size: 0.8rem;
            font-weight: 600;
            background: #eff6ff;
            color: #1d4ed8;
            border: 1px solid #bfdbfe;
        }
        .layout {
            display: grid;
            grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
            gap: 2.25rem;
        }
        .hero-title {
            font-size: 1.9rem;
            font-weight: 700;
            color: #111827;
            margin-bottom: 0.75rem;
        }
        .hero-text {
            font-size: 0.98rem;
            color: #4b5563;
            line-height: 1.7;
            margin-bottom: 1.5rem;
        }
        .pill-row {
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            margin-bottom: 1.75rem;
        }
        .pill {
            padding: 0.35rem 0.75rem;
            border-radius: 999px;
            font-size: 0.78rem;
            background: #f3f4ff;
            color: #4338ca;
            border: 1px solid #e5e7eb;
        }
        .cta-row {
            display: flex;
            flex-wrap: wrap;
            gap: 0.75rem;
            margin-bottom: 1.75rem;
        }
        .btn-primary {
            padding: 0.7rem 1.4rem;
            border-radius: 999px;
            border: none;
            background: linear-gradient(135deg, #4f46e5, #6366f1);
            color: #ffffff;
            font-size: 0.9rem;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
        }
        .btn-secondary {
            padding: 0.7rem 1.2rem;
            border-radius: 999px;
            border: 1px solid #e5e7eb;
            background: #f9fafb;
            color: #374151;
            font-size: 0.9rem;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
        }
        .status-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 0.7rem;
            margin-bottom: 1.75rem;
        }
        .status-card {
            padding: 0.8rem 0.9rem;
            border-radius: 12px;
            border: 1px solid #e5e7eb;
            background: #f9fafb;
            font-size: 0.82rem;
        }
        .status-label {
            font-weight: 600;
            color: #4b5563;
            margin-bottom: 0.25rem;
        }
        .status-ok {
            color: #15803d;
        }
        .status-bad {
            color: #b91c1c;
        }
        .status-sub {
            color: #6b7280;
            font-size: 0.78rem;
        }
        .feature-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 0.9rem;
            font-size: 0.86rem;
        }
        .feature-card {
            padding: 0.85rem 0.9rem;
            border-radius: 12px;
            border: 1px solid #e5e7eb;
            background: #f9fafb;
        }
        .feature-title {
            font-weight: 600;
            color: #111827;
            margin-bottom: 0.25rem;
        }
        .feature-desc {
            color: #6b7280;
        }
        .sidebar {
            display: flex;
            flex-direction: column;
            gap: 1rem;
        }
        .info-box {
            padding: 0.9rem 1rem;
            border-radius: 14px;
            border: 1px solid #e5e7eb;
            background: #f9fafb;
            font-size: 0.82rem;
        }
        .info-box strong {
            display: block;
            color: #111827;
            margin-bottom: 0.2rem;
        }
        .info-muted {
            color: #6b7280;
        }
        .footer {
            margin-top: 2rem;
            padding-top: 1rem;
            border-top: 1px solid #e5e7eb;
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            font-size: 0.8rem;
            color: #9ca3af;
        }
        @media (max-width: 900px) {
            .card { padding: 1.8rem 1.5rem; }
            .layout { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
<div class="shell">
    <div class="card">
        <div class="header">
            <div class="brand">
                <div class="brand-logo">QS</div>
                <div>
                    <div class="brand-title">QuickStay</div>
                    <div class="brand-subtitle">Plataforma de alquiler express conectada a la app de escritorio</div>
                </div>
            </div>
            <div class="badge-env">Front Web · DMZ</div>
        </div>

        <div class="layout">
            <div>
                <h2 class="hero-title">Gestión unificada de reservas, propiedades y clientes</h2>
                <p class="hero-text">
                    Esta página web está pensada como punto de entrada ligero a la infraestructura QuickStay.
                    Muestra el estado del servidor Java y de la base de datos, y resume los módulos que
                    ofrece la aplicación de escritorio conectada al backend.
                </p>

                <div class="pill-row">
                    <span class="pill">Reservas en tiempo real</span>
                    <span class="pill">Gestión de propiedades</span>
                    <span class="pill">Clientes y facturación</span>
                    <span class="pill">Mensajería y soporte</span>
                </div>

                <div class="cta-row">
                    <a href="app.php" class="btn-primary">
                        Abrir app web QuickStay
                    </a>
                    <a href="#" class="btn-secondary" onclick="alert('La versión original de escritorio sigue disponible desde la red interna, pero ahora también puedes usar la app web.'); return false;">
                        Info app de escritorio
                    </a>
                </div>

                <div class="status-grid">
                    <div class="status-card">
                        <div class="status-label">Servidor de aplicación Java</div>
                        <div class="<?php echo $javaUp ? 'status-ok' : 'status-bad'; ?>">
                            <?php echo $javaUp ? 'Online · puerto '.$javaPort : 'Offline'; ?>
                        </div>
                        <div class="status-sub">
                            Host: <?php echo htmlspecialchars($javaHost); ?>
                            <?php if ($javaUp && $javaLatency !== null): ?>· ~<?php echo $javaLatency; ?> ms
                            <?php endif; ?>
                        </div>
                    </div>
                    <div class="status-card">
                        <div class="status-label">Base de datos MySQL</div>
                        <div class="<?php echo $dbUp ? 'status-ok' : 'status-bad'; ?>">
                            <?php echo $dbUp ? 'Online · puerto 3306' : 'Offline'; ?>
                        </div>
                        <div class="status-sub">Host: 172.16.20.20</div>
                    </div>
                    <div class="status-card">
                        <div class="status-label">Estado servidor web</div>
                        <div class="status-ok">Online</div>
                        <div class="status-sub">Host: <?php echo gethostname(); ?></div>
                    </div>
                </div>

                <div id="servicios" class="feature-grid">
                    <div class="feature-card">
                        <div class="feature-title">Reservas y disponibilidades</div>
                        <div class="feature-desc">Alta, modificación y cancelación de reservas, cálculo de
                            disponibilidad y control de calendario centralizado.</div>
                    </div>
                    <div class="feature-card">
                        <div class="feature-title">Gestión de propiedades</div>
                        <div class="feature-desc">Fichas de propiedades, fotos, descripciones, precios y
                            estado de publicación sincronizados con el servidor Java.</div>
                    </div>
                    <div class="feature-card">
                        <div class="feature-title">Clientes y facturación</div>
                        <div class="feature-desc">Datos de clientes, métodos de pago, generación de facturas
                            y trazabilidad económica de cada estancia.</div>
                    </div>
                    <div class="feature-card">
                        <div class="feature-title">Mensajería y soporte</div>
                        <div class="feature-desc">Intercambio de mensajes entre cliente y recepción, registro
                            de incidencias y seguimiento desde la app de escritorio.</div>
                    </div>
                </div>
            </div>

            <div class="sidebar">
                <div class="info-box">
                    <strong>Información del servidor web</strong>
                    <div class="info-muted">
                        IP: <?php echo $_SERVER['SERVER_ADDR'] ?? 'desconocida'; ?><br>
                        Fecha/Hora: <?php echo date('d/m/Y H:i:s'); ?><br>
                        PHP: <?php echo phpversion(); ?>
                    </div>
                </div>
                <div class="info-box">
                    <strong>Acceso administración (requiere VPN)</strong>
                    <div class="info-muted">
                        Zabbix: http://172.16.30.102:8080<br>
                        Grafana: http://172.16.30.21:3000<br>
                        Dashboard: http://172.16.30.40:8000
                    </div>
                </div>
                <div class="info-box">
                    <strong>Cómo usar esta plataforma</strong>
                    <div class="info-muted">
                        1. Conecta la VPN de administradores.<br>
                        2. Abre la app de escritorio QuickStay.<br>
                        3. Verifica aquí que el servidor Java y MySQL están en verde.<br>
                        4. Gestiona reservas, propiedades y clientes desde la app.
                    </div>
                </div>
            </div>
        </div>

        <div class="footer">
            <div>© <?php echo date('Y'); ?> QuickStay · Proyecto ASIR · Antonio López Montes</div>
            <div>Entorno: Infraestructura Docker · DMZ / App / DB / VPN</div>
        </div>
    </div>
</div>
</body>
</html>
