<?php
session_start();
require __DIR__ . '/db.php';

$db = qs_get_db();

function current_user(): ?array
{
    return $_SESSION['qs_user'] ?? null;
}

function require_login(): void
{
    if (!current_user()) {
        header('Location: app.php?action=login');
        exit;
    }
}

$action = $_GET['action'] ?? 'home';

// Manejo de formularios
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if ($action === 'login') {
        $usuario = trim($_POST['usuario'] ?? '');
        $password = trim($_POST['password'] ?? '');

        if ($usuario !== '' && $password !== '') {
            $stmt = $db->prepare('SELECT * FROM usuario WHERE usuario = ? AND password = ?');
            $stmt->execute([$usuario, $password]);
            $user = $stmt->fetch();
            if ($user) {
                $_SESSION['qs_user'] = [
                    'usuario'   => $user['usuario'],
                    'nombre'    => $user['nombre'],
                    'apellidos' => $user['apellidos'],
                    'email'     => $user['email'],
                ];
                header('Location: app.php?action=properties');
                exit;
            }
            $error = 'Usuario o contraseña incorrectos.';
        } else {
            $error = 'Rellena usuario y contraseña.';
        }
    } elseif ($action === 'register') {
        $usuario  = trim($_POST['usuario'] ?? '');
        $password = trim($_POST['password'] ?? '');
        $email    = trim($_POST['email'] ?? '');
        $nombre   = trim($_POST['nombre'] ?? '');
        $apellidos = trim($_POST['apellidos'] ?? '');

        if ($usuario !== '' && $password !== '') {
            $stmt = $db->prepare('SELECT 1 FROM usuario WHERE usuario = ?');
            $stmt->execute([$usuario]);
            if ($stmt->fetch()) {
                $error = 'Ya existe un usuario con ese identificador.';
            } else {
                $stmt = $db->prepare('INSERT INTO usuario (usuario, password, nombre, apellidos, email) VALUES (?, ?, ?, ?, ?)');
                $stmt->execute([$usuario, $password, $nombre, $apellidos, $email]);
                $_SESSION['qs_user'] = [
                    'usuario'   => $usuario,
                    'nombre'    => $nombre,
                    'apellidos' => $apellidos,
                    'email'     => $email,
                ];
                header('Location: app.php?action=properties');
                exit;
            }
        } else {
            $error = 'Usuario y contraseña son obligatorios.';
        }
    } elseif ($action === 'reserve') {
        require_login();
        $user = current_user();
        $propId = intval($_POST['propiedad_id'] ?? 0);
        $numPersonas = intval($_POST['num_personas'] ?? 1);
        $checkin  = $_POST['checkin'] ?? '';
        $checkout = $_POST['checkout'] ?? '';

        if ($propId > 0 && $checkin !== '' && $checkout !== '') {
            $stmt = $db->query('SELECT COALESCE(MAX(id), 0) AS max_id FROM alquila');
            $row = $stmt->fetch();
            $nextId = (int)($row['max_id'] ?? 0) + 1;

            $stmt = $db->prepare('INSERT INTO alquila (id, usuario_alquila, propiedad_alquilada, num_personas, checkin, checkout) VALUES (?, ?, ?, ?, ?, ?)');
            $stmt->execute([
                $nextId,
                $user['usuario'],
                $propId,
                $numPersonas,
                $checkin,
                $checkout,
            ]);
            header('Location: app.php?action=my_reservations');
            exit;
        } else {
            $error = 'Completa todos los campos para reservar.';
        }
    }
}

if ($action === 'logout') {
    session_destroy();
    header('Location: app.php');
    exit;
}

// Helper para obtener propiedades con valoración media
function fetch_properties_with_rating(PDO $db): array
{
    $sql = 'SELECT p.*, COALESCE(AVG(r.estrellas), 0) AS rating_media, COUNT(r.id) AS num_resenas
            FROM propiedad p
            LEFT JOIN resena_propiedad r ON r.id_propiedad = p.id
            GROUP BY p.id
            ORDER BY p.id';
    $stmt = $db->query($sql);
    return $stmt->fetchAll();
}

$user = current_user();

?><!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>QuickStay - App Web</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background:#f3f4f6; margin:0; padding:0; }
        header { background:#111827; color:#f9fafb; padding:1rem 1.5rem; display:flex; justify-content:space-between; align-items:center; }
        header a { color:#e5e7eb; text-decoration:none; margin-left:1rem; font-size:0.9rem; }
        main { max-width:1100px; margin:1.5rem auto; padding:0 1rem; }
        .card { background:#ffffff; border-radius:0.75rem; padding:1.5rem; box-shadow:0 10px 30px rgba(15,23,42,0.1); margin-bottom:1.5rem; }
        h1 { font-size:1.5rem; margin:0 0 0.5rem; }
        h2 { font-size:1.2rem; margin-top:0; }
        .props { display:grid; grid-template-columns:repeat(auto-fit,minmax(260px,1fr)); gap:1rem; }
        .prop { border:1px solid #e5e7eb; border-radius:0.75rem; padding:1rem; }
        .prop-title { font-weight:600; margin-bottom:0.25rem; }
        .badge { display:inline-block; padding:0.15rem 0.5rem; border-radius:999px; font-size:0.7rem; background:#eff6ff; color:#1d4ed8; margin-bottom:0.4rem; }
        .muted { color:#6b7280; font-size:0.85rem; }
        form { margin-top:0.75rem; }
        label { display:block; font-size:0.8rem; margin-top:0.4rem; color:#4b5563; }
        input, select { width:100%; padding:0.4rem 0.5rem; border-radius:0.4rem; border:1px solid #d1d5db; font-size:0.85rem; }
        button { margin-top:0.6rem; padding:0.45rem 0.9rem; border-radius:999px; border:none; background:#4f46e5; color:#ffffff; font-size:0.85rem; cursor:pointer; }
        .error { color:#b91c1c; margin-top:0.5rem; font-size:0.85rem; }
        .success { color:#15803d; margin-top:0.5rem; font-size:0.85rem; }
        table { width:100%; border-collapse:collapse; font-size:0.85rem; }
        th, td { padding:0.45rem 0.5rem; border-bottom:1px solid #e5e7eb; text-align:left; }
        th { background:#f9fafb; font-weight:600; }
        .nav-links a { margin-right:0.7rem; }
    </style>
</head>
<body>
<header>
    <div>
        <strong>QuickStay · App Web</strong>
        <span style="font-size:0.8rem; color:#9ca3af; margin-left:0.5rem;">Reservas sobre la misma BD que la app de escritorio</span>
    </div>
    <nav class="nav-links">
        <a href="index.php">Inicio infra</a>
        <a href="app.php">Inicio app</a>
        <a href="app.php?action=properties">Propiedades</a>
        <?php if ($user): ?>
            <a href="app.php?action=my_reservations">Mis reservas</a>
            <a href="app.php?action=logout">Salir (<?php echo htmlspecialchars($user['usuario']); ?>)</a>
        <?php else: ?>
            <a href="app.php?action=login">Entrar</a>
            <a href="app.php?action=register">Registro</a>
        <?php endif; ?>
    </nav>
</header>

<main>
    <?php if (!empty($error)): ?>
        <div class="card error"><?php echo htmlspecialchars($error); ?></div>
    <?php endif; ?>

    <?php if ($action === 'login'): ?>
        <div class="card">
            <h1>Iniciar sesión</h1>
            <form method="post" action="app.php?action=login">
                <label>Usuario
                    <input type="text" name="usuario" required>
                </label>
                <label>Contraseña
                    <input type="password" name="password" required>
                </label>
                <button type="submit">Entrar</button>
            </form>
            <p class="muted">¿No tienes cuenta? <a href="app.php?action=register">Regístrate aquí</a>.</p>
        </div>

    <?php elseif ($action === 'register'): ?>
        <div class="card">
            <h1>Registro de usuario</h1>
            <form method="post" action="app.php?action=register">
                <label>Usuario
                    <input type="text" name="usuario" required>
                </label>
                <label>Contraseña
                    <input type="password" name="password" required>
                </label>
                <label>Nombre
                    <input type="text" name="nombre">
                </label>
                <label>Apellidos
                    <input type="text" name="apellidos">
                </label>
                <label>Email
                    <input type="email" name="email">
                </label>
                <button type="submit">Crear cuenta</button>
            </form>
        </div>

    <?php elseif ($action === 'my_reservations'): ?>
        <?php require_login(); $user = current_user();
        $stmt = $db->prepare('SELECT a.*, p.nombre AS propiedad_nombre, p.localidad, p.provincia
                              FROM alquila a
                              JOIN propiedad p ON p.id = a.propiedad_alquilada
                              WHERE a.usuario_alquila = ?
                              ORDER BY a.checkin DESC');
        $stmt->execute([$user['usuario']]);
        $reservas = $stmt->fetchAll();
        ?>
        <div class="card">
            <h1>Mis reservas</h1>
            <?php if (!$reservas): ?>
                <p class="muted">Todavía no tienes reservas realizadas desde la web.</p>
            <?php else: ?>
                <table>
                    <thead>
                    <tr>
                        <th>Propiedad</th>
                        <th>Ubicación</th>
                        <th>Personas</th>
                        <th>Check-in</th>
                        <th>Check-out</th>
                    </tr>
                    </thead>
                    <tbody>
                    <?php foreach ($reservas as $r): ?>
                        <tr>
                            <td><?php echo htmlspecialchars($r['propiedad_nombre']); ?></td>
                            <td><?php echo htmlspecialchars(trim(($r['localidad'] ?? '') . ' ' . ($r['provincia'] ?? ''))); ?></td>
                            <td><?php echo (int)$r['num_personas']; ?></td>
                            <td><?php echo htmlspecialchars($r['checkin']); ?></td>
                            <td><?php echo htmlspecialchars($r['checkout']); ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            <?php endif; ?>
        </div>

    <?php elseif ($action === 'properties'): ?>
        <?php $props = fetch_properties_with_rating($db); ?>
        <div class="card">
            <h1>Propiedades disponibles</h1>
            <p class="muted">Consulta las propiedades que maneja QuickStay en la misma base de datos que la app de escritorio.</p>
            <div class="props">
                <?php foreach ($props as $p): ?>
                    <div class="prop">
                        <div class="prop-title"><?php echo htmlspecialchars($p['nombre'] ?: ('Propiedad #' . $p['id'])); ?></div>
                        <div class="badge"><?php echo htmlspecialchars($p['tipo'] ?: 'N/D'); ?></div>
                        <div class="muted">
                            <?php echo htmlspecialchars(trim(($p['localidad'] ?? '') . ', ' . ($p['provincia'] ?? ''))); ?><br>
                            Precio hora aprox: <?php echo $p['precio_hora'] !== null ? number_format((float)$p['precio_hora'], 2, ',', '.') . ' €' : 'N/D'; ?><br>
                            Valoración media: <?php echo number_format((float)$p['rating_media'], 1, ',', '.'); ?> ★ (<?php echo (int)$p['num_resenas']; ?> reseñas)
                        </div>
                        <?php if ($user): ?>
                            <form method="post" action="app.php?action=reserve">
                                <input type="hidden" name="propiedad_id" value="<?php echo (int)$p['id']; ?>">
                                <label>Número de personas
                                    <input type="number" name="num_personas" min="1" max="20" value="1">
                                </label>
                                <label>Check-in
                                    <input type="datetime-local" name="checkin" required>
                                </label>
                                <label>Check-out
                                    <input type="datetime-local" name="checkout" required>
                                </label>
                                <button type="submit">Reservar</button>
                            </form>
                        <?php else: ?>
                            <p class="muted" style="margin-top:0.5rem;">Inicia sesión para reservar.</p>
                        <?php endif; ?>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>

    <?php else: ?>
        <div class="card">
            <h1>Bienvenido a la app web de QuickStay</h1>
            <p class="muted">
                Esta interfaz web trabaja directamente contra la base de datos <strong>humhouse</strong>,
                compartida con la aplicación de escritorio JavaFX. Aquí puedes consultar propiedades y
                crear reservas básicas desde el navegador.
            </p>
            <?php if ($user): ?>
                <p>Estás identificado como <strong><?php echo htmlspecialchars($user['usuario']); ?></strong>.</p>
                <p>
                    • Ve a <a href="app.php?action=properties">Propiedades</a> para reservar.<br>
                    • Revisa <a href="app.php?action=my_reservations">Mis reservas</a> para ver tu histórico.
                </p>
            <?php else: ?>
                <p>
                    • <a href="app.php?action=login">Inicia sesión</a> si ya tienes usuario creado desde la app de escritorio.<br>
                    • O <a href="app.php?action=register">regístrate</a> para crear un usuario nuevo.
                </p>
            <?php endif; ?>
        </div>
    <?php endif; ?>
</main>

</body>
</html>
