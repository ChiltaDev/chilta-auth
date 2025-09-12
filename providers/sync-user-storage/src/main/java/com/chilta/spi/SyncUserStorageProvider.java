package com.chilta.spi;

import com.chilta.spi.adapters.SyncUserAdapter;
import com.chilta.spi.exceptions.DatabaseErrorException;
import com.chilta.spi.managers.DatabaseConnectionManager;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SyncUserStorageProvider implements UserRegistrationProvider, UserLookupProvider, UserStorageProvider {
    private static final Logger logger = LoggerFactory.getLogger(SyncUserStorageProvider.class);
    private final KeycloakSession session;
    private final DatabaseConnectionManager databaseConnectionManager;

    public SyncUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.databaseConnectionManager = new DatabaseConnectionManager();
        this.databaseConnectionManager.startConnection();
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        Connection connection = databaseConnectionManager.getConnection();
        UserProvider local = getUserProvider();
        UserModel user = local.addUser(realm, username);

        String sql = """
            INSERT INTO users (uuid, \"firstName\", \"lastName\", username, email, \"pictureLink\", \"createdAt\", \"updatedAt\")
            VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
            ON CONFLICT (uuid) DO UPDATE SET
                \"firstName\" = EXCLUDED.\"firstName\",
                \"lastName\" = EXCLUDED.\"lastName\",
                username = EXCLUDED.username,
                email = EXCLUDED.email,
                \"pictureLink\" = EXCLUDED.\"pictureLink\",
                \"updatedAt\"= EXCLUDED.\"updatedAt\"
        """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(user.getId());
            statement.setObject(1, uuid);
            statement.setString(2, user.getFirstName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getUsername());
            statement.setString(5, user.getEmail());
            statement.setString(6, getPictureLink(user));

            int rowsAffected = statement.executeUpdate();
            logger.info("User {} in backend: {} rows affected",
                    rowsAffected > 0 ? "created/updated" : "not modified", rowsAffected);
        } catch (SQLException exception) {
            session.users().removeUser(realm, user);
            throw new DatabaseErrorException("Database error while creating user", exception);
        }
        user.setEnabled(true);
        return user;
    }

    private UserProvider getUserProvider() {
        return session.getProvider(UserProvider.class, "jpa");
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        Connection connection = databaseConnectionManager.getConnection();

        String sql = "UPDATE users SET \"deletedAt\" = NOW(), \"updatedAt\" = NOW() WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            UUID uuid = UUID.fromString(user.getId());
            statement.setObject(1, uuid);
            int rowsAffected = statement.executeUpdate();
            logger.info("User eliminated from backend: {} rows affected", rowsAffected);
        } catch (SQLException e) {
            logger.error("Database error while removing user from backend", e);
        }
        user.setEnabled(false);

        return true;
    }

    @Override
    public void close() {
        this.databaseConnectionManager.closeConnection();
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        UserModel user = getUserProvider().getUserById(realm, id);
        if (user != null) {
            return new SyncUserAdapter(user, this.databaseConnectionManager);
        }
        return null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        UserModel user = getUserProvider().getUserByUsername(realm, username);
        if (user != null) {
            return new SyncUserAdapter(user, this.databaseConnectionManager);
        }
        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        UserModel user = getUserProvider().getUserByEmail(realm, email);
        if (user != null) {
            return new SyncUserAdapter(user, this.databaseConnectionManager);
        }
        return null;
    }

    private String getPictureLink(UserModel user) {
        String pictureLink = user.getFirstAttribute("picture");
        if (pictureLink == null || pictureLink.isEmpty()) {
            return "https://ui-avatars.com/api/?name=" +
                    user.getFirstName() + "+" + user.getLastName() +
                    "&background=random";
        }
        return pictureLink;
    }
}