package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidatedException;
import com.auxby.usermanager.utils.service.KeycloakService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.auxby.usermanager.utils.TestMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private KeycloakService keycloakService;
    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken() {
        UserRepresentation mockUserRepresentation = mock(UserRepresentation.class);
        when(mockUserRepresentation.isEmailVerified())
                .thenReturn(true);
        when(userService.findUser(any()))
                .thenReturn(mockUserDetails());
        when(keycloakService.getUserRepresentation(any()))
                .thenReturn(mockUserRepresentation);
        when(keycloakService.performLogin(any()))
                .thenReturn(mockKeycloakAuthResponse());

        var response = authService.login(new AuthInfo("test@email.com", "testPass"));
        assertEquals("access_token", response.token());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        UserRepresentation mockUserRepresentation = mock(UserRepresentation.class);
        when(mockUserRepresentation.isEmailVerified())
                .thenReturn(true);
        when(userService.findUser(any()))
                .thenReturn(mockUserDetails());
        when(keycloakService.getUserRepresentation(any()))
                .thenReturn(mockUserRepresentation);

        var request = new AuthInfo("test@email.com", "testPass");
        assertThrows(SignInException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowException_whenEmailNotVerified() {
        UserRepresentation mockUserRepresentation = mock(UserRepresentation.class);
        when(mockUserRepresentation.isEmailVerified())
                .thenReturn(false);
        when(userService.findUser(any()))
                .thenReturn(mockUserDetails());
        when(keycloakService.getUserRepresentation(any()))
                .thenReturn(mockUserRepresentation);

        var request = new AuthInfo("test@email.com", "testPass");
        assertThrows(UserEmailNotValidatedException.class, () -> authService.login(request));
    }

    @Test
    void resendVerificationLink_shouldSucceed() {
        var mockUser = mockUserDetails();
        when(userService.findUser(anyString()))
                .thenReturn(mockUser);
        authService.resendVerificationLink("tes@email");
        ArgumentCaptor<String> userUuidArg = ArgumentCaptor.forClass(String.class);
        verify(keycloakService, times(1)).sendVerificationEmailLink(userUuidArg.capture());
        assertEquals(mockUser.getAccountUuid(), userUuidArg.getValue());
    }

    @Test
    void resetPassword_shouldSucceed() {
        authService.resetPassword("tes@email");
        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).sendResetPasswordLink(emailArg.capture());
        assertEquals("tes@email", emailArg.getValue());
    }

}