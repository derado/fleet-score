package com.fleetscore.common.exception;

public class DuplicateSailNumberException extends RuntimeException {

    public DuplicateSailNumberException(Integer sailNumber) {
        super("Sail number " + sailNumber + " is already registered in this class for this regatta");
    }
}
