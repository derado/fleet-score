package com.fleetscore.common.exception;

public class DuplicateOrganisationException extends RuntimeException {

    public DuplicateOrganisationException(String name) {
        super("Organisation with name '" + name + "' already exists");
    }
}
