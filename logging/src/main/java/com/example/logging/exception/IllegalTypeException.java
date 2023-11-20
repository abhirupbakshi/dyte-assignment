package com.example.logging.exception;

public class IllegalTypeException extends RuntimeException {

    public IllegalTypeException() {
        super();
    }

    public IllegalTypeException(String message) {
        super(message);
    }

    public IllegalTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTypeException(Throwable cause) {
        super(cause);
    }

    protected IllegalTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
