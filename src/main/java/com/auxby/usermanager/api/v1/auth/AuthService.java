package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.exception.SignInException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;

    public AuthResponse login(AuthInfo authInfo) {
        try {
            KeycloakAuthResponse response = webClient.post()
                    .uri("https://keycloak-auxby.herokuapp.com/auth/realms/Auxby/protocol/openid-connect/token")
                    .body(BodyInserters.fromFormData("username", authInfo.email())
                            .with("client_secret", "SzGZEKRShNjmMaBnDRivOstRlw027BIX")
                            .with("client_id", "user-management")
                            .with("password", authInfo.password())
                            .with("grant_type", "password"))
                    .retrieve()
                    .bodyToFlux(KeycloakAuthResponse.class)
                    .blockFirst();
            return new AuthResponse(response.access_token());
        } catch (WebClientResponseException exception) {
            throw new SignInException("Login user " + authInfo.email() + " failed.");
        }
    }
}
