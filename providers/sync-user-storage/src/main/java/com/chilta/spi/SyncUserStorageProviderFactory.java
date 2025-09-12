package com.chilta.spi;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class SyncUserStorageProviderFactory implements UserStorageProviderFactory<SyncUserStorageProvider> {

    @Override
    public SyncUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new SyncUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return "sync-user-storage";
    }
}