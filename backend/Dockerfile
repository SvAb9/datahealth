# Imagen base: solo el entorno de ejecución de Java 21 (no hace falta el JDK completo)
FROM eclipse-temurin:21-jre

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# JAR generado antes con: ./mvnw clean package
COPY target/datahealth-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la API
EXPOSE 8080

# Arranque con el perfil "docker" (application-docker.properties)
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
