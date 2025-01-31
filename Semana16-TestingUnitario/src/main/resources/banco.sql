-- Conectarse a la base de datos postgres
\c postgres;

-- Verificar si la base de datos 'banco' ya existe y crearla si no existe
SELECT 'CREATE DATABASE banco'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'banco')\gexec

-- Conectarse a la base de datos banco
    \c banco;

-- Borrar tablas si existen (en orden de dependencias)
DROP TABLE IF EXISTS transacciones;
DROP TABLE IF EXISTS cuentas;
DROP TABLE IF EXISTS perfiles;
DROP TABLE IF EXISTS usuarios;

-- Crear tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
                                        id_usuario SERIAL PRIMARY KEY,
                                        username VARCHAR(50) NOT NULL UNIQUE,
                                        password VARCHAR(50) NOT NULL,
                                        email VARCHAR(100) NOT NULL UNIQUE,
                                        fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Crear tabla perfiles
CREATE TABLE IF NOT EXISTS perfiles (
                                        id_perfil SERIAL PRIMARY KEY,
                                        id_usuario INT NOT NULL UNIQUE,
                                        nombre_completo VARCHAR(100) NOT NULL,
                                        telefono VARCHAR(15) UNIQUE,
                                        direccion TEXT,
                                        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-- Crear tabla cuentas
CREATE TABLE IF NOT EXISTS cuentas (
                                       id_cuenta SERIAL PRIMARY KEY,
                                       id_usuario INT NOT NULL,
                                       saldo DECIMAL(10, 2) DEFAULT 0.00 CHECK (saldo >= 0.00),
                                       fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       tipo_cuenta VARCHAR(20) NOT NULL CHECK (tipo_cuenta IN ('Ahorros', 'Corriente')),
                                       FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT
);

-- Crear tabla transacciones
CREATE TABLE IF NOT EXISTS transacciones (
                                             id_transaccion SERIAL PRIMARY KEY,
                                             id_cuenta INT NOT NULL,
                                             monto DECIMAL(10, 2) NOT NULL,
                                             fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             tipo_transaccion VARCHAR(20) NOT NULL CHECK (tipo_transaccion IN ('Deposito', 'Retiro')),
                                             FOREIGN KEY (id_cuenta) REFERENCES cuentas(id_cuenta) ON DELETE CASCADE
);


-- Insertar datos adicionales en usuarios
INSERT INTO usuarios (username, password, email, fecha_registro) VALUES
                                                                     ('admin', '12345', 'admin@example.com', '2023-01-01'),
                                                                     ('user1', 'password1', 'user1@example.com', '2023-02-01'),
                                                                     ('user2', 'password2', 'user2@example.com', '2023-03-01'),
                                                                     ('user3', 'password3', 'user3@example.com', '2023-04-01'),
                                                                     ('user4', 'password4', 'user4@example.com', '2023-05-01'),
                                                                     ('user5', 'password5', 'user5@example.com', '2023-06-01'),
                                                                     ('user6', 'password6', 'user6@example.com', '2023-07-01'),
                                                                     ('user7', 'password7', 'user7@example.com', '2023-08-01');

-- Insertar datos adicionales en perfiles
INSERT INTO perfiles (id_usuario, nombre_completo, telefono, direccion) VALUES
                                                                            (1, 'Administrador del Sistema', '600000111', 'Calle Principal, 123'),
                                                                            (2, 'Usuario Uno', '600000222', 'Avenida Secundaria, 456'),
                                                                            (3, 'Usuario Dos', '600000333', 'Boulevard Tercero, 789'),
                                                                            (4, 'Usuario Tres', '600000444', 'Calle Cuarta, 987'),
                                                                            (5, 'Usuario Cuatro', '600000555', 'Avenida Quinta, 654'),
                                                                            (6, 'Usuario Cinco', '600000666', 'Boulevard Sexto, 321'),
                                                                            (7, 'Usuario Seis', '600000777', 'Calle Séptima, 456'),
                                                                            (8, 'Usuario Siete', '600000888', 'Avenida Octava, 789');

-- Insertar datos adicionales en cuentas
INSERT INTO cuentas (id_usuario, saldo, tipo_cuenta) VALUES
                                                         (1, 1000.00, 'Corriente'),
                                                         (2, 500.00, 'Ahorros'),
                                                         (2, 200.00, 'Corriente'),
                                                         (3, 750.00, 'Ahorros'),
                                                         (4, 1200.00, 'Corriente'),
                                                         (4, 300.00, 'Ahorros'),
                                                         (5, 1500.00, 'Ahorros'),
                                                         (6, 250.00, 'Corriente'),
                                                         (7, 5000.00, 'Corriente'),
                                                         (8, 1000.00, 'Ahorros'),
                                                         (8, 1500.00, 'Corriente');

-- Insertar datos en transacciones con marcas temporales explícitas
INSERT INTO transacciones (id_cuenta, monto, tipo_transaccion, fecha) VALUES
                                                                          (1, 500.00, 'Deposito', '2024-01-01 10:00:00'),
                                                                          (1, -200.00, 'Retiro', '2024-01-02 11:30:00'),
                                                                          (2, 300.00, 'Deposito', '2024-01-03 14:45:00'),
                                                                          (3, 100.00, 'Deposito', '2024-01-04 16:00:00'),
                                                                          (3, -50.00, 'Retiro', '2024-01-05 09:00:00'),
                                                                          (4, 200.00, 'Deposito', '2024-02-01 10:15:00'),
                                                                          (4, -100.00, 'Retiro', '2024-02-02 13:20:00'),
                                                                          (5, 500.00, 'Deposito', '2024-02-03 08:30:00'),
                                                                          (5, -50.00, 'Retiro', '2024-02-04 11:10:00'),
                                                                          (6, 300.00, 'Deposito', '2024-02-05 15:40:00'),
                                                                          (6, -100.00, 'Retiro', '2024-02-06 10:05:00'),
                                                                          (7, 1500.00, 'Deposito', '2024-03-01 14:00:00'),
                                                                          (7, -200.00, 'Retiro', '2024-03-02 09:30:00'),
                                                                          (8, 800.00, 'Deposito', '2024-03-03 13:25:00'),
                                                                          (8, -500.00, 'Retiro', '2024-03-04 16:50:00'),
                                                                          (9, 200.00, 'Deposito', '2024-04-01 08:15:00'),
                                                                          (9, -100.00, 'Retiro', '2024-04-02 10:00:00'),
                                                                          (10, 400.00, 'Deposito', '2024-04-03 12:30:00'),
                                                                          (10, -50.00, 'Retiro', '2024-04-04 15:45:00'),
                                                                          (11, 1000.00, 'Deposito', '2024-05-01 10:20:00'),
                                                                          (11, -200.00, 'Retiro', '2024-05-02 11:35:00');