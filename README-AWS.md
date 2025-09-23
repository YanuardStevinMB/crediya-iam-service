# IAM-SERVICE - Configuración AWS

Este documento describe la configuración del IAM-SERVICE para despliegue en AWS ECS con Aurora MySQL.

## 🐳 Configuración Docker

### Variables de Entorno (AWS ECS)

Las siguientes variables se configuran automáticamente por Terraform:

```bash
# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080

# Database Configuration (Aurora MySQL)
DB_HOST={aurora-cluster-endpoint}
DB_PORT=3306
DB_NAME=iam_db
ADAPTERS_R2DBC_HOST={aurora-cluster-endpoint}
ADAPTERS_R2DBC_PORT=3306
ADAPTERS_R2DBC_DATABASE=iam_db

# Credenciales vía AWS Secrets Manager
DB_USERNAME={from-secrets-manager}
DB_PASSWORD={from-secrets-manager}
JWT_SECRET={from-secrets-manager}

# Swagger UI Configuration
SPRINGDOC_API_DOCS_PATH=/api/v1/api-docs
SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html
SPRINGDOC_SWAGGER_UI_ENABLED=true
SPRINGDOC_API_DOCS_ENABLED=true

# CORS Configuration
CORS_ALLOWED_ORIGINS=*

# Health Check Configuration
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info
MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true
```

## 🚀 Construcción y Despliegue

### 1. Construir imagen local
```bash
docker build -t crediya-iam-service .
```

### 2. Probar localmente (con variables de entorno)
```bash
# Crear archivo .env para testing local
cat > .env << EOF
SPRING_PROFILES_ACTIVE=docker
AURORA_ENDPOINT=your-aurora-endpoint
AURORA_DATABASE=iam_db
AURORA_USERNAME=your-username
AURORA_PASSWORD=your-password
JWT_SECRET=your-jwt-secret
CORS_ALLOWED_ORIGINS=*
EOF

# Ejecutar con docker-compose
docker-compose up
```

### 3. Desplegar a AWS ECR
```bash
# Obtener URL del repositorio
ECR_URI=$(aws ecr describe-repositories --repository-names crediya-iam-prod --query 'repositories[0].repositoryUri' --output text)

# Construir para AWS
docker build -t $ECR_URI:latest .

# Login a ECR
aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin $ECR_URI

# Subir imagen
docker push $ECR_URI:latest
```

## 🌐 Endpoints

Una vez desplegado en AWS, el servicio estará disponible en:

### Via API Gateway (Producción)
- Base URL: `https://{api-gateway-id}.execute-api.us-east-2.amazonaws.com/iam`
- Health Check: `/iam/actuator/health`
- Swagger UI: `/iam/swagger-ui.html`
- API Docs: `/iam/api/v1/api-docs`

### Via ALB (Testing)
- Base URL: `http://{alb-dns}/iam`
- Health Check: `/iam/actuator/health`
- Swagger UI: `/iam/swagger-ui.html`

## 🔍 Monitoreo

### Ver logs en CloudWatch
```bash
aws logs tail /ecs/crediya-prod-iam --follow
```

### Health Check
```bash
curl https://{api-gateway-url}/iam/actuator/health
```

### Ver métricas ECS
```bash
aws ecs describe-services --cluster crediya-prod --services crediya-prod-iam
```

## 🛠️ Configuración de Base de Datos

### Conexión a Aurora
- **Host**: Configurado automáticamente via variable `DB_HOST`
- **Puerto**: 3306
- **Base de datos**: `iam_db`
- **Credenciales**: Gestionadas por AWS Secrets Manager

### Obtener credenciales manualmente
```bash
aws secretsmanager get-secret-value --secret-id crediya-prod-iam/db
```

## 📋 Configuraciones Spring Boot Requeridas

Asegúrate de tener en tu `application-docker.yml`:

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  r2dbc:
    url: r2dbc:mysql://${ADAPTERS_R2DBC_HOST}:${ADAPTERS_R2DBC_PORT}/${ADAPTERS_R2DBC_DATABASE}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

springdoc:
  api-docs:
    path: ${SPRINGDOC_API_DOCS_PATH:/api/v1/api-docs}
    enabled: ${SPRINGDOC_API_DOCS_ENABLED:true}
  swagger-ui:
    path: ${SPRINGDOC_SWAGGER_UI_PATH:/swagger-ui.html}
    enabled: ${SPRINGDOC_SWAGGER_UI_ENABLED:true}

management:
  endpoints:
    web:
      exposure:
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info}
  endpoint:
    health:
      probes:
        enabled: ${MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED:true}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:*}
```

## 🔧 Troubleshooting

### Problema: Servicio no arranca
1. Verificar logs: `aws logs tail /ecs/crediya-prod-iam --follow`
2. Verificar que Aurora está disponible
3. Verificar secrets en AWS Secrets Manager

### Problema: No puede conectar a base de datos
1. Verificar security groups
2. Verificar que Aurora está en VPC correcta
3. Verificar credenciales en Secrets Manager

### Problema: Health check falla
1. Verificar que `/actuator/health` está habilitado
2. Verificar que el puerto 8080 está expuesto
3. Verificar configuración del target group