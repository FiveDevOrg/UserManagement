package com.auxby.usermanager.utils.enums;

public enum CustomHttpStatus {
    ACTION_NOT_ALLOW(481),
    BAD_CREDENTIALS(451),
    USER_EMAIL_NOT_VALIDATED(470);

    CustomHttpStatus(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }
}
