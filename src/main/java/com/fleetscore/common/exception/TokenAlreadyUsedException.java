package com.fleetscore.common.exception;

public class TokenAlreadyUsedException extends RuntimeException {

    public TokenAlreadyUsedException() {
        super("Token already used");
    }

    public TokenAlreadyUsedException(String tokenType) {
        super(tokenType + " token already used");
    }
}
