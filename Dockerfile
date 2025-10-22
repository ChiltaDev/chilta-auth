# --- Etapa 1: Compilar el SPI ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos pom y código del SPI
COPY providers/sync-user-storage/pom.xml .
COPY providers/sync-user-storage/src ./src

# Compilamos
RUN mvn clean package -DskipTests

# --- Etapa 2: Imagen de Keycloak ---
FROM quay.io/keycloak/keycloak:24.0

# Copiamos el JAR compilado al directorio de providers
COPY --from=build /app/target/*.jar /opt/keycloak/providers/

# Copiamos el tema personalizado
COPY themes/chilta /opt/keycloak/themes/chilta

# Reconstruimos Keycloak para que cargue el provider y el tema
RUN /opt/keycloak/bin/kc.sh build

# Usamos el mismo entrypoint que antes
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm"]