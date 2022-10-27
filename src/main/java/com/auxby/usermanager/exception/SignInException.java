package com.auxby.usermanager.exception;

public class SignInException extends RuntimeException {
    public SignInException(String errorMessage) {
        super(errorMessage);
    }
}
