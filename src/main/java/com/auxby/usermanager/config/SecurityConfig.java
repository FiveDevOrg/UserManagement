package com.auxby.usermanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    public static final String AUXBY_USER = "auxby_user";

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/v1/user")
                .authenticated()
                .antMatchers(HttpMethod.PUT, "/api/v1/user/**")
                .authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/v1/user")
                .authenticated()
                .anyRequest()
                .permitAll()
                .and()
                .oauth2ResourceServer()
                .jwt();

        return http.build();
    }
}
