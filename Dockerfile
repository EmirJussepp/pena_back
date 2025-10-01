# ---------- build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copiamos primero wrapper y archivos de build para cachear dependencias
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Permisos + normalizar EOL (CRLF -> LF)
RUN chmod +x gradlew && \
    apk add --no-cache dos2unix && \
    dos2unix gradlew

# Descarga de dependencias (cache-friendly)
RUN ./gradlew --no-daemon dependencies || true

# Ahora copiamos el resto del código
COPY . .

# Generar fat-jar => build/libs/app.jar
RUN ./gradlew --no-daemon clean shadowJar

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /workspace/build/libs/app.jar /app/app.jar

ENV PORT=8080
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

CMD ["sh","-c","java -Dfile.encoding=UTF-8 -Dio.ktor.development=false -Dktor.deployment.host=0.0.0.0 -Dktor.deployment.port=${PORT} -jar /app/app.jar"]
