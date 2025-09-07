# Sistema de Gestión de Usuarios - IAM (Identity and Access Management)

## Descripción del Proyecto

Sistema de autenticación y gestión de usuarios implementado con **Clean Architecture** utilizando Spring Boot con WebFlux (programación reactiva), R2DBC para acceso a base de datos MySQL de forma no bloqueante, y Spring Security para autenticación.

## 🚀 Características Principales

- **Arquitectura Limpia (Clean Architecture)**: Separación clara de responsabilidades
- **Programación Reactiva**: Utilizando Spring WebFlux
- **Base de Datos No Bloqueante**: R2DBC con MySQL
- **Seguridad**: Spring Security con autenticación básica
- **Documentación API**: OpenAPI 3.0 con Swagger UI
- **Gestión Transaccional**: Implementación de transacciones reactivas
- **Validaciones**: Bean Validation (Jakarta Validation)
- **Manejo de Errores**: Filtros personalizados para respuestas consistentes

## 🌐 API Endpoints

### Base URL
```
http://localhost:8080
```

### Usuarios

#### Crear Usuario
- **Endpoint**: `POST /api/v1/usuarios`
- **Descripción**: Crea un nuevo usuario en el sistema
- **Content-Type**: `application/json`
- **Autenticación**: Basic Auth (`admin:admin123`)

**Request Body:**
```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com",
  "birthdate": "1990-05-15",
  "identityDocument": "12345678",
  "phoneNumber": "+57 300 123 4567",
  "baseSalary": 2500000.00,
  "address": "Calle 123 #45-67, Bogotá",
  "roleId": 1
}
```

**Response Success (200):**
```json
{
  "success": true,
  "message": "Usuario creado correctamente",
  "data": {
    "id": 1,
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@example.com",
    "birthdate": "1990-05-15",
    "identityDocument": "12345678",
    "phoneNumber": "+57 300 123 4567",
    "baseSalary": 2500000.00,
    "address": "Calle 123 #45-67, Bogotá",
    "roleId": 1
  },
  "path": "/api/v1/usuarios",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Response Error (400 - Validación):**
```json
{
  "success": false,
  "message": "Error de validación",
  "errors": [
    "El campo 'email' es obligatorio.",
    "El formato de 'email' no es válido."
  ],
  "path": "/api/v1/usuarios",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Response Error (409 - Email Duplicado):**
```json
{
  "success": false,
  "message": "Email duplicado",
  "path": "/api/v1/usuarios",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 📖 Documentación API (Swagger)

### Acceso a Swagger UI
- **URL**: `http://localhost:8080/swagger-ui`
- **Descripción**: Interfaz interactiva para explorar y probar los endpoints de la API
- **Autenticación**: Requerida (admin/admin123)

### OpenAPI Specification
- **URL**: `http://localhost:8080/v3/api-docs`
- **Formato**: JSON
- **Descripción**: Especificación completa de la API en formato OpenAPI 3.0

## 🚀 Inicio Rápido con Docker (Recomendado)

### ⚡ Ejecución en Un Solo Comando

**Windows:**
```cmd
# Ejecutar script de inicio automático
start.bat
```

**Linux/macOS:**
```bash
# Iniciar todos los servicios
docker compose up --build -d
```

### 🌐 Servicios Disponibles

Tras ejecutar Docker, tendrás acceso a:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **🚀 API REST** | http://localhost:8080 | API principal del sistema |
| **💚 Health Check** | http://localhost:8080/actuator/health | Estado de la aplicación |
| **📚 Swagger UI** | http://localhost:8080/swagger-ui | Documentación interactiva de API |
| **🗄️ Adminer** | http://localhost:8081 | Interfaz web para gestión de BD |

### 📋 Requisitos Docker
- **Docker**: v20.10 o superior
- **Docker Compose**: v2.0 o superior
- **4GB RAM libre**: Mínimo recomendado
- **Puertos libres**: 8080, 3307, 8081

**📖 Para documentación detallada de Docker, ver: [DOCKER_GUIDE.md](DOCKER_GUIDE.md)**

---

## 🔧 Ejecución Manual (Sin Docker)

### Requisitos Previos
- Java 21 o superior
- MySQL 8.0
- Gradle 7.0+

### Variables de Entorno
```bash
SPRING_PROFILES_ACTIVE=local  # Perfil por defecto
```

### Configuración Manual
```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd reto

# 2. Configurar base de datos MySQL
# Crear base de datos: crediya_iam
# Usuario: root, Password: rootpassword

# 3. Compilar y ejecutar
./gradlew bootRun

# La aplicación estará disponible en:
http://localhost:8081
```

### Configuración de Base de Datos Manual
La configuración de la base de datos se encuentra en `applications/app-service/src/main/resources/application-local.yaml`. 

**Configuración por defecto:**
```yaml
adapters:
  r2dbc:
    host: localhost
    port: 3306
    database: crediya_iam
    username: root
    password: rootpassword
```

## 🏗️ Arquitectura del Proyecto

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

## 🔍 Detalles Técnicos de Implementación

### Stack Tecnológico
- **Spring Boot 3.x**: Framework principal
- **Spring WebFlux**: Programación reactiva
- **Spring Security**: Autenticación y autorización
- **R2DBC**: Acceso reactivo a base de datos
- **MySQL**: Base de datos relacional
- **SpringDoc OpenAPI**: Documentación automática de API
- **Bean Validation**: Validaciones declarativas
- **Project Reactor**: Programación reactiva con Mono y Flux
- **Gradle**: Herramienta de construcción

### Funcionalidades Implementadas

#### ✅ Gestión de Usuarios
- Creación de usuarios con validaciones completas
- Validación de email único
- Gestión de roles y permisos
- Transacciones reactivas para operaciones complejas

#### ✅ Seguridad
- Autenticación HTTP Basic
- Configuración de CORS
- Filtros de seguridad personalizados

#### ✅ Manejo de Errores
- Filtro global de errores (`ApiErrorFilter`)
- Respuestas estandarizadas (`ApiResponse`)
- Manejo específico de errores de validación
- Códigos de estado HTTP apropiados

#### ✅ Validaciones
- Validaciones en campos obligatorios
- Formato de email
- Rangos de salario (0 - 15,000,000)
- Formato de número telefónico
- Fecha de nacimiento (debe ser pasada)
- Longitud máxima de campos

### Arquitectura de Capas

```
📁 applications/app-service/          # Configuración y punto de entrada
├── 📁 config/                       # Configuraciones de Spring
├── 📁 resources/                    # application.yaml

📁 domain/                           # Lógica de negocio
├── 📁 model/                        # Entidades del dominio
├── 📁 usecase/                      # Casos de uso
│   ├── 📁 user/                     # Casos de uso de usuario
│   └── 📁 gateway/                  # Interfaces de adaptadores

📁 infrastructure/                   # Implementaciones técnicas
├── 📁 driven-adapters/
│   └── 📁 r2dbc-mysql/             # Adaptador de base de datos
└── 📁 entry-points/
    └── 📁 reactive-web/             # API REST reactiva
```

### Patrones de Diseño Utilizados

1. **Clean Architecture**: Separación de responsabilidades en capas
2. **Repository Pattern**: Abstracción del acceso a datos
3. **Gateway Pattern**: Interfaz para servicios externos
4. **DTO Pattern**: Objetos de transferencia de datos
5. **Builder Pattern**: Construcción de objetos complejos
6. **Strategy Pattern**: Manejo de diferentes estrategias de validación

### Monitoreo y Observabilidad

- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`
- **Logs**: Configurados en `logs/iam-app.log`

### Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Tests con reporte de cobertura
./gradlew test jacocoTestReport
```

### Estructura de Base de Datos

#### Tabla: users
```sql
CREATE TABLE users (
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### Tabla: roles
```sql
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📝 Changelog de Cambios Recientes

### ✨ Nuevas Funcionalidades
- Implementación completa del sistema de gestión de usuarios
- Configuración de seguridad con Spring Security
- Documentación automática con OpenAPI/Swagger
- Manejo transaccional reactivo
- Sistema de validaciones robusto
- Filtros de error globales
- Mappers para conversión de DTOs
- Tests unitarios e integración
- Configuración de CORS
- Monitoreo con Actuator

### 🔧 Mejoras Técnicas
- Migración de PostgreSQL a MySQL
- Refactorización de casos de uso
- Implementación del patrón Gateway para transacciones
- Optimización de consultas R2DBC
- Configuración mejorada de logging
- Estandarización de respuestas API

### 🐛 Correcciones
- Validaciones de campos mejoradas
- Manejo correcto de excepciones
- Configuración de encoding de archivos
- Tests actualizados y funcionales

---

## 📞 Contacto y Soporte

Para preguntas técnicas o soporte, contactar al equipo de desarrollo.

**Documentación adicional**: [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)
