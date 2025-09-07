@echo off
echo =============================================================================
echo  🐳 Sistema IAM Crediya - Inicio Rapido con Docker
echo =============================================================================
echo.

echo Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Docker no está instalado o no está en el PATH
    echo Por favor instala Docker Desktop desde: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

echo ✅ Docker encontrado

echo.
echo Verificando Docker Compose...
docker compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Docker Compose no está disponible
    echo Por favor actualiza Docker Desktop a la versión más reciente
    pause
    exit /b 1
)

echo ✅ Docker Compose encontrado

echo.
echo Verificando archivo .env...
if not exist .env (
    echo ⚠️  Archivo .env no encontrado, copiando desde .env.example
    copy .env.example .env
    echo ✅ Archivo .env creado
    echo.
    echo ⚠️  IMPORTANTE: Revisa y ajusta las variables en .env antes de continuar
    echo Presiona cualquier tecla para continuar o Ctrl+C para salir...
    pause
) else (
    echo ✅ Archivo .env encontrado
)

echo.
echo =============================================================================
echo  🚀 Iniciando servicios Docker...
echo =============================================================================
echo.

echo Construyendo y levantando todos los servicios...
docker compose up --build -d

if errorlevel 1 (
    echo.
    echo ❌ Error al iniciar los servicios
    echo Verificando logs...
    docker compose logs --tail=20
    pause
    exit /b 1
)

echo.
echo =============================================================================
echo  ✅ Sistema iniciado correctamente!
echo =============================================================================
echo.
echo 📌 Servicios disponibles:
echo   • API REST:     http://localhost:8080
echo   • Health Check: http://localhost:8080/actuator/health
echo   • Swagger UI:   http://localhost:8080/swagger-ui
echo   • Adminer:      http://localhost:8081
echo.
echo 🗄️ Base de Datos:
echo   • Host: localhost:3307
echo   • Database: crediya_autenticacion
echo   • User: autenticacion
echo   • Password: (ver archivo .env)
echo.
echo 🔍 Comandos útiles:
echo   • Ver logs:        docker compose logs -f
echo   • Parar servicios: docker compose down
echo   • Estado:          docker compose ps
echo.

echo Verificando estado de los servicios...
timeout /t 5 /nobreak > nul
docker compose ps

echo.
echo 🎉 ¡Sistema listo para usar!
echo.
pause
