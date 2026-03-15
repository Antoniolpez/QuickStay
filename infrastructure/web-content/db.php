<?php
// Conexión PDO reutilizable a la base de datos humhouse

function qs_get_db(): PDO
{
    static $pdo = null;
    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $host = getenv('DB_HOST') ?: '172.16.20.20';
    $port = getenv('DB_PORT') ?: '3306';
    $dbname = getenv('DB_NAME') ?: 'humhouse';

    // Usuario/contraseña según variables usadas por la infra
    $user = getenv('DB_USER') ?: getenv('MYSQL_USER') ?: 'quickstay_app';
    $pass = getenv('DB_PASSWORD') ?: getenv('MYSQL_PASSWORD') ?: 'QuickStay2026!AppDB';

    $dsn = sprintf('mysql:host=%s;port=%s;dbname=%s;charset=utf8mb4', $host, $port, $dbname);

    try {
        $pdo = new PDO($dsn, $user, $pass, [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]);
    } catch (PDOException $e) {
        http_response_code(500);
        echo '<h1>Error de conexión a base de datos</h1>';
        echo '<p>No se ha podido conectar al backend de QuickStay.</p>';
        exit;
    }

    return $pdo;
}
