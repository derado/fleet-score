package com.fleetscore.common.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceType, String field, String value) {
        super(resourceType + " with " + field + " '" + value + "' already exists");
    }
}
