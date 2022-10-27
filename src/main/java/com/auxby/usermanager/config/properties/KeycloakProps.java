package com.auxby.usermanager.config.properties;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Data
@Component
public class KeycloakProps {
    @NotBlank
    private String realm = "Auxby";
    @NotBlank
    private String clientId = "user-management";
    @NotBlank
    private String serverUrl = "https://keycloak-auxby.herokuapp.com/auth";
    @NotBlank
    private String clientSecret = "SzGZEKRShNjmMaBnDRivOstRlw027BIX";
}
