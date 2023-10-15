package com.auxby.usermanager.config;

import com.amazonaws.util.StringUtils;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.utils.SecurityContextUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
@AllArgsConstructor
public class MonitoringFilter implements Filter {

    private final UserService userService;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws ServletException, IOException {
        String uuid = SecurityContextUtil.getUsername();
        if (StringUtils.hasValue(uuid)) {
            userService.updateUserLastSeen(uuid);
        }
        chain.doFilter(request, response);
    }
}
