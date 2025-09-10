package com.chilta.spi;

import com.chilta.spi.factories.UserAdminEventHandlerFactory;
import com.chilta.spi.factories.UserEventHandlerFactory;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class UserSyncEventListenerProvider implements EventListenerProvider {
    private final KeycloakSession session;
    private final DatabaseConfig databaseConfig;
    
    public UserSyncEventListenerProvider(KeycloakSession session, DatabaseConfig databaseConfig) {
        this.session = session;
        this.databaseConfig = databaseConfig;
    }
    
    @Override
    public void onEvent(Event event) {
        var eventHandler = UserEventHandlerFactory.Create(event, session, databaseConfig);
        if (eventHandler != null) {
            eventHandler.handle(event);
        }
    }
    
    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        var eventHandler = UserAdminEventHandlerFactory.Create(event, session, databaseConfig);
        if (eventHandler != null) {
            eventHandler.handle(event);
        }
    }
    
    @Override
    public void close() {
    }
}
