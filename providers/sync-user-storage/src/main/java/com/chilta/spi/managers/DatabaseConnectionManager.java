package com.chilta.spi.managers;

import com.chilta.spi.exceptions.DatabaseErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private final String password;
    private final String username;
    private final String url;
    private Connection connection;

    public DatabaseConnectionManager() {
        String dbHost = getConfigValue("BACKEND_DB_HOST");
        String dbPort = getConfigValue("BACKEND_DB_PORT");
        String dbName = getConfigValue("BACKEND_DB_NAME");
        this.username = getConfigValue("BACKEND_DB_USER");
        this.password = getConfigValue("BACKEND_DB_PASSWORD");
        this.url = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);

        logger.info("DatabaseConnectionManager initialized");
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

    private String getConfigValue(String key) {
        String envValue = System.getenv(key.toUpperCase().replace(".", "_"));
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return "";
    }

    public void startConnection() {
        try {
            this.connection = DriverManager.getConnection(this.getUrl(),
                    this.getUsername(),
                    this.getPassword());
        } catch (SQLException e) {
            throw new DatabaseErrorException("Error while connecting to database.", e);
        }
    }

    public void closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            logger.error("Error while closing connection.", e);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
