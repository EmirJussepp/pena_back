# ---------- build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copiamos TODO el repo de una (incluye gradlew)
COPY . .

# Normalizamos EOL y permisos *después* de copiar todo
RUN apk add --no-cache dos2unix && \
    dos2unix gradlew && \
    chmod +x gradlew

# (Opcional) precalentar dependencias para cache
RUN ./gradlew --no-daemon --stacktrace --info dependencies || true

# Generar fat-jar => build/libs/app.jar
RUN ./gradlew --no-daemon --stacktrace --info clean shadowJar -x test

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
