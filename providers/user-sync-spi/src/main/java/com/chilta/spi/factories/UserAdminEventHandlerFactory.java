package com.chilta.spi.factories;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.handlers.abstracts.EventHandler;
import com.chilta.spi.handlers.UserCreateHandler;
import com.chilta.spi.handlers.UserDeleteHandler;
import com.chilta.spi.handlers.UserUpdateHandler;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;

public class UserAdminEventHandlerFactory {
    public static EventHandler Create(AdminEvent event, KeycloakSession session, DatabaseConfig databaseConfig) {
        if (event.getResourceType() == ResourceType.USER) {
            return switch (event.getOperationType()) {
                case CREATE -> new UserCreateHandler(session, databaseConfig);
                case UPDATE -> new UserUpdateHandler(session, databaseConfig);
                case DELETE -> new UserDeleteHandler(session, databaseConfig);
                default -> null;
            };
        }
        return null;
    }
}
