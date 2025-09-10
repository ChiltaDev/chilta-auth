package com.chilta.spi.handlers.abstracts;

import com.chilta.spi.DatabaseConfig;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class EventHandler {
    protected final KeycloakSession session;
    protected final DatabaseConfig databaseConfig;

    public EventHandler(KeycloakSession session, DatabaseConfig databaseConfig) {
        this.session = session;
        this.databaseConfig = databaseConfig;
    }

    public abstract void handle(AdminEvent event);
    public void handle(Event event) {
    }

    protected Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                databaseConfig.getUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }

    protected UserModel getUserModel(String userId) {
        return session.users().getUserById(session.getContext().getRealm(), userId);
    }

    protected static String getUserIdFromPath(AdminEvent event) {
        return event.getResourcePath().substring(event.getResourcePath().lastIndexOf('/') + 1);
    }

    protected String getPictureLink(UserModel user) {
        String pictureLink = user.getFirstAttribute("picture");
        if (pictureLink == null || pictureLink.isEmpty()) {
            return "https://ui-avatars.com/api/?name=" +
                    user.getFirstName() + "+" + user.getLastName() +
                    "&background=random";
        }
        return pictureLink;
    }
}
