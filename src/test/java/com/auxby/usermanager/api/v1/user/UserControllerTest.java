package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
import com.auxby.usermanager.exception.RegistrationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;

import static com.auxby.usermanager.utils.TestUtils.getUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @Test
    @SneakyThrows
    @WithMockUser
    void createUser_shouldSucceed() {
        when(userService.createUser(any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test@gmail.com", null, "0740400200");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void createUser_shouldFail_whenEmailNotValid() {
        when(userService.createUser(any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test", null, "0740400200");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void createUser_shouldFail_whenPhoneNotSet() {
        when(userService.createUser(any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test@gmail.com", null, "");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getUserInfo_shouldSucceed() {
        when(userService.getUser(any()))
                .thenReturn(getMockUser());

        mockMvc.perform(get(getUrl(""))
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).getUser(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getUserInfo_shouldFail_whenUserNotFound() {
        when(userService.getUser(any()))
                .thenThrow(new EntityNotFoundException("Test exception."));

        mockMvc.perform(get(getUrl(""))
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void getUserInfo_shouldFail_whenRegistrationFailureExceptionIsThrown() {
        when(userService.getUser(any()))
                .thenThrow(new RegistrationException("Test exception."));

        mockMvc.perform(get(getUrl(""))
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().is(EXPECTATION_FAILED.value()));
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void deleteUser_shouldSucceed() {
        doNothing().when(userService)
                .deleteUser(any());

        mockMvc.perform(delete(getUrl(""))
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).deleteUser(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }

    private UserDetailsResponse getMockUser() {
        return new UserDetailsResponse("Doe", "Joe",
                "test@gmail.com", null, "0740400200");
    }
}