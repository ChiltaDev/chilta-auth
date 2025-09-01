#  Chilta Auth - Servicio de Autenticación Keycloak

## 📋 Descripción

Este es el servicio de autenticación y autorización de **Chilta**, implementado con **Keycloak** como servidor de identidad centralizado. Proporciona una solución robusta para la gestión de usuarios, autenticación y autorización en toda la plataforma de Chilta.

### ✨ Características Principales

- 🔑 **Autenticación centralizada** con múltiples proveedores
- 👥 **Gestión de usuarios** y roles
-  **Autorización basada en roles** (RBAC)
- 🌐 **Integración OAuth** con Google y Facebook
- 🏢 **Realm personalizado** para Chilta
- 🐳 **Despliegue con Docker** listo para producción
- 📊 **Base de datos PostgreSQL** para persistencia

##  Inicio Rápido

### Prerrequisitos

- Docker y Docker Compose instalados
- Variables de entorno configuradas (ver sección de configuración)

### Despliegue

1. **Clonar el repositorio:**
```bash
git clone <repository-url>
cd chilta-auth
```

2. **Configurar variables de entorno:**
```bash
cp .env.example .env
# Editar .env con tus credenciales
```

3. **Levantar el servicio:**
```bash
docker compose up -d
```

4. **Acceder a Keycloak:**
   - **URL**: http://localhost:8080
   - **Usuario**: `admin`
   - **Contraseña**: `admin`

## ️ Arquitectura

### Stack Tecnológico

- **Keycloak**: v24.0 (servidor de identidad)
- **PostgreSQL**: v15 (base de datos)
- **Docker Compose**: orquestación de contenedores

### Componentes

```
chilta-auth/
├── docker-compose.yml          # Configuración de servicios
├── realms/
│   └── realm-chilta.json       # Configuración del realm
├── .env                        # Variables de entorno
└── README.md                   # Documentación
```

## ⚙️ Configuración

### Variables de Entorno

Crear archivo `.env` con las siguientes variables:

```env
# OAuth Providers
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
```

### Configuración de Docker Compose

El servicio incluye:

- **keycloak**: Servidor principal de autenticación
- **db-keycloak**: Base de datos PostgreSQL
- **Volúmenes**: Persistencia de datos

## 👥 Realm Chilta

### Usuarios Predefinidos

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `AdminUser` | `admin123` | `admin` | Administrador del sistema |
| `NormalUser` | `user123` | `user` | Usuario estándar |

### Roles del Sistema

- **`admin`**: Acceso completo al sistema
- **`user`**: Usuario con permisos básicos

### Clientes Configurados

#### ️ Chilta Web (`chilta-web`)
- **Tipo**: Cliente público
- **Protocolo**: OpenID Connect
- **URLs de redirección**: `http://localhost:3000/*`
- **Flujos habilitados**: Authorization Code Flow con PKCE
- **Scopes**: `profile`, `roles`, `email`

#### 🔌 Chilta API (`chilta-api`)
- **Tipo**: Cliente confidencial
- **Protocolo**: OpenID Connect
- **Flujos habilitados**: Direct Access Grants
- **Service Accounts**: Habilitado
- **Scopes**: `profile`, `roles`, `email`

### Proveedores de Identidad

#### 🔍 Google OAuth
- Configurado para autenticación con Google
- Requiere `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`

#### 📘 Facebook OAuth
- Configurado para autenticación con Facebook
- Requiere `FACEBOOK_CLIENT_ID` y `FACEBOOK_CLIENT_SECRET`

## 🔧 Configuración Avanzada

### Personalización del Realm

El archivo `realms/realm-chilta.json` contiene:

- Configuración de internacionalización (español/inglés)
- Temas personalizados
- Políticas de contraseñas
- Configuración de eventos y auditoría

### Configuración de Base de Datos

```yaml
# PostgreSQL Configuration
KC_DB: postgres
KC_DB_URL: jdbc:postgresql://db-keycloak:5432/keycloak
KC_DB_USERNAME: keycloak
KC_DB_PASSWORD: keycloak
```

## 🛠️ Operaciones

### Comandos Útiles

```bash
# Levantar servicios
docker compose up -d

# Ver logs
docker compose logs -f keycloak

# Detener servicios
docker compose down

# Reiniciar servicios
docker compose restart

# Limpiar volúmenes (¡CUIDADO! Borra datos)
docker compose down -v
```

### Monitoreo

- **Health Check**: http://localhost:8080/health
- **Admin Console**: http://localhost:8080/admin
- **Logs**: `docker compose logs keycloak`

##  Seguridad

### Mejores Prácticas

1. **Cambiar credenciales por defecto** en producción
2. **Configurar HTTPS** para entornos de producción
3. **Revisar logs** regularmente
4. **Actualizar Keycloak** a las últimas versiones
5. **Configurar backup** de la base de datos

### Configuración de Producción

Para entornos de producción:

- Usar variables de entorno seguras
- Configurar certificados SSL/TLS
- Implementar backup automático
- Configurar monitoreo y alertas

## 📚 Integración

### Frontend (React/Next.js)

```javascript
// Ejemplo de configuración para frontend
const keycloakConfig = {
  url: process.env.KEYCLOAK_URL,
  realm: process.env.KEYCLOAK_REALM,
  clientId: 'chilta-web'
};
```

### Backend (Node.js/Express)

```javascript
// Ejemplo de middleware de autenticación
const keycloak = new Keycloak({}, {
  realm: process.env.KEYCLOAK_REALM,
  'auth-server-url': process.env.KEYCLOAK_URL,
  'ssl-required': 'external',
  resource: 'chilta-api',
  'confidential-port': 0
});
```

## 🐛 Troubleshooting

### Problemas Comunes

1. **Error de conexión a base de datos**
   - Verificar que PostgreSQL esté ejecutándose
   - Revisar credenciales en variables de entorno

2. **Error de importación del realm**
   - Verificar que el archivo `realm-chilta.json` esté en la ubicación correcta
   - Revisar permisos del archivo

3. **Problemas con OAuth providers**
   - Verificar que las variables de entorno estén configuradas
   - Revisar URLs de redirección en los proveedores

### Logs y Debugging

```bash
# Ver logs detallados
docker compose logs -f keycloak

# Acceder al contenedor
docker exec -it chilta-back-keycloak /bin/bash
```

## 📞 Soporte

Para soporte técnico o preguntas sobre la configuración:

- **Equipo de Desarrollo**: [email]
- **Documentación**: [link]
- **Issues**: [link al repositorio]

## 📄 Licencia

Este proyecto es propiedad de **Chilta** y está destinado para uso interno de la empresa.

---

**Desarrollado por el equipo de Chilta** 🚀