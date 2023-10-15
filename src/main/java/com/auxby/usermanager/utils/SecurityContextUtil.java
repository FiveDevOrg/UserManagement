package com.auxby.usermanager.utils;

import com.auxby.usermanager.api.v1.user.repository.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityContextUtil {

    private SecurityContextUtil(){}

    public static String getUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return null;
        }
        User principal = (User) authentication.getPrincipal();

        return principal.getUsername();
    }
}
