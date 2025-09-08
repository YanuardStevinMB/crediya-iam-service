# =============================================================================
# Dockerfile multi-stage para app Spring Boot (JDK 21) con Gradle
# FASE 1 (build): compila y empaqueta el JAR del módulo app-service
# FASE 2 (runtime): ejecuta el JAR en imagen mínima (Temurin JRE 21 Alpine)
# =============================================================================

# =============================== FASE 1: BUILD ================================
# Imagen base con Gradle 8.10.1 + JDK 21 (ideal para compilar)
FROM gradle:8.10.1-jdk21 AS build

# Directorio de trabajo dentro de la imagen de build
WORKDIR /home/gradle/project

# --- Optimización de caché ---
# Copiamos primero SOLO archivos de configuración de Gradle para que la capa
# de dependencias quede cacheada y no se invalide al cambiar código fuente.
COPY gradle/ gradle/
COPY build.gradle main.gradle gradle.properties settings.gradle gradlew gradlew.bat ./
COPY lombok.config ./

# Descarga/resolve dependencias sin daemon para generar una capa cacheable
# (acelera builds posteriores mientras no cambien los archivos anteriores)
RUN gradle --no-daemon dependencies

# Copiamos el resto del proyecto (código fuente, recursos, etc.)
COPY . .

# Construimos el artefacto del módulo específico "app-service"
# --no-build-cache: fuerza no usar cache interno de Gradle (opcional)
# TIP: agrega "-x test" si no quieres ejecutar tests en esta etapa
RUN gradle --no-daemon :app-service:clean :app-service:bootJar --no-build-cache

# ============================== FASE 2: RUNTIME ===============================
# Imagen ligera de ejecución con JRE 21 (Alpine)
FROM eclipse-temurin:21-jre-alpine

# Directorio donde vivirá y se ejecutará la app
WORKDIR /app

# Utilidades mínimas:
# - curl: para el healthcheck
# - tzdata: para configurar zona horaria si se requiere
# Creamos usuario/grupo no-root "spring" (buena práctica de seguridad)
RUN apk add --no-cache curl tzdata && \
    addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# Ejecutar como usuario no-root
USER spring:spring

# Copiamos el JAR construido desde la fase anterior al contenedor de runtime
# --chown: mantiene la propiedad del archivo para el usuario no-root
# IMPORTANTE: ajusta la ruta si tu módulo/estructura difiere
COPY --from=build --chown=spring:spring /home/gradle/project/applications/app-service/build/libs/*.jar /app/app.jar

# ======================= Variables de entorno por defecto =====================
# Zona horaria (cámbiala a America/Bogota si quieres hora local)
ENV TZ=UTC

# Perfil activo de Spring (requiere application-docker.yml/properties)
ENV SPRING_PROFILES_ACTIVE=docker

# Flags recomendados para JVM en contenedores:
# - Usa límites de memoria del contenedor
# - Ajusta porcentaje de RAM máximo para la JVM
# - Entropía rápida para mejorar tiempos de arranque
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Puerto expuesto (debe coincidir con el que usa tu aplicación)
EXPOSE 8080

# ================================ Healthcheck =================================
# Verifica el estado llamando a /actuator/health.
# Asegúrate de tener Actuator habilitado y expuesto para "health".
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# ================================ Entrypoint ==================================
# Ejecuta el JAR con las opciones definidas en JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
