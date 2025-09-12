package com.chilta.spi.exceptions;

public class DatabaseErrorException extends RuntimeException {
    public DatabaseErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
