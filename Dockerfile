# --- Etapa 1: Compilar el SPI ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos pom y código del SPI
COPY providers/sync-user-storage/pom.xml .
COPY providers/sync-user-storage/src ./src

# Compilamos
RUN mvn clean package -DskipTests

# --- Etapa 2: Imagen de Keycloak para producción ---
FROM quay.io/keycloak/keycloak:24.0

# Copiamos el JAR compilado al directorio de providers
COPY --from=build /app/target/*.jar /opt/keycloak/providers/

# Copiamos el tema personalizado
COPY themes/chilta /opt/keycloak/themes/chilta

# Copiamos configuración de realm
COPY keycloak/realm-chilta-prod.json /opt/keycloak/data/import/realm-chilta.json

# Cambiar ownership de archivos
RUN chown -R keycloak:keycloak /opt/keycloak

# Cambiar a usuario no-root
USER keycloak

# Reconstruimos Keycloak para que cargue el provider y el tema
RUN /opt/keycloak/bin/kc.sh build

# Exponer puerto
EXPOSE 8080

# Variables de entorno para producción
ENV KC_HOSTNAME_STRICT=false
ENV KC_HOSTNAME_STRICT_HTTPS=false
ENV KC_HTTP_ENABLED=true

# Comando de inicio para producción
ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start", "--import-realm"]