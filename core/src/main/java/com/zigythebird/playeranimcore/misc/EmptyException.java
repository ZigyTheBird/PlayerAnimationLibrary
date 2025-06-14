package com.zigythebird.playeranimcore.misc;

public class EmptyException extends RuntimeException {
    public EmptyException(String message) {
        super(message, null);
    }

    public EmptyException(String message, Throwable cause) {
        super(message, cause, false, false);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
