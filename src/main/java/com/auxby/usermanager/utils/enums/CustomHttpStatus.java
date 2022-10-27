package com.auxby.usermanager.utils.enums;

public enum CustomHttpStatus {
    USER_EMAIL_NOT_VALIDATED(450);

    CustomHttpStatus(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }
}
