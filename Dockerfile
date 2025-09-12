# --- Etapa 1: Compilar el SPI ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos pom y código del SPI
COPY providers/user-sync-spi/pom.xml .
COPY providers/user-sync-spi/src ./src

# Compilamos
RUN mvn clean package -DskipTests

# --- Etapa 2: Imagen de Keycloak ---
FROM quay.io/keycloak/keycloak:24.0

# Copiamos el JAR compilado al directorio de providers
COPY --from=build /app/target/*.jar /opt/keycloak/providers/

# Reconstruimos Keycloak para que cargue el provider
RUN /opt/keycloak/bin/kc.sh build

# Usamos el mismo entrypoint que antes
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]