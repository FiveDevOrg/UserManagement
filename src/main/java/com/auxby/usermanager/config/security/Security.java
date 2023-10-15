package com.auxby.usermanager.config.security;

import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.config.MonitoringFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.auxby.usermanager.utils.constant.AppConstant.BASE_V1_URL;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Security {
    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    private static final String[] POST_API_WHITELIST = {
            "/api/v1/user",
            "/api/v1/user/reset",
            "/api/v1/user/login",
            "/api/v1/user/googleAuth",
            "/api/v1/user/resend-verification-link"
    };

    private static final String[] GET_API_WHITELIST = {
            "/api/v1/user/email/check"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(SWAGGER_WHITELIST).permitAll()
                .antMatchers(HttpMethod.GET, GET_API_WHITELIST).permitAll()
                .antMatchers(HttpMethod.POST, POST_API_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<MonitoringFilter> userActivityFilter() {
        FilterRegistrationBean<MonitoringFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new MonitoringFilter(userService));
        registrationBean.addUrlPatterns("/" + BASE_V1_URL + "/*");
        registrationBean.setOrder(100);

        return registrationBean;
    }
}
