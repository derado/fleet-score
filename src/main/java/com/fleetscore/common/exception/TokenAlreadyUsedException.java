package com.fleetscore.common.exception;

public class TokenAlreadyUsedException extends RuntimeException {

    public TokenAlreadyUsedException(String tokenType) {
        super(tokenType + " token already used");
    }
}
