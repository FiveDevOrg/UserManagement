package com.auxby.usermanager.config;

import com.auxby.usermanager.config.properties.KeycloakProps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.UsersResource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class KeycloakClientTest {
    private KeycloakClient keycloakClient;
    @Mock
    private KeycloakProps keycloakProps;

    @BeforeEach
    void setup() {
        keycloakClient = new KeycloakClient(keycloakProps);
    }

    @Test
    void getKeycloakRealmUsersResources() {
        when(keycloakProps.getUrl())
                .thenReturn("Test");
        when(keycloakProps.getRealm())
                .thenReturn("Test");
        when(keycloakProps.getClientId())
                .thenReturn("Test");
        when(keycloakProps.getClientSecret())
                .thenReturn("Test");
        UsersResource result = keycloakClient.getKeycloakRealmUsersResources();
        assertNotNull(result);
        UsersResource resource = keycloakClient.getKeycloakRealmUsersResources();
    }
}