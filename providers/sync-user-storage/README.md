# Chilta User Sync SPI

Este es un Service Provider Interface (SPI) de Keycloak que sincroniza automáticamente los usuarios entre Keycloak y la base de datos del backend de metaprop.

## Funcionalidad

El SPI escucha los siguientes eventos de Keycloak:
- **Registro de usuarios** (`REGISTER`)
- **Creación de usuarios** (AdminEvent `CREATE`)
- **Actualización de usuarios** (AdminEvent `UPDATE`)
- **Eliminación de usuarios** (AdminEvent `DELETE`)

Cuando ocurre cualquiera de estos eventos, el SPI automáticamente:
1. Se conecta a la base de datos del backend
2. Sincroniza la información del usuario
3. Mantiene la consistencia entre ambos sistemas

## Mapeo de Campos

| Keycloak | Backend (metaprop) |
|----------|-------------------|
| `user.getId()` | `users.uuid` |
| `user.getFirstName() + " " + user.getLastName()` | `users.name` |
| `user.getEmail()` | `users.email` |
| `user.getFirstAttribute("picture")` | `users.pictureLink` |

## Configuración

### Variables de Entorno

El SPI utiliza las siguientes variables de entorno para conectarse a la base de datos del backend:

**Variables específicas del backend (prioridad alta):**
- `BACKEND_DB_HOST`: Host de la base de datos (por defecto: `localhost`)
- `BACKEND_DB_PORT`: Puerto de la base de datos (por defecto: `5432`)
- `BACKEND_DB_NAME`: Nombre de la base de datos (por defecto: `database`)
- `BACKEND_DB_USER`: Usuario de la base de datos (por defecto: `user`)
- `BACKEND_DB_PASSWORD`: Contraseña de la base de datos (por defecto: `pass`)

**Variables genéricas (prioridad media):**
- `DB_HOST`: Host de la base de datos
- `DB_PORT`: Puerto de la base de datos
- `DB_NAME`: Nombre de la base de datos
- `DB_USER`: Usuario de la base de datos
- `DB_PASSWORD`: Contraseña de la base de datos

**Orden de prioridad:**
1. Variables específicas del backend (`BACKEND_DB_*`)
2. Variables genéricas (`DB_*`)
3. Configuración de Keycloak
4. Valores por defecto

### Docker Compose

El docker-compose.yml ya está configurado para:
1. Montar el JAR del SPI en Keycloak
2. Configurar las variables de entorno necesarias
3. Conectarse a la base de datos del backend externa

## Construcción Automática

El SPI se construye automáticamente cuando ejecutas `docker compose up -d`. No necesitas construir manualmente el SPI.

### Proceso Automatizado

1. **Docker Compose** ejecuta automáticamente el servicio `build-spi`
2. Se construye el SPI usando Maven en un contenedor Docker
3. Keycloak espera a que el SPI esté construido antes de iniciar
4. Se levantan todos los servicios con el SPI ya construido

**Orden de ejecución:**
1. `build-spi` - Construye el SPI
2. `db-keycloak` - Inicia la base de datos
3. `keycloak` - Inicia Keycloak (depende de `build-spi` y `db-keycloak`)

## Instalación

1. El SPI se construye automáticamente al ejecutar `docker compose up -d`
2. El JAR se genera en `target/sync-user-storage-1.0.0.jar`
3. El docker-compose.yml monta automáticamente el JAR en Keycloak

## Uso

1. **Ejecutar** `docker compose up -d` desde el directorio raíz `chilta-auth`
2. El SPI se construye automáticamente y se activa cuando Keycloak inicie
3. No se requieren pasos adicionales

## Logs

Los logs del SPI aparecerán en los logs de Keycloak. Busca mensajes que contengan:
- `UserSyncEventListenerProvider`
- `Usuario sincronizado`
- `Error al sincronizar`

## Estructura del Proyecto

```
providers/sync-user-storage/
├── src/main/java/com/chilta/spi/
│   ├── UserSyncEventListenerProvider.java      # Implementación del listener
│   ├── UserSyncEventListenerProviderFactory.java # Factory del provider
│   └── DatabaseConfig.java                           # Configuración de BD
├── src/main/resources/META-INF/services/
│   └── org.keycloak.events.EventListenerProviderFactory # Registro del SPI
├── pom.xml                                           # Configuración de Maven
├── build.sh                                          # Script de construcción (Linux/Mac)
├── build.bat                                         # Script de construcción (Windows)
└── README.md                                         # Este archivo
```

## Solución de Problemas

### El SPI no se carga
- Verificar que el JAR esté en la ruta correcta
- Revisar los logs de Keycloak para errores de carga
- Asegurarse de que las dependencias estén correctas

### Error de conexión a la base de datos
- Verificar que la base de datos del backend esté ejecutándose
- Comprobar las variables de entorno de conexión
- Revisar la conectividad de red entre contenedores

### Usuarios no se sincronizan
- Verificar que el SPI esté habilitado en Keycloak
- Revisar los logs para errores específicos
- Comprobar que la tabla `users` exista en el backend
