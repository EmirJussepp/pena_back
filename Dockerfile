FROM openjdk:21-jdk-slim


# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR desde el directorio de construcción de tu máquina local al contenedor
COPY build/libs/ktor-socios-0.0.1.jar /app/ktor-socios-all.jar

# Expone el puerto que utilizará tu aplicación
EXPOSE 8081

# Comando para ejecutar la aplicación Ktor en el contenedor
CMD ["java", "-jar", "/app/ktor-socios-all.jar"]
