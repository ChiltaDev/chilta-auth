package com.chilta.spi.handlers;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.handlers.abstracts.UserCreationHandler;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserCreateHandler extends UserCreationHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateHandler.class);

    public UserCreateHandler(KeycloakSession session, DatabaseConfig databaseConfig) {
        super(logger, session, databaseConfig);
    }

    @Override
    public void handle(AdminEvent event) {
        try {
            String userId = getUserIdFromPath(event);
            UserModel user = getUserModel(userId);
            if (user != null) {
                createUserInBackend(user);
                logger.info("Synced user created: {}", user.getUsername());
            }
        } catch (Exception e) {
            logger.error("Error while syncing created user", e);
        }
    }

    private void createUserInBackend(UserModel user) {
        try (Connection connection = getDatabaseConnection()) {
            createUserInBackend(connection, user);
        } catch (SQLException e) {
            logger.error("Db connection error while syncing created user", e);
        }
    }
}
