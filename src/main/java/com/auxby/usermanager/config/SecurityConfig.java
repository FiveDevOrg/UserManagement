package com.auxby.usermanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.PUT, "/api/v1/user/**")
                .hasAuthority("auxby_user")
                .antMatchers(HttpMethod.DELETE, "/api/v1/user")
                .hasAuthority("auxby_user")
                .anyRequest()
                .permitAll()
                .and()
                .oauth2ResourceServer()
                .jwt();

        return http.build();
    }
}
