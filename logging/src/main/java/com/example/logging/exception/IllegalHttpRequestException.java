package com.example.logging.exception;

public class IllegalHttpRequestException extends RuntimeException {

    public IllegalHttpRequestException() {
        super();
    }

    public IllegalHttpRequestException(String message) {
        super(message);
    }

    public IllegalHttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalHttpRequestException(Throwable cause) {
        super(cause);
    }

    protected IllegalHttpRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
