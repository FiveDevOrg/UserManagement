package com.auxby.usermanager.exception;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String errorMessage) {
        super(errorMessage);
    }
}
