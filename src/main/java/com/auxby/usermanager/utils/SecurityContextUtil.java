package com.auxby.usermanager.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityContextUtil {

    private SecurityContextUtil(){}

    public static String getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return null;
        }
        return authentication.getName();
    }
}
