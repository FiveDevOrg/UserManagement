package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
    void createUser_shouldSucceed() {
        when(userService.createUser(any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test@gmail.com", null, "0740400200");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void createUser_shouldFail_whenEmailNotValid() {
        when(userService.createUser(any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test", null, "0740400200");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createUser_shouldFail_whenPhoneNotSet() {
        when(userService.createUser(any()))
                .thenReturn(getMockUser());
        var mockUser = new UserDetailsInfo("Doe", "Joe", "testPass",
                "test@gmail.com", null, "");

        mockMvc.perform(post(getUrl(""))
                        .content(mapper.writeValueAsString(mockUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void getUserInfo_shouldSucceed() {
        when(userService.getUser(any()))
                .thenReturn(getMockUser());

        mockMvc.perform(get(getUrl(""))
                        .param("email", "test@gmail.com"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> emailArg = ArgumentCaptor.forClass(String.class);
        verify(userService, times(1)).getUser(emailArg.capture());
        assertEquals("test@gmail.com", emailArg.getValue());
    }

    @Test
    @SneakyThrows
    void deleteUser_shouldSucceed() {
        doNothing().when(userService)
                .deleteUser(any());

        mockMvc.perform(delete(getUrl(""))
                        .param("email", "test@gmail.com"))
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