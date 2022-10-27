package com.auxby.usermanager.exception;

public class UserEmailNotValidated extends RuntimeException {
    public UserEmailNotValidated(String email) {
        super(String.format("Email address %s not validated. Please validate the address.", email));
    }
}
