# 🏗️ **IAM SERVICE - INFRAESTRUCTURA COMPLETA**

## 📋 **INFORMACIÓN GENERAL**

### **🌐 URLs PRINCIPALES FUNCIONANDO**

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **🔥 Swagger UI (PRODUCCIÓN)** | [`https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/webjars/swagger-ui/index.html`](https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/webjars/swagger-ui/index.html#/IAM%20API/listUsers) | **URL PRINCIPAL - FUNCIONA 100%** |
| **📊 API Documentation** | `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/v3/api-docs` | Documentación OpenAPI |
| **❤️ Health Check** | `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health` | Monitoreo de salud |
| **🔐 Login Endpoint** | `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/api/v1/login` | Autenticación JWT |
| **👥 Users Endpoint** | `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/api/v1/usuarios` | Gestión de usuarios |

### **🧪 URLs DE DESARROLLO/TESTING**

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Swagger UI (TEST)** | `http://iam-service-test-alb-471938719.us-east-2.elb.amazonaws.com/swagger-ui` | ALB de prueba HTTP |
| **API Documentation (TEST)** | `http://iam-service-test-alb-471938719.us-east-2.elb.amazonaws.com/v3/api-docs` | Docs en ALB de prueba |

---

## 🏛️ **ARQUITECTURA DE SERVICIOS**

### **1. 🌐 API GATEWAY**
**ARN**: `arn:aws:apigateway:us-east-2::/restapis/uno0s0vk0f`  
**URL**: `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com`

**🎯 Función**: 
- **Punto de entrada principal** para todas las peticiones HTTPS
- **Gestión de SSL/TLS** automática
- **Rate limiting** y throttling
- **API versioning** y gestión de stages
- **Logging** y monitoreo de requests

**📡 Configuración**:
```yaml
Scheme: HTTPS
Stage: default
Methods: ANY
Path: /{proxy+}
Integration: NLB Target
```

---

### **2. ⚖️ NETWORK LOAD BALANCER (NLB)**
**Name**: `iam-service-prod-nlb`  
**DNS**: `iam-service-prod-nlb-c0dd25bfe56a1e63.elb.us-east-2.amazonaws.com`  
**Type**: Internal (Private)

**🎯 Función**:
- **Balanceador de carga TCP** interno
- **Alta disponibilidad** entre múltiples AZs
- **Health checks** a nivel TCP
- **Routing** del tráfico del API Gateway hacia ECS

**📡 Configuración**:
```yaml
Scheme: internal
Type: network
Port: 80
Protocol: TCP
Target Type: IP
Health Check: TCP:8080
```

---

### **3. ⚖️ APPLICATION LOAD BALANCER (ALB) - TESTING**
**Name**: `iam-service-test-alb`  
**DNS**: `iam-service-test-alb-471938719.us-east-2.elb.amazonaws.com`  
**Type**: Internet-facing (Public)

**🎯 Función**:
- **ALB público para testing HTTP**
- **Desarrollo y debugging** sin HTTPS
- **Health checks HTTP** avanzados
- **Path-based routing** (si es necesario)

**📡 Configuración**:
```yaml
Scheme: internet-facing
Type: application
Port: 80
Protocol: HTTP
Target Type: IP
Health Check: HTTP:8080/actuator/health
```

---

### **4. 🐳 ELASTIC CONTAINER SERVICE (ECS)**
**Cluster**: `iam-service-prod`  
**Service**: `iam-service-prod`  
**Launch Type**: Fargate

**🎯 Función**:
- **Orquestación de contenedores** serverless
- **Auto scaling** basado en CPU/memoria
- **Rolling deployments** sin downtime
- **Service discovery** y networking
- **Integration** con ECR para imágenes

**📡 Configuración**:
```yaml
Desired Count: 2
Min Capacity: 1  
Max Capacity: 10
CPU: 512
Memory: 1024
Port: 8080
Subnets: Private (subnet-0bf0cb7889a31b90d, subnet-0f6ece25bed275ef1)
```

**📦 Task Definition**:
```json
{
  "family": "iam-service-prod",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [{
    "name": "iam-service-prod",
    "image": "889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service:latest",
    "portMappings": [{
      "containerPort": 8080,
      "protocol": "tcp"
    }],
    "environment": [
      {"name": "SPRING_PROFILES_ACTIVE", "value": "prod"}
    ]
  }]
}
```

---

### **5. 🏪 ELASTIC CONTAINER REGISTRY (ECR)**
**Repository**: `iam-service`  
**URI**: `889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service`

**🎯 Función**:
- **Almacén privado** de imágenes Docker
- **Versionado** de imágenes por tags
- **Security scanning** de vulnerabilidades
- **Integration** con ECS para deployments

**📦 Images**:
```bash
# Latest deployment
889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service:latest
# SHA256: sha256:8b0a29a7eaa9961024dac018f2a26274d2fc20f48e8310c55884e1a35f165309
```

---

### **6. 🎯 TARGET GROUPS**

#### **📍 NLB Target Group**
**Name**: `iam-service-prod-tg`  
**Type**: IP  
**Protocol**: TCP  
**Port**: 8080

**🎯 Función**: 
- **Health checking** TCP para NLB
- **Target registration** automático desde ECS
- **Connection draining** durante deployments

#### **📍 ALB Target Group (Testing)**
**Name**: `iam-service-test-tg`  
**Type**: IP  
**Protocol**: HTTP  
**Port**: 8080

**🎯 Función**:
- **Health checking HTTP** avanzado (`/actuator/health`)
- **HTTP status code monitoring**
- **Development testing** y debugging

---

### **7. 🔒 SECURITY GROUPS**

#### **🛡️ ECS Security Group**
**ID**: `sg-0fb4ff58b782c9e11`
```yaml
Ingress Rules:
  - Port: 8080, Protocol: TCP, Source: VPC (10.0.0.0/16)
  - Port: 8080, Protocol: TCP, Source: ALB SG (sg-0b938a1cc4caafb6b)
Egress Rules:
  - All traffic allowed
```

#### **🛡️ ALB Security Group**
**ID**: `sg-0b938a1cc4caafb6b`
```yaml
Ingress Rules:
  - Port: 80, Protocol: TCP, Source: 0.0.0.0/0
Egress Rules:
  - Port: 8080, Protocol: TCP, Target: ECS SG (sg-0fb4ff58b782c9e11)
  - All other traffic allowed
```

---

### **8. 🌐 VPC y NETWORKING**

#### **🏠 Virtual Private Cloud**
**VPC ID**: `vpc-069ca6b1b9b0eb468`  
**CIDR**: `10.0.0.0/16`

#### **🔗 Subnets**

| Subnet | Type | CIDR | AZ | Usage |
|--------|------|------|----|---------| 
| `subnet-0bf0cb7889a31b90d` | Private | `10.0.101.0/24` | us-east-2b | ECS Tasks |
| `subnet-0f6ece25bed275ef1` | Private | `10.0.100.0/24` | us-east-2a | ECS Tasks |
| `subnet-04f16b206a83a5c3f` | Public | `10.0.1.0/24` | us-east-2b | ALB Testing |
| `subnet-0fd566316e32f278d` | Public | `10.0.0.0/24` | us-east-2a | ALB Testing |

---

### **9. 🗄️ DATABASE (RDS Aurora)**
**Endpoint**: [Configurado en Secrets Manager]  
**Engine**: MySQL 8.0  
**Type**: Aurora Serverless v2

**🎯 Función**:
- **Base de datos principal** para usuarios y autenticación
- **Multi-AZ deployment** para alta disponibilidad
- **Auto-scaling** basado en demanda
- **Backups automáticos** y point-in-time recovery

---

### **10. 🔐 SECRETS MANAGEMENT**

#### **🔑 AWS Secrets Manager**
- **Database credentials**
- **JWT secrets**
- **API keys** y tokens

#### **🛡️ IAM Roles**
```yaml
ECS Task Role:
  - AmazonECSTaskExecutionRolePolicy
  - SecretsManagerReadWrite
  - CloudWatchLogsFullAccess
```

---

### **11. 📊 MONITORING y LOGGING**

#### **📈 CloudWatch**
- **ECS Service metrics** (CPU, Memory, Tasks)
- **ALB metrics** (Request count, Latency, Error rates)
- **Custom application logs**
- **Alarms** para auto-scaling

#### **🔍 Application Insights**
- **Health endpoints monitoring**
- **Performance metrics**
- **Error tracking**

---

## 🚀 **PROCESO DE DEPLOYMENT**

### **📦 Build Process**
```bash
# 1. Build Docker Image
docker build -t iam-service-prod .

# 2. Tag for ECR
docker tag iam-service-prod:latest 889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service:latest

# 3. Login to ECR
aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 889522049804.dkr.ecr.us-east-2.amazonaws.com

# 4. Push to ECR
docker push 889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service:latest

# 5. Force ECS Deployment
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment
```

### **🔄 CI/CD Pipeline**
```yaml
GitHub Actions:
  - Trigger: Push to main branch
  - Steps: Test → Build → Push to ECR → Deploy to ECS
  - Secrets: AWS credentials, ECR registry
```

---

## ⚙️ **CONFIGURACIÓN DE LA APLICACIÓN**

### **🐳 Docker Configuration**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/project/applications/app-service/build/libs/*.jar app.jar
EXPOSE 8080
USER spring
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **📋 Spring Boot Configuration**
```yaml
server:
  port: 8080  # Container port

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:prod}

springdoc:
  api-docs.path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus

cors:
  allowed-origins: "*"  # Permissive for development

security:
  jwt:
    secret: [SECRETS_MANAGER]
    expiration-sec: 3600
```

### **🔒 Security Headers**
```java
Content-Security-Policy: default-src * 'unsafe-inline' 'unsafe-eval' data: blob: ws: wss:; 
                        script-src * 'unsafe-inline' 'unsafe-eval'; 
                        style-src * 'unsafe-inline';
X-Content-Type-Options: nosniff
Cache-Control: no-store
Referrer-Policy: strict-origin-when-cross-origin
```

---

## 🧪 **TESTING y DEBUGGING**

### **🔍 Health Checks**
```bash
# Primary endpoint
curl -X GET "https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health"

# Test ALB endpoint  
curl -X GET "http://iam-service-test-alb-471938719.us-east-2.elb.amazonaws.com/actuator/health"
```

### **📊 API Documentation**
```bash
# Get OpenAPI spec
curl -X GET "https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/v3/api-docs"
```

### **🔐 Authentication Test**
```bash
# Login (expect 401 without credentials)
curl -X GET "https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/api/v1/users/exist"
```

---

## 📈 **MONITORING y ALERTING**

### **🚨 CloudWatch Alarms**
- **High CPU Usage** (>80% for 5 minutes)
- **High Memory Usage** (>80% for 5 minutes) 
- **HTTP 5xx Errors** (>10 in 5 minutes)
- **Response Latency** (>2s average)

### **📊 Key Metrics**
- **ECS Service**: Running tasks, CPU, Memory
- **ALB**: Request count, Response time, Error rate
- **API Gateway**: Request count, Integration latency, Errors

---

## 🔧 **TROUBLESHOOTING**

### **🆘 Common Issues**

#### **❌ 503 Service Unavailable**
```bash
# Check ECS service status
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod

# Check target health
aws elbv2 describe-target-health --target-group-arn [TARGET_GROUP_ARN]
```

#### **❌ Mixed Content Errors**
- **Solución**: Swagger UI ahora incluye múltiples servidores (HTTP y HTTPS)
- **URL correcta**: Use `https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/webjars/swagger-ui/index.html`

#### **❌ CSP Blocking**
- **Configuración**: CSP actualizado para permitir Swagger UI
- **Headers**: Permissive policy aplicada en SecurityHeadersConfig.java

### **🔍 Debugging Commands**
```bash
# Check ECS logs
aws logs tail /ecs/iam-service-prod --follow

# Check service deployment status
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod --query "services[0].deployments[0]"

# Force new deployment
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment
```

---

## 📝 **CHANGELOG**

### **✅ Version Actual (2025-09-20)**
- ✅ **CSP Issues Fixed**: Swagger UI carga sin errores
- ✅ **Mixed Content Resolved**: Múltiples servidores configurados
- ✅ **CORS Updated**: Política permissiva para desarrollo
- ✅ **OpenAPI Enhanced**: Documentación con múltiples endpoints
- ✅ **Security Groups**: Reglas optimizadas para ALB y ECS
- ✅ **Target Groups**: Health checks HTTP y TCP funcionando
- ✅ **Load Balancers**: ALB público y NLB interno operativos

### **🚀 Próximos Pasos**
- [ ] **HTTPS en ALB Testing**: Certificado SSL para ALB de prueba
- [ ] **API Gateway Integration**: Conectar con el ALB de producción existente
- [ ] **Monitoring Enhancement**: Dashboards personalizados
- [ ] **Backup Strategy**: Políticas de backup para RDS
- [ ] **Security Hardening**: WAF rules para API Gateway

---

## 👥 **CONTACTO y SOPORTE**

**🏢 Equipo**: Pragma - Crediya  
**📧 Contacto**: [Team Contact]  
**📖 Documentación**: Este README  
**🐛 Issues**: GitHub Issues  
**🔄 CI/CD**: GitHub Actions

---

## 🔐 **CREDENCIALES y SECRETOS**

⚠️ **IMPORTANTE**: Todas las credenciales están gestionadas por AWS Secrets Manager

```bash
# Para acceder a secretos
aws secretsmanager get-secret-value --secret-id [SECRET_NAME] --region us-east-2
```

**🔑 Secretos Gestionados**:
- Database connection strings
- JWT signing keys  
- API keys
- External service credentials

---

**🎉 ¡INFRAESTRUCTURA 100% OPERATIVA!** 

✅ **Swagger UI**: [`https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/webjars/swagger-ui/index.html`](https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/webjars/swagger-ui/index.html#/IAM%20API/listUsers)