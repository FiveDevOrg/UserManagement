package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.*;
import com.auxby.usermanager.exception.RegistrationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
    @WithMockUser
    void createUser_shouldSucceed() {
        when(userService.createUser(any(), any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test@gmail.com", null, "0740400200", "");

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
        when(userService.createUser(any(), any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test", null, "0740400200", "");

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
        when(userService.createUser(any(), any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test@gmail.com", null, null, "");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "uuid")
    void getUserInfo_shouldSucceed() {
        when(userService.getUser(any()))
                .thenReturn(getMockUser());

        mockMvc.perform(get(getUrl(""))
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).getUser(uuidArg.capture());
        assertEquals("uuid", uuidArg.getValue());
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
                        .with(csrf()))
                .andExpect(status().isOk());

        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).deleteUser(emailArg.capture());
        assertEquals("user", emailArg.getValue());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void updateUser_shouldSucceed() {
        when(userService.updateUser(any(), any()))
                .thenReturn(new UserDetailsResponse("Doe", "Joe", "test.com", null, "0740400200", "", 0));

        var mockUser = new UpdateUserInfo("Doe", "Joe", null, "0740400200");

        mockMvc.perform(put(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void updateUser_shouldFailIfEmailNotValid() {
        when(userService.updateUser(any(), any()))
                .thenReturn(new UserDetailsResponse("Doe", "Joe", "test.com", null, "0740400200", "", 0));

        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test.com", null, "0740400200", "");

        mockMvc.perform(put(getUrl("/test@gmail.com"))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @SneakyThrows
    @WithMockUser
    void checkUser_shouldSucceed() {
        when(userService.checkUserExists(any()))
                .thenReturn(Boolean.FALSE);

        mockMvc.perform(get(getUrl("/email/check"))
                        .param("email", "test@gmail.com")
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "uuid-test")
    void updateUserAvatar_shouldSucceed() {
        when(userService.updateUserAvatar(any(), any()))
                .thenReturn(new UploadAvatarResponse("avatar-uuid"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        mockMvc.perform(multipart(getUrl("/avatar"))
                        .file(file)
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful());
        ArgumentCaptor<String> userUuidArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).updateUserAvatar(any(), userUuidArg.capture());
        assertEquals("uuid-test", userUuidArg.getValue());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "uuid-test")
    void changeUserPassword_shouldSucceed() {
        when(userService.changePassword(any(), any()))
                .thenReturn(true);

        var request = new ChangePasswordDto("test", "test.1234");
        mockMvc.perform(put(getUrl("/password"))
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private UserDetailsResponse getMockUser() {
        return new UserDetailsResponse("Doe", "Joe",
                "test@gmail.com", null, "0740400200", "https://test-avatar", 0);
    }
}