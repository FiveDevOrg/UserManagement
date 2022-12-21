package com.auxby.usermanager.utils.service;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.config.properties.KeycloakProps;
import com.auxby.usermanager.utils.TestMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {
    @Mock
    private WebClient webClient;
    @Mock
    private KeycloakProps keycloakProps;
    @Mock
    private KeycloakClient keycloakClient;
    @InjectMocks
    private KeycloakService keycloakService;

    @Test
    void performLogin() {
        setupKeycloakPropsMock();
        setupWebClientMock();

        var response = keycloakService.performLogin(new AuthInfo("test@email.com", "testPass"));
        assertNotNull(response);
        assertEquals("access_token", response.access_token());
        assertEquals("refresh_token", response.refresh_token());
    }

    @Test
    void getKeycloakUser() {
        var usersResourcesMock = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(usersResourcesMock);
        when(usersResourcesMock.search(any(), anyBoolean()))
                .thenReturn(List.of(mock(UserRepresentation.class)));

        var response = keycloakService.getKeycloakUser("test-uuid");
        assertTrue(response.isPresent());
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        verify(usersResourcesMock, times(1))
                .search(uuidArg.capture(), anyBoolean());
        assertEquals("test-uuid", uuidArg.getValue());
    }

    @Test
    void deleteKeycloakUser() {
        var mockUserResource = mock(UserResource.class);
        var usersResourcesMock = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(usersResourcesMock);
        when(usersResourcesMock.get(any()))
                .thenReturn(mockUserResource);

        keycloakService.deleteKeycloakUser("uuid");
        verify(mockUserResource, times(1)).remove();
    }

    @Test
    void sendVerificationPasswordLink_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();

        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);
        doNothing().when(mockUserResource).sendVerifyEmail();

        keycloakService.sendVerificationEmailLink(uuid);

        verify(mockUserResource, times(1)).sendVerifyEmail();
    }

    @Test
    void performUserUpdate() {
        var uuid = UUID.randomUUID().toString();

        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);

        keycloakService.performUserUpdate(uuid, mock(UserRepresentation.class));
        verify(mockUserResource, times(1)).update(any());
    }

    @Test
    void addUserRole() {
        var uuid = UUID.randomUUID().toString();
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        var mockRoleScopeResource = mock(RoleScopeResource.class);
        var mockRoleNappingResource = mock(RoleMappingResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);
        when(mockUserResource.roles())
                .thenReturn(mockRoleNappingResource);
        when(mockRoleNappingResource.realmLevel())
                .thenReturn(mockRoleScopeResource);

        keycloakService.addUserRole(uuid);
        ArgumentCaptor<List> roleArg = ArgumentCaptor.forClass(List.class);
        verify(mockRoleScopeResource, times(1))
                .add(roleArg.capture());
        assertEquals(1, roleArg.getValue().size());
    }

    @Test
    void sendResetPasswordLink() {
        var uuid = UUID.randomUUID().toString();

        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);

        keycloakService.sendResetPasswordLink(uuid);
        ArgumentCaptor<List<String>> actionsArg = ArgumentCaptor.forClass(List.class);
        verify(mockUserResource, times(1))
                .executeActionsEmail(actionsArg.capture());
        assertEquals(1, actionsArg.getValue().size());
        assertEquals("UPDATE_PASSWORD", actionsArg.getValue().get(0));
    }

    @Test
    void performCreateUser() {
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);

        keycloakService.performCreateUser(mock(UserRepresentation.class));
        verify(mockRealmUserResources, times(1))
                .create(any());
    }

    @Test
    void getUserRepresentation() {
        var uuid = UUID.randomUUID().toString();

        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation())
                .thenReturn(mock(UserRepresentation.class));

        var result = keycloakService.getUserRepresentation(uuid);
        assertNotNull(result);
    }

    private void setupKeycloakPropsMock() {
        when(keycloakProps.getAuthUrl())
                .thenReturn("http://test/auth");
        when(keycloakProps.getClientId())
                .thenReturn("test-client-id");
        when(keycloakProps.getClientId())
                .thenReturn("test-client-secret");
    }

    private void setupWebClientMock() {
        var mockWeb = mock(WebClient.RequestBodyUriSpec.class);
        var mockWebSpec = mock(WebClient.RequestBodySpec.class);
        var mockWebHeader = mock(WebClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(WebClient.ResponseSpec.class);
        when(webClient.post())
                .thenReturn(mockWeb);
        when(mockWeb.uri(anyString()))
                .thenReturn(mockWebSpec);
        when(mockWebSpec.body(any()))
                .thenReturn(mockWebHeader);
        when(mockWebHeader.retrieve())
                .thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToFlux(KeycloakAuthResponse.class))
                .thenReturn(Flux.just(TestMock.mockKeycloakAuthResponse()));
    }
}