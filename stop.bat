@echo off
echo =============================================================================
echo  🛑 Sistema IAM Crediya - Detener Servicios Docker
echo =============================================================================
echo.

echo Deteniendo todos los servicios...
docker compose down

if errorlevel 1 (
    echo ❌ Error al detener los servicios
    pause
    exit /b 1
)

echo.
echo ✅ Servicios detenidos correctamente

echo.
echo ¿Deseas eliminar también los volúmenes de datos (base de datos)?
echo ⚠️  ADVERTENCIA: Esto eliminará todos los datos almacenados!
echo.
set /p confirm="Escribe 'SI' para eliminar datos o presiona Enter para mantenerlos: "

if /i "%confirm%"=="SI" (
    echo.
    echo Eliminando volúmenes de datos...
    docker compose down -v
    echo ✅ Volúmenes eliminados
) else (
    echo ✅ Datos preservados
)

echo.
echo 📊 Estado actual:
docker compose ps

echo.
echo 🧹 ¿Deseas limpiar imágenes Docker no utilizadas?
set /p cleanup="Escribe 'SI' para limpiar o presiona Enter para omitir: "

if /i "%cleanup%"=="SI" (
    echo.
    echo Limpiando imágenes no utilizadas...
    docker image prune -f
    echo ✅ Limpieza completada
)

echo.
echo 🏁 Sistema detenido
echo.
pause
