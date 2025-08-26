-- Script de inicialización de la base de datos para el Sistema IAM
-- Este script se ejecuta automáticamente cuando se crea el contenedor de MySQL

-- Crear la base de datos si no existe (aunque ya se crea en docker-compose)
CREATE DATABASE IF NOT EXISTS crediya_iam;

-- Usar la base de datos
USE crediya_iam;

-- Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    birthdate DATE NOT NULL,
    identity_document VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    base_salary DECIMAL(10,2) NOT NULL,
    address TEXT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_identity_document ON users(identity_document);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_roles_name ON roles(name);

-- Insertar roles por defecto
INSERT INTO roles (name, description) VALUES 
    ('ADMIN', 'Administrador del sistema con todos los permisos'),
    ('USER', 'Usuario estándar del sistema'),
    ('MANAGER', 'Gerente con permisos de gestión'),
    ('EMPLOYEE', 'Empleado básico')
ON DUPLICATE KEY UPDATE 
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;

-- Insertar un usuario administrador por defecto para pruebas
INSERT INTO users (
    first_name, 
    last_name, 
    email, 
    birthdate, 
    identity_document, 
    phone_number, 
    base_salary, 
    address, 
    role_id
) VALUES (
    'Admin',
    'Sistema',
    'admin@crediya.com',
    '1990-01-01',
    '12345678',
    '+57 300 123 4567',
    5000000.00,
    'Calle Admin #123, Bogotá',
    1
) ON DUPLICATE KEY UPDATE 
    first_name = VALUES(first_name),
    updated_at = CURRENT_TIMESTAMP;

-- Verificar que las tablas se crearon correctamente
SELECT 'Tablas creadas correctamente:' as status;
SHOW TABLES;

-- Mostrar los roles insertados
SELECT 'Roles insertados:' as status;
SELECT * FROM roles;

-- Mostrar el usuario admin insertado
SELECT 'Usuario admin insertado:' as status;
SELECT u.*, r.name as role_name 
FROM users u 
JOIN roles r ON u.role_id = r.id 
WHERE u.email = 'admin@crediya.com';
