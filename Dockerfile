# ---------- build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copiamos primero wrapper y archivos de build para cachear dependencias
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Asegurar permisos de ejecución
RUN chmod +x gradlew

# Descarga de dependencias (cache-friendly)
RUN ./gradlew --no-daemon dependencies || true

# Ahora copiamos el resto del código
COPY . .

# Generar fat-jar => build/libs/app.jar
# (Asegurate en build.gradle.kts de tener tasks.shadowJar { archiveFileName.set("app.jar") })
RUN ./gradlew --no-daemon clean shadowJar

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# User no-root
RUN addgroup -S app && adduser -S app -G app
USER app

# Copiamos el jar
COPY --from=build /workspace/build/libs/app.jar /app/app.jar

# Puerto para Railway (lo expone el contenedor; Railway inyecta PORT)
ENV PORT=8080
EXPOSE 8080

# Flags de memoria para contenedores (ajustan heap automáticamente)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Arranque: forzamos host/port por system properties (Ktor los respeta)
CMD ["sh", "-c", "java -Dfile.encoding=UTF-8 -Dio.ktor.development=false -Dktor.deployment.host=0.0.0.0 -Dktor.deployment.port=${PORT} -jar /app/app.jar"]
