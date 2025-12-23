package com.fleetscore.common.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException() {
        super("Token expired");
    }

    public TokenExpiredException(String tokenType) {
        super(tokenType + " token expired");
    }
}
