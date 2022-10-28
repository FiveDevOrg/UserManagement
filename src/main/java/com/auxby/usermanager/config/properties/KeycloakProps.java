package com.auxby.usermanager.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Data
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProps {
    @NotBlank
    private String url;
    @NotBlank
    private String realm;
    @NotBlank
    private String authUrl;
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
}
