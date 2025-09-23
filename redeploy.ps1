# === SCRIPT DE REPUBLICACIÓN RÁPIDA ===
Write-Host "🚀 Iniciando republicación de imagen..." -ForegroundColor Green

# Paso 1: Verificar directorio
cd "C:\Users\Usuario\Documents\Yanuard\Pragma\IAM-SERVICE"
Write-Host "📂 Directorio: $(Get-Location)" -ForegroundColor Cyan

# Paso 2: Verificar Docker
try {
    docker --version | Out-Null
    Write-Host "✅ Docker funcionando" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker no está corriendo - Inicia Docker Desktop" -ForegroundColor Red
    exit 1
}

# Paso 3: Construir imagen
Write-Host "🔨 Construyendo imagen..." -ForegroundColor Yellow
docker build -t iam-service .
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Imagen construida exitosamente" -ForegroundColor Green
} else {
    Write-Host "❌ Error construyendo imagen" -ForegroundColor Red
    exit 1
}

# Paso 4: Login ECR
Write-Host "🔐 Autenticando con ECR..." -ForegroundColor Yellow
try {
    $ECR_TOKEN = aws ecr get-login-password --region us-east-2
    echo $ECR_TOKEN | docker login --username AWS --password-stdin 889522049804.dkr.ecr.us-east-2.amazonaws.com
    Write-Host "✅ Login ECR exitoso" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en login ECR" -ForegroundColor Red
    exit 1
}

# Paso 5: Tagear imagen
Write-Host "🏷️ Tageando imagen..." -ForegroundColor Yellow
docker tag iam-service:latest 889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service:latest

# Paso 6: Push imagen
Write-Host "📤 Subiendo imagen a ECR..." -ForegroundColor Yellow
docker push 889522049804.dkr.ecr.us-east-2.amazonaws.com/iam-service:latest
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Imagen subida exitosamente" -ForegroundColor Green
} else {
    Write-Host "❌ Error subiendo imagen" -ForegroundColor Red
    exit 1
}

# Paso 7: Forzar deployment
Write-Host "🔄 Forzando nuevo deployment en ECS..." -ForegroundColor Yellow
aws ecs update-service --cluster iam-service-prod --service iam-service-prod --force-new-deployment | Out-Null

# Paso 8: Verificar deployment
Write-Host "⏱️ Esperando deployment (45 segundos)..." -ForegroundColor Cyan
Start-Sleep -Seconds 45

Write-Host "📊 Estado del servicio:" -ForegroundColor Cyan
aws ecs describe-services --cluster iam-service-prod --services iam-service-prod --query "services[0].{Name:serviceName,Status:status,DesiredCount:desiredCount,RunningCount:runningCount,PendingCount:pendingCount}"

# Paso 9: Verificar que funcione
Write-Host "🧪 Probando health check..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API funcionando correctamente!" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️ API aún no responde, puede tomar unos minutos más..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 ¡Deployment completado!" -ForegroundColor Green
Write-Host "🌐 API Base: https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com" -ForegroundColor Magenta
Write-Host "📚 Swagger UI: https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/swagger-ui" -ForegroundColor Magenta
Write-Host "💚 Health Check: https://uno0s0vk0f.execute-api.us-east-2.amazonaws.com/actuator/health" -ForegroundColor Magenta
Write-Host ""
Write-Host "⚡ Para futuros deployments, solo ejecuta: .\redeploy.ps1" -ForegroundColor Cyan