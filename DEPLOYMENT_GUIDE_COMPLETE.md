# 🚀 IAM Service - Guía Completa de Despliegue desde Cero

Esta guía te permitirá recrear **completamente** toda la infraestructura del IAM Service en AWS desde cero, paso a paso, sin omitir ningún detalle.

---

## 📋 **TABLA DE CONTENIDOS**

1. [Prerequisites y Requerimientos](#-prerequisites-y-requerimientos)
2. [Fase 1: Preparación del Entorno](#-fase-1-preparación-del-entorno)
3. [Fase 2: Configuración de Terraform](#-fase-2-configuración-de-terraform)
4. [Fase 3: Despliegue de Infraestructura AWS](#-fase-3-despliegue-de-infraestructura-aws)
5. [Fase 4: Preparación de la Aplicación](#-fase-4-preparación-de-la-aplicación)
6. [Fase 5: Build y Deploy de la Aplicación](#-fase-5-build-y-deploy-de-la-aplicación)
7. [Fase 6: Configuración CI/CD](#-fase-6-configuración-cicd)
8. [Fase 7: Testing y Verificación](#-fase-7-testing-y-verificación)
9. [Troubleshooting](#-troubleshooting)
10. [Arquitectura Final](#-arquitectura-final)

---

## 🛠️ **PREREQUISITES Y REQUERIMIENTOS**

### **📝 Checklist de Herramientas Requeridas**
- [ ] **AWS CLI** instalado y configurado
- [ ] **Terraform** v1.0+ instalado
- [ ] **Docker Desktop** instalado y funcionando
- [ ] **Git** instalado
- [ ] **Visual Studio Code** (opcional pero recomendado)
- [ ] **PowerShell** 5.1+ (Windows) o **Bash** (Linux/Mac)

### **🔐 Configuración AWS Inicial**
- [ ] Cuenta AWS activa
- [ ] Usuario IAM con permisos de administrador
- [ ] AWS CLI configurado con credenciales válidas
- [ ] Región objetivo definida (recomendado: `us-east-2`)

### **💰 Costo Estimado**
- **Costo mensual aproximado**: $159 USD
- **Componentes principales**: ECS Fargate, Aurora MySQL, API Gateway, NAT Gateway

---

## 🚀 **FASE 1: PREPARACIÓN DEL ENTORNO**

### **Paso 1.1: Verificar Instalaciones**

```powershell
# Verificar AWS CLI
aws --version
# Debe mostrar: aws-cli/2.x.x

# Verificar credenciales AWS
aws sts get-caller-identity
# Debe mostrar tu AccountId, UserId, y Arn

# Verificar Terraform
terraform --version
# Debe mostrar: Terraform v1.x.x

# Verificar Docker
docker --version
# Debe mostrar: Docker version xx.x.x

# Verificar región AWS configurada
aws configure get region
# Debe mostrar: us-east-2 (o tu región preferida)
```

**✅ RESULTADO ESPERADO**: Todas las herramientas funcionando correctamente.

### **Paso 1.2: Crear Estructura de Directorios**

```powershell
# Crear directorio principal para Terraform
New-Item -ItemType Directory -Path "C:\Users\Usuario\terraform" -Force

# Navegar al directorio
cd C:\Users\Usuario\terraform

# Verificar ubicación actual
Get-Location
# Debe mostrar: C:\Users\Usuario\terraform
```

**✅ RESULTADO ESPERADO**: Directorio `C:\Users\Usuario\terraform` creado y accesible.

---

## ⚙️ **FASE 2: CONFIGURACIÓN DE TERRAFORM**

### **Paso 2.1: Crear Archivo de Variables**

```powershell
# Asegurarse de estar en el directorio correcto
cd C:\Users\Usuario\terraform

# Crear archivo variables.tf
New-Item -ItemType File -Name "variables.tf"
```

**Contenido de `variables.tf`**:
```hcl
variable "aws_region" {
  type        = string
  description = "AWS region"
  default     = "us-east-2"
}

variable "project_name" {
  type        = string
  description = "Project name (used for naming)"
  default     = "iam-service"
}

variable "environment" {
  type        = string
  description = "Deployment environment"
  default     = "prod"
}

variable "vpc_cidr" {
  type        = string
  description = "VPC CIDR"
  default     = "10.0.0.0/16"
}

variable "az_count" {
  type        = number
  description = "Number of AZs to use"
  default     = 2
}

variable "fargate_cpu" {
  type        = number
  description = "Fargate task CPU (e.g., 256, 512, 1024)"
  default     = 512
}

variable "fargate_memory" {
  type        = number
  description = "Fargate task memory (MiB)"
  default     = 1024
}

variable "desired_count" {
  type        = number
  description = "Desired number of ECS tasks"
  default     = 2
}

variable "min_capacity" {
  type        = number
  description = "Min ECS tasks"
  default     = 2
}

variable "max_capacity" {
  type        = number
  description = "Max ECS tasks"
  default     = 6
}

variable "db_name" {
  type        = string
  description = "Aurora database name"
  default     = "iam_db"
}

variable "db_master_username" {
  type        = string
  description = "Aurora master username"
  default     = "appmaster"
}

variable "container_port" {
  type        = number
  description = "Application container port"
  default     = 8080
}

variable "alarm_email" {
  type        = string
  description = "Email to subscribe to SNS for alarms (optional)"
  default     = ""
}

locals {
  name = "${var.project_name}-${var.environment}"
  tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}
```

### **Paso 2.2: Crear Provider Configuration**

```powershell
# Crear archivo provider.tf
New-Item -ItemType File -Name "provider.tf"
```

**Contenido de `provider.tf`**:
```hcl
terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.1"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.tags
  }
}
```

### **Paso 2.3: Crear Configuración de Red (VPC)**

```powershell
# Crear archivo vpc.tf
New-Item -ItemType File -Name "vpc.tf"
```

**Contenido de `vpc.tf`** (archivo completo):
```hcl
data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = merge(local.tags, { Name = "${local.name}-vpc" })
}

resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id
  tags   = merge(local.tags, { Name = "${local.name}-igw" })
}

# Public subnets (for NAT GW)
resource "aws_subnet" "public" {
  count                   = var.az_count
  vpc_id                  = aws_vpc.this.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true
  tags = merge(local.tags, {
    Name = "${local.name}-public-${count.index}"
    Tier = "public"
  })
}

# Private subnets (for ECS tasks, NLB internal, Aurora)
resource "aws_subnet" "private" {
  count             = var.az_count
  vpc_id            = aws_vpc.this.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + 100)
  availability_zone = data.aws_availability_zones.available.names[count.index]
  tags = merge(local.tags, {
    Name = "${local.name}-private-${count.index}"
    Tier = "private"
  })
}

resource "aws_eip" "nat" {
  domain = "vpc"
  tags = merge(local.tags, { Name = "${local.name}-nat-eip" })
}

resource "aws_nat_gateway" "this" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id
  tags          = merge(local.tags, { Name = "${local.name}-nat" })
  depends_on    = [aws_internet_gateway.this]
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
  tags = merge(local.tags, { Name = "${local.name}-public-rt" })
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.this.id
  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.this.id
  }
  tags = merge(local.tags, { Name = "${local.name}-private-rt" })
}

resource "aws_route_table_association" "private" {
  count          = length(aws_subnet.private)
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

# SG for ECS tasks (allow from VPC only on app port)
resource "aws_security_group" "ecs_service" {
  name        = "${local.name}-ecs-sg"
  description = "ECS service security group"
  vpc_id      = aws_vpc.this.id

  ingress {
    description = "App port from VPC"
    from_port   = var.container_port
    to_port     = var.container_port
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, { Name = "${local.name}-ecs-sg" })
}

# SG for RDS (allow MySQL from ECS SG)
resource "aws_security_group" "rds" {
  name        = "${local.name}-rds-sg"
  description = "RDS security group"
  vpc_id      = aws_vpc.this.id

  ingress {
    description     = "MySQL from ECS"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, { Name = "${local.name}-rds-sg" })
}
```

### **Paso 2.4: Crear Configuración de Secrets**

```powershell
# Crear archivo secrets.tf
New-Item -ItemType File -Name "secrets.tf"
```

**Contenido de `secrets.tf`**:
```hcl
# Random passwords for database and JWT
resource "random_password" "db_master" {
  length  = 20
  special = true
}

resource "random_password" "jwt_secret" {
  length  = 48
  special = true
}

# Secrets Manager for database credentials
resource "aws_secretsmanager_secret" "db" {
  name                    = "${local.name}/db"
  recovery_window_in_days = 30
  tags                    = local.tags
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    username = var.db_master_username
    password = random_password.db_master.result
  })
}

# Secrets Manager for JWT secret
resource "aws_secretsmanager_secret" "jwt" {
  name                    = "${local.name}/jwt"
  recovery_window_in_days = 30
  tags                    = local.tags
}

resource "aws_secretsmanager_secret_version" "jwt" {
  secret_id = aws_secretsmanager_secret.jwt.id
  secret_string = jsonencode({
    jwt_secret = random_password.jwt_secret.result
  })
}
```

### **Paso 2.5: Crear Configuración de Base de Datos**

```powershell
# Crear archivo rds.tf
New-Item -ItemType File -Name "rds.tf"
```

**Contenido de `rds.tf`**:
```hcl
# DB subnet group for Aurora
resource "aws_db_subnet_group" "this" {
  name       = "${local.name}-db-subnets"
  subnet_ids = aws_subnet.private[*].id
  tags       = local.tags
}

# Aurora MySQL cluster
resource "aws_rds_cluster" "this" {
  cluster_identifier      = "${local.name}-aurora"
  engine                 = "aurora-mysql"
  engine_mode            = "provisioned"
  database_name          = var.db_name
  master_username        = var.db_master_username
  master_password        = random_password.db_master.result
  backup_retention_period = 7
  preferred_backup_window = "03:00-04:00"
  skip_final_snapshot    = true
  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.this.name
  storage_encrypted      = true
  deletion_protection    = false

  tags = local.tags
}

# Aurora MySQL instances
resource "aws_rds_cluster_instance" "this" {
  count              = 2
  identifier         = "${local.name}-aurora-${count.index}"
  cluster_identifier = aws_rds_cluster.this.id
  instance_class     = "db.t3.medium"
  engine             = aws_rds_cluster.this.engine
  publicly_accessible = false
  
  tags = local.tags
}
```

### **Paso 2.6: Crear Configuración ECR**

```powershell
# Crear archivo ecr.tf
New-Item -ItemType File -Name "ecr.tf"
```

**Contenido de `ecr.tf`**:
```hcl
resource "aws_ecr_repository" "app" {
  name                 = var.project_name
  image_tag_mutability = "MUTABLE"
  image_scanning_configuration {
    scan_on_push = true
  }
  encryption_configuration {
    encryption_type = "KMS"
  }
  tags = local.tags
}
```

### **Paso 2.7: Crear Configuración IAM**

```powershell
# Crear archivo iam.tf
New-Item -ItemType File -Name "iam.tf"
```

**Contenido de `iam.tf`**:
```hcl
# ECS Task Execution Role
resource "aws_iam_role" "ecs_task_execution" {
  name = "${local.name}-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })

  tags = local.tags
}

# Attach the basic ECS task execution policy
resource "aws_iam_role_policy_attachment" "ecs_task_execution_basic" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
  role       = aws_iam_role.ecs_task_execution.name
}

# Additional policy for ECR access
resource "aws_iam_role_policy_attachment" "ecr_readonly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.ecs_task_execution.name
}

# Policy for Secrets Manager access
resource "aws_iam_policy" "secrets_access" {
  name        = "${local.name}-secrets-access"
  description = "Allow ECS task execution role to read app secrets"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Resource = [
          aws_secretsmanager_secret.db.arn,
          aws_secretsmanager_secret.jwt.arn
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "secrets_access_attach" {
  policy_arn = aws_iam_policy.secrets_access.arn
  role       = aws_iam_role.ecs_task_execution.name
}
```

### **Paso 2.8: Crear Configuración ECS**

```powershell
# Crear archivo ecs.tf
New-Item -ItemType File -Name "ecs.tf"
```

**Contenido de `ecs.tf`**:
```hcl
# ECS Cluster
resource "aws_ecs_cluster" "this" {
  name = local.name
  tags = local.tags
}

# ECS Task Definition
resource "aws_ecs_task_definition" "app" {
  family                   = local.name
  requires_compatibilities = ["FARGATE"]
  network_mode            = "awsvpc"
  cpu                     = var.fargate_cpu
  memory                  = var.fargate_memory
  execution_role_arn      = aws_iam_role.ecs_task_execution.arn

  container_definitions = jsonencode([
    {
      name  = local.name
      image = "${aws_ecr_repository.app.repository_url}:latest"
      
      portMappings = [
        {
          containerPort = var.container_port
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.app.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE"
          value = "docker"
        },
        {
          name  = "SERVER_PORT"
          value = tostring(var.container_port)
        }
      ]

      # Environment variables from secrets
      secrets = [
        {
          name      = "ADAPTERS_R2DBC_HOST"
          valueFrom = aws_rds_cluster.this.endpoint
        },
        {
          name      = "ADAPTERS_R2DBC_PORT"
          valueFrom = "3306"
        },
        {
          name      = "ADAPTERS_R2DBC_DATABASE"
          valueFrom = var.db_name
        },
        {
          name      = "ADAPTERS_R2DBC_USERNAME"
          valueFrom = "${aws_secretsmanager_secret.db.arn}:username::"
        },
        {
          name      = "ADAPTERS_R2DBC_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.db.arn}:password::"
        },
        {
          name      = "SECURITY_JWT_SECRET"
          valueFrom = "${aws_secretsmanager_secret.jwt.arn}:jwt_secret::"
        }
      ]

      essential = true
    }
  ])

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  tags = local.tags
}

# ECS Service
resource "aws_ecs_service" "app" {
  name            = local.name
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_service.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.nlb_tg.arn
    container_name   = local.name
    container_port   = var.container_port
  }

  depends_on = [
    aws_lb_listener.nlb_listener,
    aws_iam_role_policy_attachment.ecs_task_execution_basic
  ]

  tags = local.tags
}
```

### **Paso 2.9: Crear Configuración Load Balancer**

```powershell
# Crear archivo nlb.tf
New-Item -ItemType File -Name "nlb.tf"
```

**Contenido de `nlb.tf`**:
```hcl
# Network Load Balancer (internal)
resource "aws_lb" "nlb" {
  name               = "${local.name}-nlb"
  internal           = true
  load_balancer_type = "network"
  subnets            = aws_subnet.private[*].id

  enable_deletion_protection = false

  tags = local.tags
}

# Target Group for NLB
resource "aws_lb_target_group" "nlb_tg" {
  name        = "${local.name}-tg"
  port        = var.container_port
  protocol    = "TCP"
  target_type = "ip"
  vpc_id      = aws_vpc.this.id

  health_check {
    enabled             = true
    healthy_threshold   = 3
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    path                = "/actuator/health"
    protocol            = "HTTP"
    port                = "traffic-port"
  }

  tags = local.tags
}

# Listener for NLB
resource "aws_lb_listener" "nlb_listener" {
  load_balancer_arn = aws_lb.nlb.arn
  port              = "80"
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.nlb_tg.arn
  }
}
```

### **Paso 2.10: Crear Configuración API Gateway**

```powershell
# Crear archivo apigw.tf
New-Item -ItemType File -Name "apigw.tf"
```

**Contenido de `apigw.tf`**:
```hcl
# API Gateway HTTP API with VPC Link to NLB
resource "aws_apigatewayv2_vpc_link" "this" {
  name               = "${local.name}-vpclink"
  subnet_ids         = aws_subnet.private[*].id
  security_group_ids = [aws_security_group.ecs_service.id]
  tags               = local.tags
}

resource "aws_apigatewayv2_api" "this" {
  name          = local.name
  protocol_type = "HTTP"
  tags          = local.tags
}

resource "aws_apigatewayv2_integration" "nlb" {
  api_id                 = aws_apigatewayv2_api.this.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  connection_type        = "VPC_LINK"
  connection_id          = aws_apigatewayv2_vpc_link.this.id
  integration_uri        = aws_lb_listener.nlb_listener.arn
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "proxy" {
  api_id    = aws_apigatewayv2_api.this.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.nlb.id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.this.id
  name        = "$default"
  auto_deploy = true
  tags        = local.tags
}
```

### **Paso 2.11: Crear Configuración de Logs**

```powershell
# Crear archivo logs.tf
New-Item -ItemType File -Name "logs.tf"
```

**Contenido de `logs.tf`**:
```hcl
# CloudWatch Log Group for ECS
resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name}"
  retention_in_days = 30
  tags              = local.tags
}
```

### **Paso 2.12: Crear Configuración de Auto Scaling**

```powershell
# Crear archivo autoscaling.tf
New-Item -ItemType File -Name "autoscaling.tf"
```

**Contenido de `autoscaling.tf`**:
```hcl
# Application Auto Scaling Target
resource "aws_appautoscaling_target" "ecs" {
  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${aws_ecs_cluster.this.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

# Application Auto Scaling Policy
resource "aws_appautoscaling_policy" "cpu" {
  name               = "${local.name}-cpu-scaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value = 60.0

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    disable_scale_in   = false
    scale_in_cooldown  = 120
    scale_out_cooldown = 120
  }
}
```

### **Paso 2.13: Crear Configuración de Monitoreo**

```powershell
# Crear archivo cloudwatch.tf
New-Item -ItemType File -Name "cloudwatch.tf"
```

**Contenido de `cloudwatch.tf`**:
```hcl
# SNS Topic for alarms
resource "aws_sns_topic" "alarms" {
  name = "${local.name}-alarms"
  tags = local.tags
}

# CloudWatch Alarm for ECS CPU
resource "aws_cloudwatch_metric_alarm" "ecs_cpu_high" {
  alarm_name          = "${local.name}-ECS-CPU-High"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ECS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "ECS service CPU > 80%"
  alarm_actions       = [aws_sns_topic.alarms.arn]

  dimensions = {
    ServiceName = aws_ecs_service.app.name
    ClusterName = aws_ecs_cluster.this.name
  }

  tags = local.tags
}

# CloudWatch Alarm for NLB unhealthy targets
resource "aws_cloudwatch_metric_alarm" "nlb_unhealthy" {
  alarm_name          = "${local.name}-NLB-Unhealthy"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "UnHealthyHostCount"
  namespace           = "AWS/NetworkELB"
  period              = 60
  statistic           = "Average"
  threshold           = 0
  alarm_description   = "Unhealthy targets detected in NLB target group"
  alarm_actions       = [aws_sns_topic.alarms.arn]
  treat_missing_data  = "notBreaching"

  dimensions = {
    TargetGroup  = aws_lb_target_group.nlb_tg.arn_suffix
    LoadBalancer = aws_lb.nlb.arn_suffix
  }

  tags = local.tags
}
```

### **Paso 2.14: Crear Outputs**

```powershell
# Crear archivo outputs.tf
New-Item -ItemType File -Name "outputs.tf"
```

**Contenido de `outputs.tf`**:
```hcl
output "api_gateway_endpoint" {
  description = "API Gateway endpoint URL"
  value       = aws_apigatewayv2_api.this.api_endpoint
}

output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  description = "RDS Aurora cluster endpoint"
  value       = aws_rds_cluster.this.endpoint
}

output "nlb_dns_name" {
  description = "Network Load Balancer DNS name"
  value       = aws_lb.nlb.dns_name
}

output "ecs_service_name" {
  description = "ECS service name"
  value       = aws_ecs_service.app.name
}

output "db_secret_arn" {
  description = "Database credentials secret ARN"
  value       = aws_secretsmanager_secret.db.arn
}

output "jwt_secret_arn" {
  description = "JWT secret ARN"
  value       = aws_secretsmanager_secret.jwt.arn
}
```

**✅ RESULTADO ESPERADO**: 14 archivos Terraform creados en `C:\Users\Usuario\terraform\`.

---

## 🏗️ **FASE 3: DESPLIEGUE DE INFRAESTRUCTURA AWS**

### **Paso 3.1: Inicializar Terraform**

```powershell
# Verificar que estás en el directorio correcto
cd C:\Users\Usuario\terraform
Get-Location

# Inicializar Terraform
terraform init

# Verificar la configuración
terraform validate
```

**✅ RESULTADO ESPERADO**: 
- "Terraform has been successfully initialized!"
- "Success! The configuration is valid."

### **Paso 3.2: Planificar el Despliegue**

```powershell
# Crear plan de ejecución
terraform plan -out=tfplan

# Revisar el plan (debe mostrar ~49 recursos a crear)
```

**✅ RESULTADO ESPERADO**: "Plan: 49 to add, 0 to change, 0 to destroy."

### **Paso 3.3: Aplicar la Infraestructura**

```powershell
# Aplicar el plan (esto tomará ~10-15 minutos)
terraform apply tfplan

# O aplicar directamente con auto-aprobación
terraform apply -auto-approve
```

**⏱️ TIEMPO ESTIMADO**: 10-15 minutos

**✅ RESULTADO ESPERADO**: "Apply complete! Resources: 49 added, 0 changed, 0 destroyed."

### **Paso 3.4: Verificar Outputs**

```powershell
# Ver todos los outputs
terraform output

# Guardar valores importantes
$API_ENDPOINT = terraform output -raw api_gateway_endpoint
$ECR_REPO = terraform output -raw ecr_repository_url
$DB_ENDPOINT = terraform output -raw rds_endpoint

Write-Host "API Gateway: $API_ENDPOINT"
Write-Host "ECR Repository: $ECR_REPO"
Write-Host "Database: $DB_ENDPOINT"
```

**✅ RESULTADO ESPERADO**: URLs y endpoints válidos mostrados.

### **Paso 3.5: Verificar Recursos en AWS**

```powershell
# Verificar ECS Cluster
aws ecs describe-clusters --clusters iam-service-prod

# Verificar RDS Cluster
aws rds describe-db-clusters --db-cluster-identifier iam-service-prod-aurora

# Verificar API Gateway
aws apigatewayv2 get-apis --query "Items[?Name=='iam-service-prod']"
```

**✅ RESULTADO ESPERADO**: Todos los servicios creados y en estado "ACTIVE" o "available".

---

## 📱 **FASE 4: PREPARACIÓN DE LA APLICACIÓN**

### **Paso 4.1: Navegar al Proyecto**

```powershell
# Navegar al directorio de la aplicación
cd "C:\Users\Usuario\Documents\Yanuard\Pragma\IAM-SERVICE"
Get-Location
```

### **Paso 4.2: Obtener Credenciales de Base de Datos**

```powershell
# Obtener credenciales de la base de datos
$DB_SECRET = aws secretsmanager get-secret-value --secret-id $(terraform output -raw db_secret_arn) --query SecretString --output text | ConvertFrom-Json

Write-Host "DB Username: $($DB_SECRET.username)"
Write-Host "DB Password: $($DB_SECRET.password)"
Write-Host "DB Endpoint: $(terraform output -raw rds_endpoint)"
```

### **Paso 4.3: Actualizar Configuración de la Aplicación**

```powershell
# Hacer backup del archivo .env original
Copy-Item ".env" ".env.backup"

# Obtener los valores reales
$DB_ENDPOINT = terraform output -raw rds_endpoint
$DB_CREDS = aws secretsmanager get-secret-value --secret-id $(terraform output -raw db_secret_arn) --query SecretString --output text | ConvertFrom-Json
```

**Editar el archivo `.env`** y actualizar las siguientes líneas:
```bash
# Buscar estas líneas y reemplazarlas:
AURORA_ENDPOINT=iam-service-prod-aurora.cluster-XXXXXXX.us-east-2.rds.amazonaws.com
AURORA_DATABASE=iam_db
AURORA_USERNAME=appmaster
AURORA_PASSWORD=TuPasswordReal
AURORA_PORT=3306
```

**✅ RESULTADO ESPERADO**: Archivo `.env` actualizado con credenciales reales.

---

## 🐳 **FASE 5: BUILD Y DEPLOY DE LA APLICACIÓN**

### **Paso 5.1: Iniciar Docker Desktop**

```powershell
# Verificar que Docker está corriendo
docker --version
docker ps
```

**⚠️ IMPORTANTE**: Si Docker no responde, inicia Docker Desktop manualmente.

### **Paso 5.2: Construir la Imagen Docker**

```powershell
# Asegurarse de estar en el directorio del proyecto
cd "C:\Users\Usuario\Documents\Yanuard\Pragma\IAM-SERVICE"

# Construir la imagen
docker build -t iam-service .
```

**⏱️ TIEMPO ESTIMADO**: 3-5 minutos

**✅ RESULTADO ESPERADO**: "Successfully tagged iam-service:latest"

### **Paso 5.3: Autenticar con ECR**

```powershell
# Obtener token y hacer login
$ECR_TOKEN = aws ecr get-login-password --region us-east-2
echo $ECR_TOKEN | docker login --username AWS --password-stdin $(terraform output -raw ecr_repository_url | Split-Path -Parent)
```

**✅ RESULTADO ESPERADO**: "Login Succeeded"

### **Paso 5.4: Tagear y Subir la Imagen**

```powershell
# Obtener URL del repositorio ECR
$ECR_REPO = terraform output -raw ecr_repository_url

# Tagear la imagen
docker tag iam-service:latest $ECR_REPO`:latest

# Subir la imagen
docker push $ECR_REPO`:latest
```

**⏱️ TIEMPO ESTIMADO**: 2-5 minutos (depende de tu conexión)

**✅ RESULTADO ESPERADO**: "latest: digest: sha256:xxxx size: 856"

### **Paso 5.5: Forzar Deploy en ECS**

```powershell
# Forzar nuevo deployment
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment

# Esperar a que el servicio esté estable
Write-Host "Esperando deployment... (puede tomar 2-3 minutos)"
Start-Sleep -Seconds 60

# Verificar estado
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod --query "services[0].{Name:serviceName,Status:status,DesiredCount:desiredCount,RunningCount:runningCount,PendingCount:pendingCount}"
```

**✅ RESULTADO ESPERADO**: RunningCount = DesiredCount (generalmente 2)

---

## 🔄 **FASE 6: CONFIGURACIÓN CI/CD**

### **Paso 6.1: Crear Estructura GitHub Actions**

```powershell
# Crear directorio para GitHub Actions
New-Item -ItemType Directory -Path ".github\workflows" -Force
```

### **Paso 6.2: Crear Workflow de Deploy**

```powershell
# Crear archivo de workflow
New-Item -ItemType File -Path ".github\workflows\deploy.yml"
```

**Contenido de `.github\workflows\deploy.yml`**:
```yaml
name: Deploy IAM Service to AWS

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

env:
  AWS_REGION: us-east-2
  ECR_REPOSITORY: iam-service
  ECS_CLUSTER: iam-service-prod
  ECS_SERVICE: iam-service-prod

jobs:
  test:
    name: Run Tests
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-
          
    - name: Make gradlew executable
      run: chmod +x ./gradlew
      
    - name: Run tests
      run: ./gradlew test
      
    - name: Generate test report
      run: ./gradlew jacocoTestReport
      if: always()

  build-and-deploy:
    name: Build and Deploy
    needs: test
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
        
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v2
      
    - name: Get commit hash
      id: get-commit-hash
      run: echo "commit-hash=$(git rev-parse --short HEAD)" >> $GITHUB_OUTPUT
      
    - name: Build, tag, and push image to Amazon ECR
      id: build-image
      env:
        ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
        IMAGE_TAG: ${{ steps.get-commit-hash.outputs.commit-hash }}
      run: |
        # Update Aurora endpoint in .env file
        sed -i 's/AURORA_ENDPOINT=.*/AURORA_ENDPOINT=${{ secrets.AURORA_ENDPOINT }}/' .env
        sed -i 's/AURORA_DATABASE=.*/AURORA_DATABASE=${{ secrets.AURORA_DATABASE }}/' .env
        sed -i 's/AURORA_USERNAME=.*/AURORA_USERNAME=${{ secrets.AURORA_USERNAME }}/' .env
        sed -i 's/AURORA_PASSWORD=.*/AURORA_PASSWORD=${{ secrets.AURORA_PASSWORD }}/' .env
        
        # Build Docker image
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:latest .
        
        # Push to ECR
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
        
        echo "image=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" >> $GITHUB_OUTPUT
        
    - name: Force new deployment
      run: |
        aws ecs update-service --cluster ${{ env.ECS_CLUSTER }} --service ${{ env.ECS_SERVICE }} --force-new-deployment
        
    - name: Wait for deployment to complete
      run: |
        aws ecs wait services-stable --cluster ${{ env.ECS_CLUSTER }} --services ${{ env.ECS_SERVICE }}
        
    - name: Get service status
      run: |
        aws ecs describe-services --cluster ${{ env.ECS_CLUSTER }} --services ${{ env.ECS_SERVICE }} --query "services[0].{ServiceName:serviceName,Status:status,RunningCount:runningCount,DesiredCount:desiredCount}"
        
  notification:
    name: Send Notification
    needs: [test, build-and-deploy]
    runs-on: ubuntu-latest
    if: always()
    
    steps:
    - name: Notify deployment status
      run: |
        if [[ "${{ needs.build-and-deploy.result }}" == "success" ]]; then
          echo "✅ Deployment successful!"
          echo "🚀 Your IAM Service is now live!"
        else
          echo "❌ Deployment failed!"
        fi
```

**✅ RESULTADO ESPERADO**: Archivo de workflow creado en `.github\workflows\deploy.yml`

---

## 🧪 **FASE 7: TESTING Y VERIFICACIÓN**

### **Paso 7.1: Verificar Health Check**

```powershell
# Obtener endpoint del API Gateway
$API_ENDPOINT = terraform output -raw api_gateway_endpoint

# Probar health check
$response = Invoke-WebRequest -Uri "$API_ENDPOINT/actuator/health" -UseBasicParsing
Write-Host "Status: $($response.StatusCode)"
Write-Host "Response: $($response.Content)"
```

**✅ RESULTADO ESPERADO**: Status: 200, Response: {"status":"UP","groups":["liveness","readiness"]}

### **Paso 7.2: Verificar Swagger UI**

```powershell
# Probar Swagger UI
$swaggerResponse = Invoke-WebRequest -Uri "$API_ENDPOINT/swagger-ui" -UseBasicParsing
Write-Host "Swagger Status: $($swaggerResponse.StatusCode)"
```

**✅ RESULTADO ESPERADO**: Swagger Status: 200

### **Paso 7.3: Verificar Estado de Servicios ECS**

```powershell
# Estado del servicio ECS
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod --query "services[0].{Name:serviceName,Status:status,DesiredCount:desiredCount,RunningCount:runningCount,PendingCount:pendingCount}"

# Estado de los targets del Load Balancer
aws elbv2 describe-target-health --target-group-arn $(aws elbv2 describe-target-groups --names iam-service-prod-tg --query "TargetGroups[0].TargetGroupArn" --output text) --query "TargetHealthDescriptions[].TargetHealth.State"
```

**✅ RESULTADO ESPERADO**: 
- ECS: RunningCount = DesiredCount (2)
- Load Balancer: ["healthy","healthy"]

### **Paso 7.4: Inicializar Base de Datos (IMPORTANTE)**

```powershell
# Crear script SQL temporal
@"
USE iam_db;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO roles (id, name, description) VALUES 
(1, 'ADMIN', 'Administrator role with full access'),
(2, 'USER', 'Standard user role'),
(3, 'MANAGER', 'Manager role with elevated permissions');

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

SHOW TABLES;
SELECT * FROM roles;
"@ | Out-File -FilePath "init_db.sql" -Encoding UTF8

Write-Host "Archivo init_db.sql creado. Ejecutar manualmente en la base de datos:"
Write-Host "Host: $(terraform output -raw rds_endpoint)"
Write-Host "Port: 3306"
Write-Host "Database: iam_db"
Write-Host "Username: appmaster"
Write-Host "Password: [usar secretsmanager]"
```

### **Paso 7.5: Probar API de Crear Usuario**

```powershell
# Solo después de inicializar la BD
$API_ENDPOINT = terraform output -raw api_gateway_endpoint
$headers = @{
    'Content-Type' = 'application/json'
    'Authorization' = 'Basic ' + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes('admin:admin123'))
}

$body = @{
    firstName = 'Juan'
    lastName = 'Pérez'
    email = 'juan.perez@test.com'
    birthdate = '1990-05-15'
    identityDocument = '12345678'
    phoneNumber = '+57 300 123 4567'
    baseSalary = 2500000.00
    address = 'Calle 123 #45-67, Bogotá'
    roleId = 1
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "$API_ENDPOINT/api/v1/usuarios" -Method POST -Headers $headers -Body $body -UseBasicParsing
    Write-Host "✅ Usuario creado exitosamente!"
    Write-Host $response.Content
} catch {
    Write-Host "❌ Error al crear usuario: $($_.Exception.Message)"
}
```

**✅ RESULTADO ESPERADO**: Usuario creado con respuesta JSON exitosa.

---

## 🚨 **TROUBLESHOOTING**

### **Problema 1: Docker no responde**
```powershell
# Solución:
# 1. Abrir Docker Desktop manualmente
# 2. Esperar a que esté completamente iniciado
# 3. Verificar: docker ps
```

### **Problema 2: Error 401 en API**
```powershell
# Causa: Base de datos no inicializada
# Solución: Ejecutar init_db.sql en la base de datos
```

### **Problema 3: ECS tasks no inician**
```powershell
# Verificar logs
aws logs tail /ecs/iam-service-prod --follow

# Forzar nuevo deployment
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment
```

### **Problema 4: Terraform errores de permisos**
```powershell
# Verificar credenciales
aws sts get-caller-identity

# Verificar permisos IAM del usuario
```

---

## 🏗️ **ARQUITECTURA FINAL**

### **📊 Componentes Desplegados**

```
Internet
    ↓
API Gateway (Public Endpoint)
    ↓
VPC Link
    ↓
Network Load Balancer (Internal)
    ↓
ECS Fargate Service (2 tasks)
    ↓
Aurora MySQL Cluster (2 instances)
```

### **🔧 Recursos AWS Creados**

| Tipo | Cantidad | Nombres |
|------|----------|---------|
| **VPC** | 1 | iam-service-prod-vpc |
| **Subredes** | 4 | 2 públicas, 2 privadas |
| **Security Groups** | 2 | ECS, RDS |
| **ECS Cluster** | 1 | iam-service-prod |
| **ECS Service** | 1 | iam-service-prod |
| **RDS Cluster** | 1 | iam-service-prod-aurora |
| **RDS Instances** | 2 | db.t3.medium |
| **API Gateway** | 1 | HTTP API |
| **Load Balancer** | 1 | Network LB |
| **ECR Repository** | 1 | iam-service |
| **Secrets** | 2 | DB credentials, JWT |
| **CloudWatch** | Multiple | Logs, alarms |

### **🌐 URLs Finales**

- **API Base**: `https://[api-id].execute-api.us-east-2.amazonaws.com`
- **Swagger UI**: `https://[api-id].execute-api.us-east-2.amazonaws.com/swagger-ui`
- **Health Check**: `https://[api-id].execute-api.us-east-2.amazonaws.com/actuator/health`

### **💰 Costos Mensuales Estimados**

| Servicio | Costo Estimado |
|----------|----------------|
| ECS Fargate | ~$30 |
| Aurora MySQL | ~$70 |
| API Gateway | ~$5 |
| Load Balancer | ~$20 |
| NAT Gateway | ~$32 |
| CloudWatch | ~$2 |
| **TOTAL** | **~$159/mes** |

---

## ✅ **CHECKLIST FINAL DE VERIFICACIÓN**

### **Infraestructura**
- [ ] 49 recursos Terraform creados exitosamente
- [ ] VPC con subredes públicas y privadas
- [ ] Security Groups configurados
- [ ] NAT Gateway funcionando
- [ ] Aurora MySQL cluster disponible
- [ ] ECS Cluster activo

### **Aplicación**
- [ ] ECR repository creado
- [ ] Imagen Docker construida y subida
- [ ] ECS Service con 2 tareas corriendo
- [ ] Load Balancer con targets healthy
- [ ] API Gateway respondiendo

### **Endpoints**
- [ ] Health check: Status 200
- [ ] Swagger UI: Accesible
- [ ] API create user: Funcionando (después de init DB)

### **CI/CD**
- [ ] GitHub Actions workflow creado
- [ ] Secrets configurados (pendiente en GitHub)
- [ ] Pipeline probado (pendiente)

### **Base de Datos**
- [ ] Tablas creadas (roles, users)
- [ ] Datos iniciales insertados
- [ ] Conexión desde aplicación verificada

---

## 🎉 **¡DEPLOYMENT COMPLETO EXITOSO!**

Si has seguido todos los pasos y completado el checklist, tu **IAM Service** estará:

✅ **Desplegado en AWS** con arquitectura de producción  
✅ **Altamente disponible** con múltiples AZ  
✅ **Escalable** con Auto Scaling configurado  
✅ **Monitoreado** con CloudWatch y alarmas  
✅ **Seguro** con VPC privadas y Security Groups  
✅ **Listo para CI/CD** con GitHub Actions  

### **🚀 Próximos Pasos Opcionales**

1. **Configurar GitHub Secrets** para automatizar deployments
2. **Configurar dominio personalizado** (si tienes uno)
3. **Agregar más endpoints** a la API
4. **Configurar notificaciones** por email/Slack
5. **Implementar backups** automatizados

**¡Felicidades! Has creado una infraestructura empresarial completa en AWS.** 🎊