package com.auxby.usermanager.config;

import com.auxby.usermanager.config.properties.KeycloakProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    private final KeycloakProps keycloakProps;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(keycloakProps.getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }
}
