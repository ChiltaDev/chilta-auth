package com.chilta.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private String password;
    private String username;
    private String url;

    public DatabaseConfig() {
        String dbHost = getConfigValue("BACKEND_DB_HOST", "");
        String dbPort = getConfigValue("BACKEND_DB_PORT", "");
        String dbName = getConfigValue("BACKEND_DB_NAME", "");
        this.username = getConfigValue("BACKEND_DB_USER", "");
        this.password = getConfigValue("BACKEND_DB_PASSWORD", "");
        this.url = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);

        logger.info("DatabaseConfig inicializado con configuración:");
        logger.info("DB Host: {}", dbHost);
        logger.info("DB Port: {}", dbPort);
        logger.info("DB Name: {}", dbName);
        logger.info("DB User: {}", this.username);
    }

    public String getUrl() {
        return this.url;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    private String getConfigValue(String key, String defaultValue) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return defaultValue;
    }
}
