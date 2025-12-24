package com.fleetscore.common.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String tokenType) {
        super(tokenType + " token expired");
    }
}
