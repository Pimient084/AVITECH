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

-- =========================
-- 1) Usuarios (unique: usuario)
-- =========================
INSERT INTO Usuarios (usuario, password, rol, email, telefono, direccion)
VALUES ('admin', 'admin123', 'ADMIN', 'admin@avicola.local', '0990000001', 'Oficina Central')
ON DUPLICATE KEY UPDATE password = VALUES(password), rol = VALUES(rol), email = VALUES(email), telefono = VALUES(telefono), direccion = VALUES(direccion);

INSERT INTO Usuarios (usuario, password, rol, email, telefono, direccion)
VALUES ('supervisor', 'super123', 'SUPERVISOR', 'supervisor@avicola.local', '0990000002', 'Planta Principal')
ON DUPLICATE KEY UPDATE password = VALUES(password), rol = VALUES(rol), email = VALUES(email), telefono = VALUES(telefono), direccion = VALUES(direccion);

INSERT INTO Usuarios (usuario, password, rol, email, telefono, direccion)
VALUES ('operador', 'oper123', 'OPERADOR', 'operador@avicola.local', '0990000003', 'Galponera Norte')
ON DUPLICATE KEY UPDATE password = VALUES(password), rol = VALUES(rol), email = VALUES(email), telefono = VALUES(telefono), direccion = VALUES(direccion);

-- Cache de IDs
SET @admin_id     := (SELECT id_usuario FROM Usuarios WHERE usuario='admin' LIMIT 1);
SET @superv_id    := (SELECT id_usuario FROM Usuarios WHERE usuario='supervisor' LIMIT 1);
SET @operador_id  := (SELECT id_usuario FROM Usuarios WHERE usuario='operador' LIMIT 1);

-- =========================
-- 2) Lotes (clave natural: nombre_lote)
-- =========================
INSERT INTO Lote (nombre_lote, estado, cantidadGallinas)
SELECT 'Lote A', 'Activo', 1200 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Lote WHERE nombre_lote='Lote A');

INSERT INTO Lote (nombre_lote, estado, cantidadGallinas)
SELECT 'Lote B', 'Activo', 900 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Lote WHERE nombre_lote='Lote B');

INSERT INTO Lote (nombre_lote, estado, cantidadGallinas)
SELECT 'Lote C', 'Reemplazo', 600 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Lote WHERE nombre_lote='Lote C');

SET @loteA_id := (SELECT Id_lote FROM Lote WHERE nombre_lote='Lote A' LIMIT 1);
SET @loteB_id := (SELECT Id_lote FROM Lote WHERE nombre_lote='Lote B' LIMIT 1);
SET @loteC_id := (SELECT Id_lote FROM Lote WHERE nombre_lote='Lote C' LIMIT 1);

-- =========================
-- 3) Galpones (clave natural: nombre)
-- =========================
INSERT INTO Galpones (Id_lote, nombre, capacidad, estado_sanitario, id_usuario)
SELECT @loteA_id, 'Galpón 1A', 400, 'OK', @admin_id FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Galpones WHERE nombre='Galpón 1A');

INSERT INTO Galpones (Id_lote, nombre, capacidad, estado_sanitario, id_usuario)
SELECT @loteA_id, 'Galpón 1B', 800, 'OK', @superv_id FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Galpones WHERE nombre='Galpón 1B');

INSERT INTO Galpones (Id_lote, nombre, capacidad, estado_sanitario, id_usuario)
SELECT @loteB_id, 'Galpón 2A', 450, 'Observado', @operador_id FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Galpones WHERE nombre='Galpón 2A');

INSERT INTO Galpones (Id_lote, nombre, capacidad, estado_sanitario, id_usuario)
SELECT @loteC_id, 'Galpón 3A', 600, 'En cuarentena', @superv_id FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Galpones WHERE nombre='Galpón 3A');

SET @g1a := (SELECT id_galpon FROM Galpones WHERE nombre='Galpón 1A' LIMIT 1);
SET @g1b := (SELECT id_galpon FROM Galpones WHERE nombre='Galpón 1B' LIMIT 1);
SET @g2a := (SELECT id_galpon FROM Galpones WHERE nombre='Galpón 2A' LIMIT 1);
SET @g3a := (SELECT id_galpon FROM Galpones WHERE nombre='Galpón 3A' LIMIT 1);

-- =========================
-- 4) Monitoreo (clave natural: fecha + id_galpon)
-- =========================
INSERT INTO Monitoreo (id_galpon, temperatura, humedad, fecha)
SELECT @g1a, 22.5, 65.0, DATE('2025-10-20') FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Monitoreo WHERE id_galpon=@g1a AND fecha=DATE('2025-10-20'));

INSERT INTO Monitoreo (id_galpon, temperatura, humedad, fecha)
SELECT @g1b, 23.1, 63.5, DATE('2025-10-20') FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Monitoreo WHERE id_galpon=@g1b AND fecha=DATE('2025-10-20'));

INSERT INTO Monitoreo (id_galpon, temperatura, humedad, fecha)
SELECT @g2a, 21.8, 68.2, DATE('2025-10-19') FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Monitoreo WHERE id_galpon=@g2a AND fecha=DATE('2025-10-19'));

INSERT INTO Monitoreo (id_galpon, temperatura, humedad, fecha)
SELECT @g3a, 24.0, 60.0, DATE('2025-10-18') FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Monitoreo WHERE id_galpon=@g3a AND fecha=DATE('2025-10-18'));

-- =========================
-- 5) Medicamentos (clave natural: nombre + presentacion)
-- =========================
INSERT INTO Medicamentos (nombre, presentacion, stock, stock_minimo, valor_unitario)
SELECT 'Vacuna Newcastle', 'Frasco 100ml', 50, 5, 150.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Medicamentos WHERE nombre='Vacuna Newcastle' AND presentacion='Frasco 100ml');

INSERT INTO Medicamentos (nombre, presentacion, stock, stock_minimo, valor_unitario)
SELECT 'Antibiótico X', 'Caja 20 tabletas', 200, 20, 25.50 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Medicamentos WHERE nombre='Antibiótico X' AND presentacion='Caja 20 tabletas');

INSERT INTO Medicamentos (nombre, presentacion, stock, stock_minimo, valor_unitario)
SELECT 'Suplemento A', 'Saco 25kg', 30, 5, 450.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM Medicamentos WHERE nombre='Suplemento A' AND presentacion='Saco 25kg');

SET @med_newcastle := (SELECT Id_Medicamento FROM Medicamentos WHERE nombre='Vacuna Newcastle' AND presentacion='Frasco 100ml' LIMIT 1);
SET @med_abx       := (SELECT Id_Medicamento FROM Medicamentos WHERE nombre='Antibiótico X' AND presentacion='Caja 20 tabletas' LIMIT 1);
SET @med_supl      := (SELECT Id_Medicamento FROM Medicamentos WHERE nombre='Suplemento A' AND presentacion='Saco 25kg' LIMIT 1);

-- =========================
-- 6) Plan Sanitario (clave natural: lote + medicamento + fecha)
-- =========================
INSERT INTO Plan_Sanitario (Id_lote, Id_medicamento, fecha, nombre_enfermedad, muertes, descripcion, observaciones)
SELECT @loteA_id, @med_newcastle, DATE('2025-09-15'), 'Newcastle', 2, 'Aplicación preventiva anual', 'Sin incidencias' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Plan_Sanitario WHERE Id_lote=@loteA_id AND Id_medicamento=@med_newcastle AND fecha=DATE('2025-09-15')
);

INSERT INTO Plan_Sanitario (Id_lote, Id_medicamento, fecha, nombre_enfermedad, muertes, descripcion, observaciones)
SELECT @loteB_id, @med_abx, DATE('2025-10-01'), 'Infección bacteriana', 5, 'Tratamiento por diagnóstico', 'Monitorear 7 días' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Plan_Sanitario WHERE Id_lote=@loteB_id AND Id_medicamento=@med_abx AND fecha=DATE('2025-10-01')
);

INSERT INTO Plan_Sanitario (Id_lote, Id_medicamento, fecha, nombre_enfermedad, muertes, descripcion, observaciones)
SELECT @loteA_id, @med_supl, DATE('2025-08-05'), 'Deficiencia nutricional', 0, 'Suplemento vitamínico', 'Mejoría observada' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Plan_Sanitario WHERE Id_lote=@loteA_id AND Id_medicamento=@med_supl AND fecha=DATE('2025-08-05')
);

-- =========================
-- 7) Producción de huevos (clave natural: fecha + galpon)
-- =========================
INSERT INTO ProduccionHuevos (fecha, galpon, total_huevos, huevos_L, huevos_M, huevos_S, temperatura, humedad, mortalidad, responsable)
SELECT DATE('2025-10-20'), @g1a, 320, 120, 130, 70, 22.5, 65.0, 1, 'Operador A' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ProduccionHuevos WHERE galpon=@g1a AND fecha=DATE('2025-10-20'));

INSERT INTO ProduccionHuevos (fecha, galpon, total_huevos, huevos_L, huevos_M, huevos_S, temperatura, humedad, mortalidad, responsable)
SELECT DATE('2025-10-20'), @g1b, 640, 240, 260, 140, 23.1, 63.5, 2, 'Operador B' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ProduccionHuevos WHERE galpon=@g1b AND fecha=DATE('2025-10-20'));

INSERT INTO ProduccionHuevos (fecha, galpon, total_huevos, huevos_L, huevos_M, huevos_S, temperatura, humedad, mortalidad, responsable)
SELECT DATE('2025-10-19'), @g2a, 280, 100, 110, 70, 21.8, 68.2, 0, 'Operador C' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ProduccionHuevos WHERE galpon=@g2a AND fecha=DATE('2025-10-19'));

-- =========================
-- 8) Suministros (clave natural: fecha + tipo + item + cantidad)
-- =========================
INSERT INTO Suministros (fecha, tipo, item, cantidad, unidad, responsable, proveedor, motivo, valor_total)
SELECT DATE('2025-10-10'), 'Entrada', 'Saco de alimento 25kg', 10, 'unidad', 'Proveedor X', 'Proveedor X', 'Compra semanal', 4500.00 FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Suministros WHERE fecha=DATE('2025-10-10') AND tipo='Entrada' AND item='Saco de alimento 25kg' AND cantidad=10
);

INSERT INTO Suministros (fecha, tipo, item, cantidad, unidad, responsable, proveedor, motivo, valor_total)
SELECT DATE('2025-10-12'), 'Salida', 'Vacuna Newcastle', 2, 'frasco', 'Operador B', 'Farmacia Interna', 'Uso en lote 1', 300.00 FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Suministros WHERE fecha=DATE('2025-10-12') AND tipo='Salida' AND item='Vacuna Newcastle' AND cantidad=2
);

-- =========================
-- 9) Alertas (clave natural: tipo + descripcion)
-- =========================
INSERT INTO Alertas (tipo, descripcion, categoria, id_item, estado)
SELECT 'Advertencia', 'Stock de Suplemento A por debajo del mínimo', 'Stock', @med_supl, 'Activa' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Alertas WHERE tipo='Advertencia' AND descripcion='Stock de Suplemento A por debajo del mínimo'
);

INSERT INTO Alertas (tipo, descripcion, categoria, id_item, estado)
SELECT 'Crítico', 'Aumento de mortalidad en Galpón 2A', 'Sanidad', @g2a, 'Activa' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Alertas WHERE tipo='Crítico' AND descripcion='Aumento de mortalidad en Galpón 2A'
);

-- =========================
-- 10) Auditoría (clave natural: usuario + accion + modulo + detalle)
-- =========================
INSERT INTO Auditoria (id_usuario, accion, modulo, detalle, referencia)
SELECT @admin_id, 'Login', 'Autenticación', 'Acceso exitoso del admin', 'usuario=admin' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Auditoria WHERE id_usuario=@admin_id AND accion='Login' AND modulo='Autenticación' AND detalle='Acceso exitoso del admin'
);

INSERT INTO Auditoria (id_usuario, accion, modulo, detalle, referencia)
SELECT @superv_id, 'Registro Monitoreo', 'Monitoreo', 'Registro de temperatura/humedad galpón 1B', 'galpon=1B' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Auditoria WHERE id_usuario=@superv_id AND accion='Registro Monitoreo' AND modulo='Monitoreo' AND detalle='Registro de temperatura/humedad galpón 1B'
);

INSERT INTO Auditoria (id_usuario, accion, modulo, detalle, referencia)
SELECT @operador_id, 'Registro Producción', 'Producción', 'Ingreso de producción diaria', 'produccion_id=NULL' FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Auditoria WHERE id_usuario=@operador_id AND accion='Registro Producción' AND modulo='Producción' AND detalle='Ingreso de producción diaria'
);

-- =========================
-- 11) Respaldos (clave natural: archivo)
-- =========================
INSERT INTO Respaldos (archivo, fecha_hora, tipo, `tamaño`, estado, usuario)
SELECT 'backup_completo_2025-10-15.sql', '2025-10-15 02:30:00', 'Completo', '25MB', 'Exitoso', @admin_id FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Respaldos WHERE archivo='backup_completo_2025-10-15.sql'
);

INSERT INTO Respaldos (archivo, fecha_hora, tipo, `tamaño`, estado, usuario)
SELECT 'backup_selectivo_medicamentos_2025-10-18.sql', '2025-10-18 03:10:00', 'Selectivo', '3MB', 'Exitoso', @superv_id FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM Respaldos WHERE archivo='backup_selectivo_medicamentos_2025-10-18.sql'
);

-- =========================
-- 12) Catálogo de planes (unique: nombre)
-- =========================
INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado)
VALUES ('Programa Vacunación Ponedoras', 'Aplicar según edad y cronograma', '7–72 semanas', 'Preventivo')
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), edad=VALUES(edad), estado=VALUES(estado);

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado)
VALUES ('Desparasitación Trimestral', 'Programa de desparasitación interna/externa', 'Cada 12 semanas', 'Preventivo')
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), edad=VALUES(edad), estado=VALUES(estado);

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado)
VALUES ('Tratamiento Respiratorio', 'Manejo de síntomas respiratorios agudos', 'Según diagnóstico', 'Curativo')
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), edad=VALUES(edad), estado=VALUES(estado);

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado)
VALUES ('Refuerzo de Suplementación', 'Refuerzo vitamínico y mineral preventivo', 'Mensual', 'Preventivo')
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), edad=VALUES(edad), estado=VALUES(estado);

INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado)
VALUES ('Plan Mixto Bioseguridad', 'Refuerzos y acciones combinadas según riesgo', 'Variable', 'Mixto')
ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion), edad=VALUES(edad), estado=VALUES(estado);
