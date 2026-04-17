package com.fleetscore.common.exception;

public class RegistrationInUseException extends RuntimeException {

    public RegistrationInUseException() {
        super("Registration cannot be deleted because it is referenced by race results");
    }
}
