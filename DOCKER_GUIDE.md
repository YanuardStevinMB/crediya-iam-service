# 🐳 Guía de Dockerización - Sistema IAM Crediya

## 📋 Tabla de Contenidos

- [Introducción](#introducción)
  - [Arquitectura Docker](#arquitectura-docker)
  - [Pre-requisitos](#pre-requisitos)
  - [Configuración Inicial](#configuración-inicial)
  - [Construcción y Ejecución](#construcción-y-ejecución)
  - [Verificación del Sistema](#verificación-del-sistema)
  - [Gestión de la Base de Datos](#gestión-de-la-base-de-datos)
  - [Comandos Útiles](#comandos-útiles)
  - [Troubleshooting](#troubleshooting)
  - [Configuración de Producción](#configuración-de-producción)

## 🌟 Introducción

Este proyecto ha sido completamente dockerizado para facilitar el desarrollo, testing y despliegue del Sistema IAM (Identity and Access Management) de Crediya. La solución incluye:

- **Aplicación Spring Boot** con arquitectura reactiva
  - **Base de datos MySQL 8.4** con inicialización automática
  - **Adminer** para gestión visual de la base de datos
  - **Configuración de red Docker** para comunicación entre servicios
  - **Health checks** para monitoreo automático
  - **Variables de entorno** para configuración flexible

## 🏗️ Arquitectura Docker

```
📦 Contenedores Docker
├── 🗄️ mysql-iam          # Base de datos MySQL 8.4
├── 🚀 iam-service         # Aplicación Spring Boot
└── 🌐 adminer             # Interfaz web para DB (opcional)

🌐 Red: iam-network (bridge)
💾 Volumen: mysql-iam-data (persistente)
```

## ✅ Pre-requisitos

Antes de comenzar, asegúrate de tener instalado:

- **Docker**: v20.10 o superior
  - **Docker Compose**: v2.0 o superior  
  - **Git**: Para clonar el repositorio
  - **4GB RAM libre**: Mínimo recomendado
  - **Puertos disponibles**: 8080, 3307, 8081

### Verificar Instalación

```bash
# Verificar Docker
docker --version
docker compose --version

# Verificar que Docker está ejecutándose
docker info
```

## ⚙️ Configuración Inicial

### 1. Clonar y Navegar al Proyecto

```bash
git clone <repository-url>
cd reto
```

### 2. Configurar Variables de Entorno

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar variables según tu entorno
notepad .env  # Windows
nano .env     # Linux/macOS
```

### 3. Variables de Entorno Principales

```bash
# Base de datos
MYSQL_ROOT_PASSWORD=root_secure_password_2024
MYSQL_DATABASE=crediya_autenticacion
MYSQL_USER=autenticacion
MYSQL_PASSWORD=autenticacion_secure_2024
MYSQL_PORT=3307

# Aplicación
IAM_SERVICE_PORT=8080
ADMINER_PORT=8081

# Seguridad
JWT_SECRET=QnE1T2lXbVRhV3RzR2VOUXlHaFZ2d2dyU2p2a1R2TnM=
JWT_EXPIRATION=3600

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:8080
```

## 🚀 Construcción y Ejecución

### Opción 1: Inicialización Completa (Recomendado)

```bash
# Construir y ejecutar todos los servicios
docker compose up --build

# Para ejecutar en segundo plano
docker compose up --build -d
```

### Opción 2: Construcción por Pasos

```bash
# 1. Construir solo las imágenes
docker compose build

# 2. Iniciar solo la base de datos primero
docker compose up mysql-iam -d

# 3. Esperar a que la BD esté lista y luego iniciar la aplicación
docker compose up iam-service -d

# 4. Iniciar Adminer (opcional)
docker compose up adminer -d
```

### Opción 3: Solo la Aplicación (sin Adminer)

```bash
docker compose up mysql-iam iam-service --build -d
```

## ✅ Verificación del Sistema

### 1. Verificar Estado de los Contenedores

```bash
# Ver todos los contenedores
docker compose ps

# Ver logs en tiempo real
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f iam-service
```
### 2. pruebas de persistencia de datos

```bash
#Se detiene y elimina solo el contenedor (el volumen se conserva):
docker compose -f docker-compose.yml down

# Verificar salud de la aplicación
docker compose -f docker-compose.yml down
```


### 2. Health Checks

```bash
# Verificar salud de la base de datos
docker compose exec mysql-iam mysqladmin ping -p

# Verificar salud de la aplicación
curl http://localhost:8080/actuator/health
```

### 3. Endpoints de la Aplicación

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **API REST** | http://localhost:8080 | API principal del sistema |
| **Health Check** | http://localhost:8080/actuator/health | Estado de la aplicación |
| **Swagger UI** | http://localhost:8080/swagger-ui | Documentación interactiva |
| **Adminer** | http://localhost:8081 | Gestión visual de BD |

### 4. Probar la API

```bash
# Ejemplo: Crear un usuario (requiere autenticación Basic: admin/admin123)
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@crediya.com",
    "birthdate": "1990-01-01",
    "identityDocument": "987654321",
    "phoneNumber": "+57 300 999 8888",
    "baseSalary": 3000000.00,
    "address": "Calle Test #123",
    "roleId": 2
  }'
```

## 🗄️ Gestión de la Base de Datos

### Conexión mediante Adminer

1. Abrir http://localhost:8081
   2. Usar las credenciales:
      - **Sistema**: MySQL
      - **Servidor**: mysql-iam
      - **Usuario**: autenticacion
      - **Contraseña**: (definida en .env)
      - **Base de datos**: crediya_autenticacion

### Conexión Directa

```bash
# Conectarse desde la línea de comandos
docker compose exec mysql-iam mysql -u autenticacion -p crediya_autenticacion
```

### Scripts de Inicialización

Los scripts en `docker/init-scripts/` se ejecutan automáticamente:

- `01-init-database.sql`: Crea tablas y estructura básica
  - `02-sample-data.sql`: Inserta datos de ejemplo para testing

### Backup y Restore

```bash
# Crear backup
docker compose exec mysql-iam mysqldump -u autenticacion -p crediya_autenticacion > backup.sql

# Restaurar backup
docker compose exec -T mysql-iam mysql -u autenticacion -p crediya_autenticacion < backup.sql
```

## 🛠️ Comandos Útiles

### Gestión de Contenedores

```bash
# Ver estado de todos los servicios
docker compose ps

# Detener todos los servicios
docker compose down

# Detener y eliminar volúmenes (¡CUIDADO! Elimina datos)
docker compose down -v

# Reiniciar un servicio específico
docker compose restart iam-service

# Ver uso de recursos
docker stats
```

### Logs y Debugging

```bash
# Logs de todos los servicios
docker compose logs -f

# Logs de un servicio específico
docker compose logs -f mysql-iam

# Logs de los últimos 100 líneas
docker compose logs --tail=100 iam-service

# Conectarse a un contenedor
docker compose exec iam-service sh
```

### Limpieza del Sistema

```bash
# Eliminar contenedores, redes y volúmenes del proyecto
docker compose down -v

# Limpiar imágenes no utilizadas
docker image prune -f

# Limpiar todo el sistema Docker (¡CUIDADO!)
docker system prune -a
```

## 🚨 Troubleshooting

### Problemas Comunes

#### 1. Puerto ya en uso

```bash
Error: bind: address already in use
```

**Solución:**
```bash
# Verificar qué procesos usan los puertos
netstat -tulpn | grep :8080
netstat -tulpn | grep :3307

# Cambiar puertos en .env o detener procesos conflictivos
```

#### 2. La aplicación no puede conectar a la BD

**Verificaciones:**
```bash
# 1. Verificar que MySQL esté healthy
docker compose ps

# 2. Ver logs de la base de datos
docker compose logs mysql-iam

# 3. Verificar conectividad de red
docker compose exec iam-service ping mysql-iam
```

#### 3. Problemas de memoria

```bash
# Ver uso de memoria
docker stats

# Aumentar memoria disponible para Docker
# (Docker Desktop > Settings > Resources > Memory)
```

#### 4. Problemas de permisos

```bash
# En Linux/macOS, verificar permisos de archivos
ls -la docker/init-scripts/
chmod +x docker/init-scripts/*.sql
```

### Logs de Depuración

```bash
# Habilitar logs detallados
export COMPOSE_LOG_LEVEL=DEBUG
docker compose up --build

# Ver logs específicos de construcción
docker compose build --progress=plain
```

## 🔒 Configuración de Producción

### Variables de Entorno de Producción

```bash
# .env.production
MYSQL_ROOT_PASSWORD=super_secure_root_password
MYSQL_PASSWORD=production_secure_password
JWT_SECRET=production_jwt_secret_256_bits
ENVIRONMENT=production
LOG_LEVEL=WARN
```

### Docker Compose Override

Crear `docker-compose.override.yml`:

```yaml
version: "3.9"

services:
  iam-service:
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: production
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M

  mysql-iam:
    restart: always
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
```

### Comandos de Producción

```bash
# Ejecutar en modo producción
docker compose -f docker-compose.yml -f docker-compose.override.yml up -d

# Monitoreo continuo
docker compose logs -f --tail=50

# Backup automático
docker compose exec mysql-iam mysqldump -u autenticacion -p crediya_autenticacion | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

## 📞 Soporte y Contacto

Para issues técnicos:

1. **Verificar logs**: `docker compose logs -f`
   2. **Revisar health checks**: `curl http://localhost:8080/actuator/health`
   3. **Consultar documentación**: `http://localhost:8080/swagger-ui`

---

