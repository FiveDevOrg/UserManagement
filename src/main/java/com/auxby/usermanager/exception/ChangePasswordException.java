package com.auxby.usermanager.exception;

public class ChangePasswordException extends RuntimeException {
    public ChangePasswordException(String user) {
        super(String.format("Change password for user %s failed. Wrong password provided.", user));
    }
}
