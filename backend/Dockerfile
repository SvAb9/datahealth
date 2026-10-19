# ---- Etapa 1: compilar con Maven (ya no hay que compilar el JAR a mano) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Etapa 2: imagen final, solo con el entorno de ejecución de Java 21 ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 --no-create-home app
COPY --from=build /build/target/datahealth-0.0.1-SNAPSHOT.jar app.jar
COPY entrypoint.sh entrypoint.sh
# Quita los saltos de línea de Windows (CRLF) que rompen el script en Linux
RUN sed -i 's/\r$//' entrypoint.sh && chmod +x entrypoint.sh
USER app

# Perfil por defecto para correr en tu computador; en Azure se cambia con SPRING_PROFILES_ACTIVE=azure
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["/app/entrypoint.sh"]
