package com.chilta.spi.handlers;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.handlers.abstracts.EventHandler;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDeleteHandler extends EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserDeleteHandler.class);

    public UserDeleteHandler(KeycloakSession session, DatabaseConfig databaseConfig) {
        super(session, databaseConfig);
    }

    @Override
    public void handle(AdminEvent event) {
        try {
            String userId = getUserIdFromPath(event);
            deleteUserFromBackend(userId);
            logger.info("Eliminated user synced: {}", userId);
        } catch (Exception e) {
            logger.error("Db connection error while syncing eliminated user", e);
        }
    }

    private void deleteUserFromBackend(String userId) {
        try (Connection connection = getDatabaseConnection()) {
            String sql = "UPDATE users SET deletedAt = NOW(), updatedAt = NOW() WHERE uuid = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                int rowsAffected = statement.executeUpdate();
                logger.info("User eliminated from backend: {} rows affected", rowsAffected);
            }
        } catch (SQLException e) {
            logger.error("Erro while trying to eliminate user from database", e);
        }
    }
}
