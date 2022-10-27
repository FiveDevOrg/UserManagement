package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.config.properties.KeycloakProps;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidated;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CLIENT_ID = "client_id";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_SECRET = "client_secret";
    private final WebClient webClient;
    private final UserService userService;
    private final KeycloakProps keycloakProps;
    private final KeycloakClient keycloakClient;

    public AuthResponse login(AuthInfo authInfo) {
        verifyUserValidateEmailAddress(authInfo.email());
        try {
            KeycloakAuthResponse response = webClient.post()
                    .uri("https://keycloak-auxby.herokuapp.com/auth/realms/Auxby/protocol/openid-connect/token")
                    .body(BodyInserters.fromFormData(USERNAME, authInfo.email())
                            .with(CLIENT_SECRET, keycloakProps.getClientSecret())
                            .with(CLIENT_ID, keycloakProps.getClientId())
                            .with(PASSWORD, authInfo.password())
                            .with(GRANT_TYPE, PASSWORD))
                    .retrieve()
                    .bodyToFlux(KeycloakAuthResponse.class)
                    .blockFirst();
            return new AuthResponse(response.access_token());
        } catch (WebClientResponseException exception) {
            throw new SignInException("Login user " + authInfo.email() + " failed.");
        }
    }

    public void resetPassword(String email) {
        userService.sendResetPasswordLink(email);
    }

    private void verifyUserValidateEmailAddress(String email) {
        UserDetails user = userService.findUser(email);
        UserRepresentation userRepresentation = keycloakClient.getKeycloakRealmUsersResources()
                .get(user.getAccountUuid())
                .toRepresentation();
        if (Boolean.FALSE.equals(userRepresentation.isEmailVerified())) {
            throw new UserEmailNotValidated(email);
        }
    }

    public void resendVerificationLink(String email) {
        UserDetails user = userService.findUser(email);
        userService.sendVerificationPasswordLink(user.getAccountUuid());
    }
}
