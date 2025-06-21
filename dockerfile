# Imagen base con Java 17
FROM eclipse-temurin:17-jdk

# Crea un directorio dentro del contenedor
WORKDIR /app

# Copia el .jar compilado desde tu máquina local al contenedor
COPY target/*.jar app.jar

# Expone el puerto 8089 (usado por Spring Boot)
EXPOSE 8089

# Comando que se ejecuta al iniciar el contenedor
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
