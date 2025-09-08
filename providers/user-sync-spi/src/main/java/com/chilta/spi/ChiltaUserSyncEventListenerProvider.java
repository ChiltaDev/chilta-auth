package com.chilta.spi;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class ChiltaUserSyncEventListenerProvider implements EventListenerProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ChiltaUserSyncEventListenerProvider.class);
    
    private final KeycloakSession session;
    private final DatabaseConfig databaseConfig;
    
    public ChiltaUserSyncEventListenerProvider(KeycloakSession session, DatabaseConfig databaseConfig) {
        this.session = session;
        this.databaseConfig = databaseConfig;
    }
    
    @Override
    public void onEvent(Event event) {
        // Manejar eventos de usuario (login, logout, etc.)
        if (event.getType() == EventType.REGISTER) {
            handleUserRegistration(event);
        }
    }
    
    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // Manejar eventos administrativos (crear, actualizar, eliminar usuarios)
        if (event.getResourceType() == ResourceType.USER) {
            switch (event.getOperationType()) {
                case CREATE:
                    handleUserCreate(event);
                    break;
                case UPDATE:
                    handleUserUpdate(event);
                    break;
                case DELETE:
                    handleUserDelete(event);
                    break;
                default:
                    break;
            }
        }
    }
    
    private void handleUserRegistration(Event event) {
        try {
            String userId = event.getUserId();
            if (userId != null) {
                UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
                if (user != null) {
                    syncUserToBackend(user, "CREATE");
                    logger.info("Usuario sincronizado después del registro: {}", user.getUsername());
                }
            }
        } catch (Exception e) {
            logger.error("Error al sincronizar usuario después del registro", e);
        }
    }
    
    private void handleUserCreate(AdminEvent event) {
        try {
            String userId = event.getResourcePath().substring(event.getResourcePath().lastIndexOf('/') + 1);
            UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
            if (user != null) {
                syncUserToBackend(user, "CREATE");
                logger.info("Usuario creado sincronizado: {}", user.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error al sincronizar usuario creado", e);
        }
    }
    
    private void handleUserUpdate(AdminEvent event) {
        try {
            String userId = event.getResourcePath().substring(event.getResourcePath().lastIndexOf('/') + 1);
            UserModel user = session.users().getUserById(session.getContext().getRealm(), userId);
            if (user != null) {
                syncUserToBackend(user, "UPDATE");
                logger.info("Usuario actualizado sincronizado: {}", user.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error al sincronizar usuario actualizado", e);
        }
    }
    
    private void handleUserDelete(AdminEvent event) {
        try {
            String userId = event.getResourcePath().substring(event.getResourcePath().lastIndexOf('/') + 1);
            deleteUserFromBackend(userId);
            logger.info("Usuario eliminado sincronizado: {}", userId);
        } catch (Exception e) {
            logger.error("Error al sincronizar eliminación de usuario", e);
        }
    }
    
    private void syncUserToBackend(UserModel user, String operation) {
        try (Connection connection = getDatabaseConnection()) {
            if ("CREATE".equals(operation)) {
                createUserInBackend(connection, user);
            } else if ("UPDATE".equals(operation)) {
                updateUserInBackend(connection, user);
            }
        } catch (SQLException e) {
            logger.error("Error de conexión a la base de datos del backend", e);
        }
    }
    
    private void createUserInBackend(Connection connection, UserModel user) throws SQLException {
        String sql = "INSERT INTO users (uuid, name, email, \"pictureLink\") VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT (email) DO UPDATE SET " +
                    "name = EXCLUDED.name, " +
                    "\"pictureLink\" = EXCLUDED.\"pictureLink\"";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getId());
            statement.setString(2, user.getFirstName() + " " + user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, getPictureLink(user));
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Usuario {} en backend: {} filas afectadas", 
                       rowsAffected > 0 ? "creado/actualizado" : "no modificado", rowsAffected);
        }
    }
    
    private void updateUserInBackend(Connection connection, UserModel user) throws SQLException {
        String sql = "UPDATE users SET name = ?, \"pictureLink\" = ? WHERE email = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFirstName() + " " + user.getLastName());
            statement.setString(2, getPictureLink(user));
            statement.setString(3, user.getEmail());
            
            int rowsAffected = statement.executeUpdate();
            logger.info("Usuario actualizado en backend: {} filas afectadas", rowsAffected);
        }
    }
    
    private void deleteUserFromBackend(String userId) {
        try (Connection connection = getDatabaseConnection()) {
            String sql = "DELETE FROM users WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                
                int rowsAffected = statement.executeUpdate();
                logger.info("Usuario eliminado del backend: {} filas afectadas", rowsAffected);
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar usuario del backend", e);
        }
    }
    
    private String getPictureLink(UserModel user) {
        // Obtener la imagen del perfil del usuario
        String pictureLink = user.getFirstAttribute("picture");
        if (pictureLink == null || pictureLink.isEmpty()) {
            // Si no hay imagen, usar una por defecto o el avatar de email
            return "https://ui-avatars.com/api/?name=" + 
                   user.getFirstName() + "+" + user.getLastName() + 
                   "&background=random";
        }
        return pictureLink;
    }
    
    private Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
            databaseConfig.getUrl(),
            databaseConfig.getUsername(),
            databaseConfig.getPassword()
        );
    }
    
    @Override
    public void close() {
        // Limpiar recursos si es necesario
    }
}
