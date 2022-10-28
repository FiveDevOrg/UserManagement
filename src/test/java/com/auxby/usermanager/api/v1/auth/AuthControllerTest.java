package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.auxby.usermanager.utils.TestUtils.getUrl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;

    @Test
    @SneakyThrows
    void login_shouldReturnToken() {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("test-token"));

        mockMvc.perform(post(getUrl("login"))
                        .content(mapper.writeValueAsString(new AuthInfo("test@gmail.com", "testPassword")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void login_shouldThrowException_whenEmailNotSet() {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("test-token"));

        mockMvc.perform(post(getUrl("login"))
                        .content(mapper.writeValueAsString(new AuthInfo("", "testPassword")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void resetPassword() {
        doNothing().when(authService)
                .resetPassword(any());

        mockMvc.perform(post(getUrl("reset"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@gmail.com"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(authService, times(1)).resetPassword(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }

    @Test
    @SneakyThrows
    void resendVerificationLink() {
        doNothing().when(authService)
                .resendVerificationLink(any());

        mockMvc.perform(post(getUrl("resend-verification-link"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@gmail.com"))
                .andExpect(status().isOk());
        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(authService, times(1)).resendVerificationLink(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }
}