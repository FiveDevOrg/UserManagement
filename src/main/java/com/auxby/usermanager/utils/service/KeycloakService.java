package com.auxby.usermanager.utils.service;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.config.properties.KeycloakProps;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.keycloak.OAuth2Constants.*;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private final WebClient webClient;
    private final KeycloakProps keycloakProps;
    private final KeycloakClient keycloakClient;


    public KeycloakAuthResponse performLogin(AuthInfo authInfo) {
        return webClient.post()
                .uri(keycloakProps.getAuthUrl())
                .body(BodyInserters.fromFormData(USERNAME, authInfo.email())
                        .with(CLIENT_SECRET, keycloakProps.getClientSecret())
                        .with(CLIENT_ID, keycloakProps.getClientId())
                        .with(PASSWORD, authInfo.password())
                        .with(GRANT_TYPE, PASSWORD))
                .retrieve()
                .bodyToFlux(KeycloakAuthResponse.class)
                .blockFirst();
    }

    public Optional<UserRepresentation> getKeycloakUser(String userName) {
        return keycloakClient.getKeycloakRealmUsersResources()
                .search(userName, true)
                .stream()
                .findFirst();
    }

    public void deleteKeycloakUser(String userId) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(userId)
                .remove();
    }

    public void performUserUpdate(String userUuid, UserRepresentation userRepresentation) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(userUuid)
                .update(userRepresentation);
    }

    public void sendResetPasswordLink(String accountUuid) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(accountUuid)
                .executeActionsEmail(List.of(UPDATE_PASSWORD));
    }

    public void sendVerificationEmailLink(String accountUuid) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(accountUuid)
                .sendVerifyEmail();
    }

    public Response performCreateUser(UserRepresentation userRepresentation) {
        return keycloakClient.getKeycloakRealmUsersResources()
                .create(userRepresentation);
    }

    public void addUserRole(String userUuid) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(userUuid)
                .roles()
                .realmLevel()
                .add(Collections.singletonList(keycloakClient.getRealmRoleRepresentation("auxby_user")));
    }

    public UserRepresentation getUserRepresentation(String accountUuid) {
        return keycloakClient.getKeycloakRealmUsersResources()
                .get(accountUuid)
                .toRepresentation();
    }
}
