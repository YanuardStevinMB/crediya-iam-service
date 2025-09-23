# 🔐 Configurar GitHub Actions - Guía Paso a Paso

## 1. 📋 **Configurar Secrets en GitHub**

Para que el pipeline CI/CD funcione, necesitas configurar estos secrets en tu repositorio de GitHub:

### **Paso 1: Ir a la configuración del repositorio**
1. Ve a tu repositorio en GitHub
2. Click en **Settings** (Configuración)
3. En el menú lateral, click en **Secrets and variables** → **Actions**
4. Click en **New repository secret**

### **Paso 2: Agregar los siguientes secrets**

| Secret Name | Value |
|------------|-------|
| `AWS_ACCESS_KEY_ID` | (Tu Access Key ID de AWS) |
| `AWS_SECRET_ACCESS_KEY` | (Tu Secret Access Key de AWS) |
| `AURORA_ENDPOINT` | `iam-service-prod-aurora.cluster-cpy6mqu0gnne.us-east-2.rds.amazonaws.com` |
| `AURORA_DATABASE` | `iam_db` |
| `AURORA_USERNAME` | `appmaster` |
| `AURORA_PASSWORD` | `iibWzA102xgNtOCOSr7<` |

### **Paso 3: Obtener AWS Credentials**

Si no tienes las credenciales de AWS, puedes obtenerlas así:

```bash
# Ver las credenciales actuales
aws configure list

# Ver el Access Key ID actual
aws sts get-caller-identity
```

O desde la consola de AWS:
1. IAM → Users → Tu usuario → Security credentials → Access keys

---

## 2. 🗄️ **Setup Inicial de Base de Datos**

Tu base de datos Aurora está corriendo pero puede necesitar las tablas iniciales. Aquí tienes los comandos:

### **Script SQL para ejecutar:**

```sql
-- Conectar a la base de datos iam_db
USE iam_db;

-- Crear tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar roles básicos
INSERT IGNORE INTO roles (id, name, description) VALUES 
(1, 'ADMIN', 'Administrator role with full access'),
(2, 'USER', 'Standard user role'),
(3, 'MANAGER', 'Manager role with elevated permissions');

-- Crear tabla de usuarios
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
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Verificar que las tablas se crearon correctamente
SHOW TABLES;
SELECT * FROM roles;
```

### **Cómo ejecutar el script:**

**Opción 1: Usando MySQL Workbench o similar**
1. Conectar a: `iam-service-prod-aurora.cluster-cpy6mqu0gnne.us-east-2.rds.amazonaws.com:3306`
2. Usuario: `appmaster`
3. Password: `iibWzA102xgNtOCOSr7<`
4. Ejecutar el script SQL

**Opción 2: Usando línea de comandos**
```bash
# Instalar MySQL client si no lo tienes
# Windows: https://dev.mysql.com/downloads/mysql/
# Ubuntu: sudo apt install mysql-client
# MacOS: brew install mysql-client

mysql -h iam-service-prod-aurora.cluster-cpy6mqu0gnne.us-east-2.rds.amazonaws.com -P 3306 -u appmaster -p iam_db < setup.sql
```

**Opción 3: Desde Adminer (interfaz web)**
Si tienes Adminer corriendo localmente:
1. Ve a http://localhost:8081
2. Conectar con los datos de Aurora
3. Ejecutar el script

---

## 3. 🚀 **Probar el Pipeline**

Una vez configurados los secrets:

1. **Haz cualquier cambio en tu código**
2. **Commit y push a GitHub**:
   ```bash
   git add .
   git commit -m "Setup CI/CD pipeline"
   git push origin main
   ```
3. **Ve a la pestaña "Actions" en GitHub** para ver el pipeline ejecutándose

---

## 4. 🧪 **Verificar que todo funciona**

### **Test del endpoint de crear usuario:**

```bash
curl -X POST https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@example.com",
    "birthdate": "1990-05-15",
    "identityDocument": "12345678",
    "phoneNumber": "+57 300 123 4567",
    "baseSalary": 2500000.00,
    "address": "Calle 123 #45-67, Bogotá",
    "roleId": 1
  }'
```

### **Test de otros endpoints:**

```bash
# Health Check
curl https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health

# Swagger UI (en el navegador)
https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/swagger-ui
```

---

## 5. 📱 **Comandos útiles para debugging**

```bash
# Ver logs de la aplicación
aws logs tail /ecs/iam-service-prod --follow

# Estado del servicio ECS
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod

# Forzar nuevo deployment (si algo no funciona)
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment
```

---

## ✅ **Checklist final:**

- [ ] Secrets configurados en GitHub
- [ ] Base de datos inicializada con tablas y roles
- [ ] Pipeline CI/CD probado con un commit
- [ ] API endpoints funcionando correctamente
- [ ] Swagger UI accesible

**¡Una vez completado esto, tendrás un pipeline completamente automatizado!** 🎉