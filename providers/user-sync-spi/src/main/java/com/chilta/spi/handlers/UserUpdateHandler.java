package com.chilta.spi.handlers;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.handlers.abstracts.EventHandler;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserUpdateHandler extends EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserUpdateHandler.class);

    public UserUpdateHandler(KeycloakSession session, DatabaseConfig databaseConfig) {
        super(session, databaseConfig);
    }

    @Override
    public void handle(AdminEvent event) {
        try {
            String userId = getUserIdFromPath(event);
            UserModel user = getUserModel(userId);
            if (user != null) {
                syncUserToBackend(user);
                logger.info("Usuario actualizado sincronizado: {}", user.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error al sincronizar usuario actualizado", e);
        }
    }

    private void syncUserToBackend(UserModel user) {
        try (Connection connection = getDatabaseConnection()) {
            updateUserInBackend(connection, user);
        } catch (SQLException e) {
            logger.error("Error de conexión a la base de datos del backend", e);
        }
    }

    private void updateUserInBackend(Connection connection, UserModel user) throws SQLException {
        String sql = "UPDATE users SET name = ?, \"pictureLink\" = ?, updatedAt = NOW(), email = ? WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFirstName() + " " + user.getLastName());
            statement.setString(2, getPictureLink(user));
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getId());

            int rowsAffected = statement.executeUpdate();
            logger.info("Updated user in backend: {} rows affected", rowsAffected);
        }
    }
}
