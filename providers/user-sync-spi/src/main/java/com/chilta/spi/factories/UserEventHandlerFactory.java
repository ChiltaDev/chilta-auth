package com.chilta.spi.factories;

import com.chilta.spi.DatabaseConfig;
import com.chilta.spi.handlers.*;
import com.chilta.spi.handlers.abstracts.EventHandler;
import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;

public class UserEventHandlerFactory {
    public static EventHandler Create(Event event, KeycloakSession session, DatabaseConfig databaseConfig) {
        switch (event.getType()) {
            case REGISTER:
                return new UserRegisterHandler(session, databaseConfig);
            default:
                return null;
        }
    }
}
