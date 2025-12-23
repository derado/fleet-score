package com.fleetscore.common.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException() {
        super("Email already in use");
    }

    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }
}
