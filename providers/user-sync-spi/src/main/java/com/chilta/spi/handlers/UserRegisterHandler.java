package com.chilta.spi.handlers;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.exceptions.DatabaseErrorException;
import com.chilta.spi.handlers.abstracts.UserCreationHandler;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRegisterHandler extends UserCreationHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisterHandler.class);

    public UserRegisterHandler(KeycloakSession session, DatabaseConfig databaseConfig) {
        super(logger, session, databaseConfig);
    }

    @Override
    public void handle(AdminEvent event) {}

    @Override
    public void handle(Event event) {
        String userId = event.getUserId();
        if (userId != null) {
            UserModel user = getUserModel(userId);
            if (user != null) {
                logger.info("Registering user with id {}", user.getId());
                syncUserToBackend(user);
                logger.info("User synced after registry: {}", user.getUsername());
            }
        }
    }

    private void syncUserToBackend(UserModel user) {
        try (Connection connection = getDatabaseConnection()) {
            createUserInBackend(connection, user);
        } catch (SQLException e) {
            logger.error("Db connection error while syncing user after registery", e);
            throw new DatabaseErrorException("Db connection error while syncing user after registery: " + e.getMessage(), e);
        }
    }
}
