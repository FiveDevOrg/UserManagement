package com.auxby.usermanager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    private static final String[] POST_API_WHITELIST = {
            "/api/v1/user",
            "/api/v1/user/reset",
            "/api/v1/user/login",
            "/api/v1/user/resend-verification-link"
    };

    private static final String[] GET_API_WHITELIST = {
            "/api/v1/user/email/check"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().disable()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(SWAGGER_WHITELIST).permitAll()
                .antMatchers(HttpMethod.GET, GET_API_WHITELIST).permitAll()
                .antMatchers(HttpMethod.POST, POST_API_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer().jwt();
        http.addFilterBefore(new CustomFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    //TODO: debug only
    public class CustomFilter extends GenericFilterBean {

        @Override
        public void doFilter(
                ServletRequest request,
                ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            log.info("-------- SECURITY LOGS ------");
            while (headerNames.hasMoreElements()) {
                log.info("Header: " + httpRequest.getHeader(headerNames.nextElement()));
            }
            log.info("-------- SECURITY END LOGS ------");

            chain.doFilter(request, response);
        }
    }
}
