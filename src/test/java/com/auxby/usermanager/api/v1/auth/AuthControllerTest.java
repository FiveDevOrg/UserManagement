package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidated;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.auxby.usermanager.utils.TestUtils.getUrl;
import static com.auxby.usermanager.utils.enums.CustomHttpStatus.USER_EMAIL_NOT_VALIDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private AdapterConfig adapterConfig;

    @BeforeEach
    void setup() {
        when(adapterConfig.getRealm()).thenReturn("test");
        when(adapterConfig.getResource()).thenReturn("test");
        when(adapterConfig.getAuthServerUrl()).thenReturn("test");
    }

    @Test
    @SneakyThrows
    void login_shouldReturnToken() {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("test-token"));

        mockMvc.perform(post(getUrl("login"))
                        .content(mapper.writeValueAsString(new AuthInfo("test@gmail.com", "testPassword")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void login_shouldFail_whenEmailNotValidatedExceptionIsThrown() {
        when(authService.login(any()))
                .thenThrow(new UserEmailNotValidated("Test exception."));

        mockMvc.perform(post(getUrl("login"))
                        .content(mapper.writeValueAsString(new AuthInfo("test@gmail.com", "testPassword")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(USER_EMAIL_NOT_VALIDATED.getCode()));
    }

    @Test
    @SneakyThrows
    void login_shouldFail_whenSignInExceptionExceptionIsThrown() {
        when(authService.login(any()))
                .thenThrow(new SignInException("Test exception."));

        mockMvc.perform(post(getUrl("login"))
                        .content(mapper.writeValueAsString(new AuthInfo("test@gmail.com", "testPassword")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void login_shouldThrowException_whenEmailNotSet() {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("test-token"));

        mockMvc.perform(post(getUrl("login"))
                        .content(mapper.writeValueAsString(new AuthInfo("", "testPassword")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void resetPassword() {
        doReturn(true).when(authService)
                .resetPassword(any());

        mockMvc.perform(post(getUrl("reset"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(authService, times(1)).resetPassword(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }

    @Test
    @SneakyThrows
    void resendVerificationLink() {
        doReturn(true).when(authService)
                .resendVerificationLink(any());

        mockMvc.perform(post(getUrl("resend-verification-link"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(authService, times(1)).resendVerificationLink(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }
}