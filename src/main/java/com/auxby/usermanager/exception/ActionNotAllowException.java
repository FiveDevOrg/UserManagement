package com.auxby.usermanager.exception;

public class ActionNotAllowException extends RuntimeException {
    public ActionNotAllowException(String message) {
        super(message);
    }
}
