package com.chilta.spi.handlers;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.exceptions.DatabaseErrorException;
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
        String userId = getUserIdFromPath(event);
        UserModel user = getUserModel(userId);
        if (user != null) {
            logger.info("Updating user with id {}", user.getId());
            syncUserToBackend(user);
            logger.info("Updated user synced: {}", user.getUsername());
        }
    }

    private void syncUserToBackend(UserModel user) {
        try (Connection connection = getDatabaseConnection()) {
            updateUserInBackend(connection, user);
        } catch (SQLException e) {
            logger.error("Db connection error while syncing user after updating", e);
            throw new DatabaseErrorException("Db connection error while syncing user after registery: " + e.getMessage(), e);
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
