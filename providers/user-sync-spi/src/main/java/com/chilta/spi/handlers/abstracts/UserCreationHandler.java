package com.chilta.spi.handlers.abstracts;

import com.chilta.spi.DatabaseConfig;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public abstract class UserCreationHandler extends EventHandler {
    Logger logger;
    public UserCreationHandler(Logger logger, KeycloakSession session, DatabaseConfig databaseConfig) {
        super(session, databaseConfig);
        this.logger = logger;
    }

    protected void createUserInBackend(Connection connection, UserModel user) throws SQLException {
        String sql = """
            INSERT INTO users (uuid, name, email, \"pictureLink\", \"createdAt\", \"updatedAt\") VALUES (?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (uuid) DO UPDATE SET
                name = EXCLUDED.name,
                \"pictureLink\" = EXCLUDED.\"pictureLink\"
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(user.getId());
            statement.setObject(1, uuid);
            statement.setString(2, user.getFirstName() + " " + user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, getPictureLink(user));

            int rowsAffected = statement.executeUpdate();
            logger.info("User {} in backend: {} rows affected",
                    rowsAffected > 0 ? "created/updated" : "not modified", rowsAffected);
        }
    }
}
