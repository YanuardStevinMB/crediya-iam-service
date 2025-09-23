# 🚀 IAM Service - Despliegue en AWS

## 📊 **Resumen de la Infraestructura Desplegada**

Tu aplicación **IAM Service** está completamente desplegada y funcionando en AWS con la siguiente arquitectura:

```
Internet → API Gateway → VPC Link → Network Load Balancer → ECS Fargate (2 tareas) → Aurora MySQL
```

---

## 🌐 **URLs y Endpoints Principales**

### **🔗 API Principal**
- **Base URL**: `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com`
- **Swagger UI**: `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/swagger-ui`
- **Health Check**: `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health`

### **📝 API Endpoints Disponibles**
- `POST /api/v1/usuarios` - Crear usuario (requiere autenticación: admin/admin123)
- `GET /actuator/health` - Health check ✅
- `GET /swagger-ui` - Documentación interactiva ✅
- `GET /v3/api-docs` - OpenAPI specification

### **🔐 Autenticación**
- **Tipo**: HTTP Basic Auth
- **Usuario**: `admin`
- **Password**: `admin123`

---

## 🏗️ **Arquitectura de AWS**

### **🔧 Componentes Principales**

| Componente | Tipo | Nombre | Estado | Descripción |
|------------|------|--------|--------|-------------|
| **API Gateway** | HTTP API | `uno0s0vk0f` | ✅ Activo | Punto de entrada público |
| **ECS Cluster** | Fargate | `iam-service-prod` | ✅ Activo | Contenedor de aplicaciones |
| **ECS Service** | Fargate | `iam-service-prod` | ✅ 2 tareas corriendo | Servicio de aplicación |
| **Load Balancer** | Network LB | `iam-service-prod-nlb` | ✅ Activo | Balanceador interno |
| **Base de Datos** | Aurora MySQL | `iam-service-prod-aurora` | ✅ Disponible | Cluster de BD |
| **ECR Repository** | Container Registry | `iam-service` | ✅ Disponible | Registro de imágenes |

### **🛡️ Seguridad y Redes**

| Recurso | Tipo | Descripción |
|---------|------|-------------|
| **VPC** | `vpc-069ca6b1b9b0eb468` | Red privada (10.0.0.0/16) |
| **Subredes Públicas** | 2 AZ | Para NAT Gateway |
| **Subredes Privadas** | 2 AZ | Para ECS y RDS |
| **Security Groups** | ECS + RDS | Configurados para comunicación segura |
| **NAT Gateway** | 1 instancia | Acceso a Internet desde subredes privadas |

### **🔐 Secrets y Configuración**

| Secret | ARN | Contenido |
|--------|-----|-----------|
| **DB Credentials** | `arn:aws:secretsmanager:us-east-2:889522049804:secret:iam-service-prod/db-ltU6Kx` | Usuario/password de Aurora |
| **JWT Secret** | `arn:aws:secretsmanager:us-east-2:889522049804:secret:iam-service-prod/jwt-wrRkkx` | Clave para tokens JWT |

---

## 🗄️ **Base de Datos**

### **📋 Configuración Aurora MySQL**
- **Endpoint**: `iam-service-prod-aurora.cluster-cpy6mqu0gnne.us-east-2.rds.amazonaws.com`
- **Puerto**: `3306`
- **Base de datos**: `iam_db`
- **Usuario**: `appmaster`
- **Instancias**: 2 (db.t3.medium)
- **Encriptación**: ✅ Habilitada
- **Backups**: 7 días de retención
- **Multi-AZ**: ✅ Alta disponibilidad

### **📊 Esquema de Base de Datos**

#### Tabla: `users`
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

#### Tabla: `roles`
```sql
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔧 **Monitoreo y Logs**

### **📈 CloudWatch**
- **Log Group**: `/ecs/iam-service-prod`
- **Alarmas configuradas**:
  - CPU > 80% (ECS)
  - Unhealthy targets (Load Balancer)
- **Métricas**: Auto Scaling basado en CPU (60% target)

### **📊 Auto Scaling**
- **Mínimo**: 2 tareas
- **Máximo**: 6 tareas
- **Target CPU**: 60%
- **Cool down**: 120 segundos

---

## 💰 **Costos Estimados (Mensual)**

| Servicio | Costo Estimado (USD/mes) |
|----------|--------------------------|
| ECS Fargate (2 tareas) | ~$30 |
| Aurora MySQL (2 instancias t3.medium) | ~$70 |
| API Gateway | ~$5 |
| Load Balancer | ~$20 |
| NAT Gateway | ~$32 |
| CloudWatch Logs | ~$2 |
| **TOTAL ESTIMADO** | **~$159/mes** |

---

## 🔄 **CI/CD Pipeline**

### **🚀 GitHub Actions**
El pipeline está configurado en `.github/workflows/deploy.yml`:

1. **Tests**: Ejecuta pruebas unitarias con Gradle
2. **Build**: Construye imagen Docker
3. **Push**: Sube imagen a ECR
4. **Deploy**: Actualiza servicio ECS
5. **Notify**: Confirma deployment exitoso

### **🔐 Secrets Requeridos en GitHub**
```bash
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AURORA_ENDPOINT=iam-service-prod-aurora.cluster-cpy6mqu0gnne.us-east-2.rds.amazonaws.com
AURORA_DATABASE=iam_db
AURORA_USERNAME=appmaster
AURORA_PASSWORD=iibWzA102xgNtOCOSr7<
```

---

## 🧪 **Testing de Endpoints**

### **✅ Health Check**
```bash
curl https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health
# Response: {"status":"UP","groups":["liveness","readiness"]}
```

### **📝 Crear Usuario**
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

---

## 📱 **Comandos Útiles**

### **🔍 Verificar Estado de Servicios**
```bash
# Estado del servicio ECS
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod

# Estado del Load Balancer
aws elbv2 describe-target-health --target-group-arn arn:aws:elasticloadbalancing:us-east-2:889522049804:targetgroup/iam-service-prod-tg/804936351ad79c93

# Estado de la base de datos
aws rds describe-db-clusters --db-cluster-identifier iam-service-prod-aurora
```

### **🚀 Deploy Manual**
```bash
# Forzar nuevo deployment
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment

# Verificar logs
aws logs tail /ecs/iam-service-prod --follow
```

### **🏗️ Reconstruir Infraestructura**
```bash
cd C:\Users\Usuario\terraform
terraform plan
terraform apply
```

---

## 🔧 **Configuración Local para Desarrollo**

### **🐳 Docker Compose Local**
```bash
cd C:\Users\Usuario\Documents\Yanuard\Pragma\IAM-SERVICE
start.bat  # Windows
# o
docker compose up --build -d  # Linux/Mac
```

### **📊 Servicios Locales**
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui
- Adminer: http://localhost:8081

---

## 📞 **Soporte y Mantenimiento**

### **🔧 Archivos de Configuración Importantes**
- **Terraform**: `C:\Users\Usuario\terraform\` (toda la infraestructura)
- **Docker**: `C:\Users\Usuario\Documents\Yanuard\Pragma\IAM-SERVICE\docker-compose.yml`
- **App Config**: `.env` (configuración de base de datos)
- **CI/CD**: `.github\workflows\deploy.yml`

### **📝 Para hacer cambios:**
1. Modifica el código en `C:\Users\Usuario\Documents\Yanuard\Pragma\IAM-SERVICE`
2. Haz commit y push a GitHub
3. El pipeline se ejecutará automáticamente
4. La aplicación se desplegará en AWS

### **🚨 En caso de problemas:**
1. Revisa logs en CloudWatch: `/ecs/iam-service-prod`
2. Verifica el estado del servicio ECS
3. Confirma que las tareas estén "healthy" en el Load Balancer

---

## ✅ **Estado Actual del Deployment**

- ✅ **Infraestructura**: Completamente desplegada
- ✅ **Aplicación**: Funcionando correctamente
- ✅ **Base de Datos**: Aurora MySQL disponible
- ✅ **API Gateway**: Respondiendo requests
- ✅ **Monitoreo**: CloudWatch configurado
- ✅ **CI/CD**: Pipeline listo para usar

**🎉 ¡Tu aplicación IAM Service está LIVE y funcionando en AWS!**