package com.example.logging.exception;

public class MappingUnsuccessfulException extends RuntimeException {

    public MappingUnsuccessfulException() {
        super();
    }

    public MappingUnsuccessfulException(String message) {
        super(message);
    }

    public MappingUnsuccessfulException(String message, Throwable cause) {
        super(message, cause);
    }

    public MappingUnsuccessfulException(Throwable cause) {
        super(cause);
    }

    protected MappingUnsuccessfulException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
