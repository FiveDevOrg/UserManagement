package com.auxby.usermanager.utils;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtil {

    private SecurityContextUtil(){}

    public static String getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
