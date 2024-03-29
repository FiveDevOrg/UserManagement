package com.auxby.usermanager.config;

import com.auxby.usermanager.config.properties.KeycloakProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakClient {

    private Keycloak keycloak;
    private final KeycloakProps keycloakProps;

    public UsersResource getKeycloakRealmUsersResources() {
        if (keycloak == null || keycloak.isClosed()) {
            initKeycloakClient();
        }
        return keycloak.realm(keycloakProps.getRealm()).users();
    }

    public RoleRepresentation getRealmRoleRepresentation(String role) {
        if (keycloak == null || keycloak.isClosed()) {
            initKeycloakClient();
        }
        return keycloak.realm(keycloakProps.getRealm())
                .roles()
                .get(role)
                .toRepresentation();
    }

    @PostConstruct
    private void initKeycloakClient() {
        log.info("Create keycloak-client");
        keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProps.getUrl())
                .realm(keycloakProps.getRealm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(keycloakProps.getClientId())
                .clientSecret(keycloakProps.getClientSecret())
                .resteasyClient(getResEasyClient())
                .build();

        try {
            if (keycloak.tokenManager().getAccessTokenString() == null) {
                throw new RuntimeException("Keycloak client configuration fails.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Keycloak client configuration fails.");
        }
    }

    private ResteasyClient getResEasyClient() {
        return new ResteasyClientBuilder()
                .connectionPoolSize(10)
                .build();
    }
}
