-- SQL script para la aplicación Avicola2
CREATE DATABASE IF NOT EXISTS avicola2;
USE avicola2;

CREATE TABLE IF NOT EXISTS Usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE,
    telefono VARCHAR(20),
    direccion VARCHAR(255)
);

-- Insertar usuarios de prueba con contraseñas hasheadas (BCrypt)
-- admin -> contraseña en texto: 'admin123'
-- supervisor -> 'super123'
-- operator -> 'oper123'
INSERT INTO Usuarios (usuario, password, rol) VALUES
('admin', '$2a$12$MrbbvFNZXMQlMLHUEUXhUuDIgQUS7J/nS7alf4BeygFNCOaQSGEMm', 'ADMIN'),
('supervisor', '$2a$12$oVmFs29hI0CrbCoJ.qm5beSORg91AvA7tnRczcFyZJhHlgGbRnVg2', 'SUPERVISOR'),
('operator', '$2a$12$oCExSUVt1SpsCZyN0irpoemVLJDzRo5wTQGtQp5HIWrAHUX0IlO9.', 'OPERADOR');

CREATE TABLE IF NOT EXISTS Lote (
    Id_lote INT AUTO_INCREMENT PRIMARY KEY,
    nombre_lote VARCHAR(25) NOT NULL,
    estado VARCHAR(45) NOT NULL,
    cantidadGallinas INT NOT NULL
);

CREATE TABLE IF NOT EXISTS Galpones (
    id_galpon INT AUTO_INCREMENT PRIMARY KEY,
    Id_lote INT NOT NULL,
    nombre VARCHAR(100),
    capacidad INT,
    estado_sanitario VARCHAR(100),
    id_usuario INT,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (Id_lote) REFERENCES Lote(Id_lote)
);

CREATE TABLE IF NOT EXISTS Monitoreo (
    id_monitoreo INT AUTO_INCREMENT PRIMARY KEY,
    id_galpon INT,
    temperatura FLOAT,
    humedad FLOAT,
    fecha DATE,
    FOREIGN KEY (id_galpon) REFERENCES Galpones(id_galpon)
);

CREATE TABLE IF NOT EXISTS Medicamentos (
    Id_Medicamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL, 
    presentacion VARCHAR(45) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT DEFAULT 0,
    valor_unitario DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS Plan_Sanitario (
    ID_planSanitario INT AUTO_INCREMENT PRIMARY KEY,
    Id_lote INT NOT NULL,
    Id_medicamento INT NOT NULL,
    fecha DATE NOT NULL,
    nombre_enfermedad VARCHAR(45) NOT NULL,
    muertes INT DEFAULT 0,
    descripcion VARCHAR(200),
    observaciones VARCHAR(200),
    FOREIGN KEY (Id_lote) REFERENCES Lote(Id_lote),
    FOREIGN KEY (Id_medicamento) REFERENCES Medicamentos(Id_Medicamento)
);

CREATE TABLE IF NOT EXISTS ProduccionHuevos (
    Id_produccion INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    galpon INT NOT NULL,
    total_huevos INT NOT NULL DEFAULT 0,
    huevos_L INT DEFAULT 0,
    huevos_M INT DEFAULT 0,
    huevos_S INT DEFAULT 0,
    temperatura FLOAT,
    humedad FLOAT,
    mortalidad INT DEFAULT 0,
    responsable VARCHAR(100),
    FOREIGN KEY (galpon) REFERENCES Galpones(id_galpon)
);

CREATE TABLE IF NOT EXISTS Suministros (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    tipo ENUM('Entrada','Salida') NOT NULL,
    item VARCHAR(100) NOT NULL,
    cantidad INT NOT NULL,
    unidad VARCHAR(50),
    responsable VARCHAR(100),
    proveedor VARCHAR(100),
    motivo VARCHAR(200),
    valor_total DECIMAL(10,2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS Alertas (
    id_alerta INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('Crítico','Bajo','Advertencia') NOT NULL,
    descripcion VARCHAR(200),
    categoria VARCHAR(50),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_item INT,
    estado ENUM('Activa','Resuelta') DEFAULT 'Activa'
);

-- Tabla de auditoría utilizada por AuditoriaDAO
CREATE TABLE IF NOT EXISTS Auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(100) NOT NULL,
    modulo VARCHAR(100),
    detalle VARCHAR(500),
    referencia VARCHAR(200),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS Respaldos (
    id_respaldo INT AUTO_INCREMENT PRIMARY KEY,
    archivo VARCHAR(100),
    fecha_hora DATETIME,
    tipo ENUM('Completo','Selectivo'),
    tamaño VARCHAR(20),
    estado ENUM('Exitoso','Error') DEFAULT 'Exitoso',
    usuario INT,
    FOREIGN KEY (usuario) REFERENCES Usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS Plan_Catalogo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    edad VARCHAR(50),
    estado VARCHAR(30) NOT NULL
);

CREATE OR REPLACE VIEW Vista_Inventario_Valorizado AS
SELECT
    Id_Medicamento,
    nombre,
    presentacion,
    stock,
    stock_minimo,
    valor_unitario,
    (stock * valor_unitario) AS valor_total_stock
FROM
    Medicamentos;

CREATE OR REPLACE VIEW Vista_Estado_Galpones AS
SELECT
    g.id_galpon,
    g.nombre AS nombre_galpon,
    l.nombre_lote,
    l.estado AS estado_lote,
    g.capacidad,
    g.estado_sanitario,
    u.usuario AS responsable
FROM Galpones g
JOIN Lote l ON g.Id_lote = l.Id_lote
LEFT JOIN Usuarios u ON g.id_usuario = u.id_usuario;

-- Datos de ejemplo para pruebas
-- NOTA: se asume que los Usuarios ya insertados arriba tienen id_usuario = 1 (admin), 2 (supervisor), 3 (operator)

-- Lotes
INSERT INTO Lote (nombre_lote, estado, cantidadGallinas) VALUES
('Lote A', 'Activo', 1200),
('Lote B', 'Activo', 900),
('Lote C', 'Reemplazo', 600);

-- Galpones
INSERT INTO Galpones (Id_lote, nombre, capacidad, estado_sanitario, id_usuario) VALUES
(1, 'Galpón 1A', 400, 'OK', 1),
(1, 'Galpón 1B', 800, 'OK', 2),
(2, 'Galpón 2A', 450, 'Observado', 3),
(3, 'Galpón 3A', 600, 'En cuarentena', 2);

-- Monitoreo (muestras recientes)
INSERT INTO Monitoreo (id_galpon, temperatura, humedad, fecha) VALUES
(1, 22.5, 65.0, '2025-10-20'),
(2, 23.1, 63.5, '2025-10-20'),
(3, 21.8, 68.2, '2025-10-19'),
(4, 24.0, 60.0, '2025-10-18');

-- Medicamentos
INSERT INTO Medicamentos (nombre, presentacion, stock, stock_minimo, valor_unitario) VALUES
('Vacuna Newcastle', 'Frasco 100ml', 50, 5, 150.00),
('Antibiótico X', 'Caja 20 tabletas', 200, 20, 25.50),
('Suplemento A', 'Saco 25kg', 30, 5, 450.00);

-- Plan sanitario (aplicaciones realizadas)
INSERT INTO Plan_Sanitario (Id_lote, Id_medicamento, fecha, nombre_enfermedad, muertes, descripcion, observaciones) VALUES
(1, 1, '2025-09-15', 'Newcastle', 2, 'Aplicación preventiva anual', 'Sin incidencias'),
(2, 2, '2025-10-01', 'Infección bacteriana', 5, 'Tratamiento por diagnóstico', 'Monitorear 7 días'),
(1, 3, '2025-08-05', 'Deficiencia nutricional', 0, 'Suplemento vitamínico', 'Mejoría observado');

-- Producción de huevos (muestras)
INSERT INTO ProduccionHuevos (fecha, galpon, total_huevos, huevos_L, huevos_M, huevos_S, temperatura, humedad, mortalidad, responsable) VALUES
('2025-10-20', 1, 320, 120, 130, 70, 22.5, 65.0, 1, 'Operador A'),
('2025-10-20', 2, 640, 240, 260, 140, 23.1, 63.5, 2, 'Operador B'),
('2025-10-19', 3, 280, 100, 110, 70, 21.8, 68.2, 0, 'Operador C');

-- Suministros (entradas y salidas)
INSERT INTO Suministros (fecha, tipo, item, cantidad, unidad, responsable, proveedor, motivo, valor_total) VALUES
('2025-10-10', 'Entrada', 'Saco de alimento 25kg', 10, 'unidad', 'Proveedor X', 'Proveedor X', 'Compra semanal', 4500.00),
('2025-10-12', 'Salida', 'Vacuna Newcastle', 2, 'frasco', 'Operador B', 'Alimentación', 'Uso en lote 1', 300.00);

-- Alertas
INSERT INTO Alertas (tipo, descripcion, categoria, id_item, estado) VALUES
('Advertencia', 'Stock de Suplemento A por debajo del mínimo', 'Stock', 3, 'Activa'),
('Crítico', 'Aumento de mortalidad en Galpón 2A', 'Sanidad', 3, 'Activa');

-- Auditoría (acciones de usuarios)
INSERT INTO Auditoria (id_usuario, accion, modulo, detalle, referencia) VALUES
(1, 'Login', 'Autenticación', 'Acceso exitoso del admin', 'usuario=admin'),
(2, 'Registro Monitoreo', 'Monitoreo', 'Registro de temperatura/humedad galpón 1B', 'galpon=2'),
(3, 'Registro Producción', 'Producción', 'Ingreso de producción diaria', 'produccion_id=NULL');

-- Respaldos (registro de backups)
INSERT INTO Respaldos (archivo, fecha_hora, tipo, tamaño, estado, usuario) VALUES
('backup_completo_2025-10-15.sql', '2025-10-15 02:30:00', 'Completo', '25MB', 'Exitoso', 1),
('backup_selectivo_medicamentos_2025-10-18.sql', '2025-10-18 03:10:00', 'Selectivo', '3MB', 'Exitoso', 2);

-- Poblar catálogo de planes sanitarios (evita duplicados por nombre)
INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado) VALUES
('Programa Vacunación Ponedoras', 'Aplicar según edad y cronograma', '7–72 semanas', 'Preventivo')
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado) VALUES
('Desparasitación Trimestral', 'Programa de desparasitación interna/externa', 'Cada 12 semanas', 'Preventivo')
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado) VALUES
('Tratamiento Respiratorio', 'Manejo de síntomas respiratorios agudos', 'Según diagnóstico', 'Curativo')
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado) VALUES
('Refuerzo de Suplementación', 'Refuerzo vitamínico y mineral preventivo', 'Mensual', 'Preventivo')
ON DUPLICATE KEY UPDATE nombre = nombre;

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado) VALUES
('Plan Mixto Bioseguridad', 'Refuerzos y acciones combinadas según riesgo', 'Variable', 'Mixto')
ON DUPLICATE KEY UPDATE nombre = nombre;

-- fin del script
