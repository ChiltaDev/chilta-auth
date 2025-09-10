package com.chilta.spi;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSyncEventListenerProviderFactory implements EventListenerProviderFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSyncEventListenerProviderFactory.class);
    
    private DatabaseConfig databaseConfig;
    
    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new UserSyncEventListenerProvider(session, databaseConfig);
    }
    
    @Override
    public void init(Config.Scope config) {        
        this.databaseConfig = new DatabaseConfig();
    }
    
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        logger.info("UserSyncEventListenerProviderFactory post-inicializado");
    }
    
    @Override
    public void close() {
        logger.info("UserSyncEventListenerProviderFactory cerrado");
    }
    
    @Override
    public String getId() {
        return "chilta-user-sync";
    }
}
