package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.config.properties.KeycloakProps;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private WebClient webClientMock;
    @Mock
    private UserService userService;
    @Mock
    private KeycloakProps keycloakProps;
    @Mock
    private KeycloakClient keycloakClient;
    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken() {
        setupKeycloakPropsMock();

        when(userService.findUser(any()))
                .thenReturn(mockUserDetails());
        var randomUuid = UUID.randomUUID();
        setupKeycloakMock(true);
        setupWebClientMock(randomUuid);
        var response = authService.login(new AuthInfo("test@email.com", "testPass"));
        assertEquals(randomUuid.toString(), response.token());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        when(userService.findUser(any()))
                .thenReturn(mockUserDetails());
        setupKeycloakMock(true);
        when(webClientMock.post())
                .thenThrow(new WebClientResponseException(401, "Test exception", null, null, null));
        assertThrows(SignInException.class, () -> authService.login(new AuthInfo("test@email.com", "testPass")));
    }

    private void setupKeycloakPropsMock() {
        when(keycloakProps.getAuthUrl())
                .thenReturn("http://test/auth");
        when(keycloakProps.getClientId())
                .thenReturn("test-client-id");
        when(keycloakProps.getClientId())
                .thenReturn("test-client-secret");
    }

    @Test
    void login_shouldThrowException_whenEmailNotVerified() {
        when(userService.findUser(any()))
                .thenReturn(mockUserDetails());
        setupKeycloakMock(false);
        assertThrows(UserEmailNotValidated.class, () -> authService.login(new AuthInfo("test@email.com", "testPass")));
    }

    @Test
    void resendVerificationLink_shouldSucceed() {
        var mockUser = mockUserDetails();
        when(userService.findUser(anyString()))
                .thenReturn(mockUser);
        authService.resendVerificationLink("tes@email");
        ArgumentCaptor<String> userUuidArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).sendVerificationPasswordLink(userUuidArg.capture());
        assertEquals(mockUser.getAccountUuid(), userUuidArg.getValue());
    }

    @Test
    void resetPassword_shouldSucceed() {
        authService.resetPassword("tes@email");
        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).sendResetPasswordLink(emailArg.capture());
        assertEquals("tes@email", emailArg.getValue());
    }

    private UserDetails mockUserDetails() {
        var mockUserDetails = new UserDetails();
        mockUserDetails.setAccountUuid("uuid-test-acc");
        return mockUserDetails;
    }

    private void setupWebClientMock(UUID randomUuid) {
        var mockWeb = mock(WebClient.RequestBodyUriSpec.class);
        var mockWebSpec = mock(WebClient.RequestBodySpec.class);
        var mockWebHeader = mock(WebClient.RequestHeadersSpec.class);
        var mockResponseSpec = mock(WebClient.ResponseSpec.class);
        when(webClientMock.post())
                .thenReturn(mockWeb);
        when(mockWeb.uri(anyString()))
                .thenReturn(mockWebSpec);
        when(mockWebSpec.body(any()))
                .thenReturn(mockWebHeader);
        when(mockWebHeader.retrieve())
                .thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToFlux(KeycloakAuthResponse.class))
                .thenReturn(Flux.just(new KeycloakAuthResponse(randomUuid.toString(), 0L, 0L, "refresh-" + randomUuid, "", "")));
    }

    private void setupKeycloakMock(boolean isEmailVerified) {
        var usersResourcesMock = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(usersResourcesMock);
        var userResourceMock = mock(UserResource.class);
        when(usersResourcesMock.get(anyString()))
                .thenReturn(userResourceMock);
        var userRepresentationMock = mock(UserRepresentation.class);
        when(userResourceMock.toRepresentation())
                .thenReturn(userRepresentationMock);
        when(userRepresentationMock.isEmailVerified())
                .thenReturn(isEmailVerified);
    }
}