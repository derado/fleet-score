package com.fleetscore.common.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String tokenType) {
        super("Invalid " + tokenType + " token");
    }
}
