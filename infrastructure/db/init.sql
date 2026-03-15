-- Database Initialization Script for QuickStay
-- Reverse Engineered from ProyectoFinal Java Code

CREATE DATABASE IF NOT EXISTS humhouse;
USE humhouse;

-- 1. Table: encryption_keys
CREATE TABLE IF NOT EXISTS encryption_keys (
    id_usuario VARCHAR(255) PRIMARY KEY,
    key_value TEXT -- Assuming key storage, type inferred
);

-- 2. Table: usuario
CREATE TABLE IF NOT EXISTS usuario (
    usuario VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(255),
    apellidos VARCHAR(255),
    fecha_nacimiento VARCHAR(50), -- Stored as String in Java
    email VARCHAR(255),
    foto_usuario LONGBLOB,
    num_telefono VARCHAR(50)
);

-- 3. Table: tarjeta
CREATE TABLE IF NOT EXISTS tarjeta (
    num_tarjeta VARCHAR(255) PRIMARY KEY,
    fecha_vencimiento VARCHAR(50),
    titular_nombre VARCHAR(255), -- Concatenated in Java insert
    cvv INT,
    direccion_facturacion TEXT,
    id_usuario VARCHAR(255),
    FOREIGN KEY (id_usuario) REFERENCES usuario(usuario) ON DELETE CASCADE
);

-- 4. Table: propiedad
CREATE TABLE IF NOT EXISTS propiedad (
    id INT PRIMARY KEY,
    nombre VARCHAR(255),
    tipo VARCHAR(100),
    id_propietario VARCHAR(255),
    descripcion TEXT,
    comunidad VARCHAR(100),
    provincia VARCHAR(100),
    localidad VARCHAR(100),
    pedania VARCHAR(100),
    latitud FLOAT,
    longitud FLOAT,
    altitud FLOAT,
    codigo_postal INT,
    calle TEXT,
    precio_hora FLOAT,
    FOREIGN KEY (id_propietario) REFERENCES usuario(usuario) ON DELETE CASCADE
);

-- 5. Table: facturacion
CREATE TABLE IF NOT EXISTS facturacion (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_propiedad INT,
    fecha_facturacion DATETIME,
    id_tarjeta VARCHAR(255),
    nif_usuario VARCHAR(50), -- nif_usuario mentioned in selectAllFacturas
    id_usuario VARCHAR(255), -- Needed for WHERE id_usuario = ?
    FOREIGN KEY (id_propiedad) REFERENCES propiedad(id),
    FOREIGN KEY (id_tarjeta) REFERENCES tarjeta(num_tarjeta),
    FOREIGN KEY (id_usuario) REFERENCES usuario(usuario)
);

-- 6. Table: resena_propiedad
CREATE TABLE IF NOT EXISTS resena_propiedad (
    id INT PRIMARY KEY,
    usuario VARCHAR(255),
    id_propiedad INT,
    info TEXT, -- 'comentario' in object, 'info' in DB
    fecha DATE,
    estrellas FLOAT,
    FOREIGN KEY (usuario) REFERENCES usuario(usuario),
    FOREIGN KEY (id_propiedad) REFERENCES propiedad(id) ON DELETE CASCADE
);

-- 7. Table: fotos_propiedad
CREATE TABLE IF NOT EXISTS fotos_propiedad (
    id INT PRIMARY KEY,
    imagen LONGBLOB,
    formato VARCHAR(50),
    idPropiedad INT,
    FOREIGN KEY (idPropiedad) REFERENCES propiedad(id) ON DELETE CASCADE
);

-- 8. Table: lista_contactos
CREATE TABLE IF NOT EXISTS lista_contactos (
    idUsuario VARCHAR(255),
    contacto VARCHAR(255),
    PRIMARY KEY (idUsuario, contacto),
    FOREIGN KEY (idUsuario) REFERENCES usuario(usuario) ON DELETE CASCADE,
    FOREIGN KEY (contacto) REFERENCES usuario(usuario) ON DELETE CASCADE
);

-- 9. Table: alquila
CREATE TABLE IF NOT EXISTS alquila (
    id INT PRIMARY KEY,
    usuario_alquila VARCHAR(255),
    propiedad_alquilada INT,
    num_personas INT,
    checkin DATETIME,
    checkout DATETIME,
    FOREIGN KEY (usuario_alquila) REFERENCES usuario(usuario),
    FOREIGN KEY (propiedad_alquilada) REFERENCES propiedad(id)
);

-- Initial Mock Data (Optional/Test)
-- INSERT INTO usuario VALUES ('admin', 'admin', 'Admin', 'User', '1990-01-01', 'admin@quickstay.local', NULL, '123456789');
