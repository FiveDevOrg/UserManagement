package com.auxby.usermanager.exception;

public class UserEmailNotValidatedException extends RuntimeException {
    public UserEmailNotValidatedException(String email) {
        super(String.format("Email address %s not validated. Please validate the address.", email));
    }
}
