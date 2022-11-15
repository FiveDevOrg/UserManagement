package com.auxby.usermanager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests(
                        auth -> auth.antMatchers("/api/v1/user/**").permitAll()
                                .antMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        http.addFilterBefore(new CustomFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public static class CustomFilter extends GenericFilter {
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement();
                String value = httpRequest.getHeader(key);
                log.info("Header - " + key + " value:" + value);
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
