package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.user.model.ChangePasswordDto;
import com.auxby.usermanager.api.v1.user.model.UpdateUserInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.entity.Address;
import com.auxby.usermanager.entity.Contact;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.ActionNotAllowException;
import com.auxby.usermanager.exception.ChangePasswordException;
import com.auxby.usermanager.exception.RegistrationException;
import com.auxby.usermanager.utils.TestMock;
import com.auxby.usermanager.utils.enums.ContactType;
import com.auxby.usermanager.utils.service.AmazonClientService;
import com.auxby.usermanager.utils.service.KeycloakService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AmazonClientService awsService;
    @Mock
    private KeycloakService keycloakService;

    @Test
    void createUser_shouldSucceed() {
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CREATED.value());
        when(keycloakService.performCreateUser(any()))
                .thenReturn(mockResponse);
        when(userRepository.save(any()))
                .thenReturn(mockUser("test-uuid", false));

        UserRepresentation mock = mock(UserRepresentation.class);
        when(mock.getId()).thenReturn("test-uuid");
        when(keycloakService.getKeycloakUser(anyString()))
                .thenReturn(Optional.of(mock));
        doNothing().when(keycloakService)
                .sendVerificationEmailLink(any());
        doNothing().when(keycloakService)
                .addUserRole(anyString());

        var request = getMockUserDetails(false);
        var result = userService.createUser(request, false);
        assertNotNull(result);
        assertSaveIsPerformed(request, false);
        assertCreateUserKeycloakActionsArePerformed(request, "test-uuid");
    }

    @Test
    void createUserWithAddress_shouldSucceed() {
        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CREATED.value());
        when(keycloakService.performCreateUser(any()))
                .thenReturn(mockResponse);
        when(userRepository.save(any()))
                .thenReturn(mockUser("test-uuid", false));

        UserRepresentation mock = mock(UserRepresentation.class);
        when(mock.getId()).thenReturn("test-uuid");
        when(keycloakService.getKeycloakUser(anyString()))
                .thenReturn(Optional.of(mock));
        doNothing().when(keycloakService)
                .sendVerificationEmailLink(any());
        doNothing().when(keycloakService)
                .addUserRole(anyString());

        var request = getMockUserDetails(true);
        var result = userService.createUser(request, false);
        assertNotNull(result);
        assertNotNull(result);
        assertSaveIsPerformed(request, true);
        assertCreateUserKeycloakActionsArePerformed(request, "test-uuid");
    }

    @Test
    void createUser_shouldFail_whenKeycloakUserNotFound() {
        var mockResponse = mock(Response.class);
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CREATED.value());
        when(keycloakService.performCreateUser(any()))
                .thenReturn(mockResponse);
        when(keycloakService.getKeycloakUser(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(RegistrationException.class, () -> userService.createUser(getMockUserDetails(false), false));
    }

    @Test
    void createUser_shouldDeleteUser_whenPersistToDbFails() {
        var mockResponse = mock(Response.class);
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CREATED.value());
        UserRepresentation mock = mock(UserRepresentation.class);
        when(mock.getId()).thenReturn("test-uuid");
        when(keycloakService.getKeycloakUser(anyString()))
                .thenReturn(Optional.of(mock));
        when(keycloakService.performCreateUser(any()))
                .thenReturn(mockResponse);
        when(userRepository.save(any()))
                .thenThrow(new RuntimeException("Test exception."));

        assertThrows(RegistrationException.class, () -> userService.createUser(getMockUserDetails(false), false));
        verify(keycloakService, times(1)).deleteKeycloakUser(any());
    }


    @Test
    void createUser_shouldFail_whenKeycloakRegistrationFails() {
        var mockResponse = mock(Response.class);
        Response.StatusType mockStatus = mock(Response.StatusType.class);
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CONFLICT.value());
        when(mockResponse.getStatusInfo())
                .thenReturn(mockStatus);
        when(mockStatus.getReasonPhrase())
                .thenReturn("Test exception");
        when(keycloakService.performCreateUser(any()))
                .thenReturn(mockResponse);

        assertThrows(RegistrationException.class, () -> userService.createUser(getMockUserDetails(false), false));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void getUser_shouldReturnUserDetails() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser(uuid, true)));

        var result = userService.getUser("uuid");
        assertNotNull(result);
        assertEquals("test@gmail.com", result.email());
        assertEquals("Suceava", result.address().city());
        assertEquals("Ro", result.address().country());
    }

    @Test
    void getUser_shouldReturnUserDetails_withNoContactDetails() {
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(TestMock.mockUserDetails()));

        var result = userService.getUser("uuid");
        assertNotNull(result);
        assertEquals("", result.email());
        assertEquals("", result.phone());
    }

    @Test
    void deleteUser_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser(uuid, true)));

        userService.deleteUser("test");
        verify(keycloakService, times(1))
                .deleteKeycloakUser(any());
        verify(userRepository, times(1))
                .deleteById(any());
    }

    @Test
    void deleteUser_shouldSucceed_shouldFail() {
        var uuid = UUID.randomUUID().toString();
        var mockUser = mockUser(uuid, true);
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser));
        when(userRepository.getTopBidderIdForOffers())
                .thenReturn(List.of(mockUser.getId()));

        assertThrows(ActionNotAllowException.class, () -> userService.deleteUser("test"));
        verify(keycloakService, times(0))
                .deleteKeycloakUser(any());
        verify(userRepository, times(0))
                .deleteById(any());
    }

    @Test
    void sendResetPasswordLink_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.of(mockUser(uuid, true)));


        userService.sendResetPasswordLink("test@gmail.com");
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        verify(keycloakService, times(1))
                .sendResetPasswordLink(uuidArg.capture());
        assertEquals(uuid, uuidArg.getValue());
    }

    @Test
    void findUser_shouldReturnUserDetails() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.of(mockUser(uuid, true)));

        var result = userService.findUser("test@gmail.com");
        assertNotNull(result);
    }

    @Test
    void findUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findUser("test@gmail.com"));
    }

    @Test
    void updateUserDetails_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser(uuid, true)));

        var userInfo = getMockUserUpdateDetails(true);
        userService.updateUser(uuid, userInfo);
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserRepresentation> userRepresentationArg = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(keycloakService, times(1))
                .performUserUpdate(uuidArg.capture(), userRepresentationArg.capture());
        assertTrue(userRepresentationArg.getValue().isEnabled());
        assertTrue(userRepresentationArg.getValue().isEmailVerified());
        assertEquals(userInfo.lastName(), userRepresentationArg.getValue().getLastName());
        assertEquals(userInfo.firstName(), userRepresentationArg.getValue().getFirstName());
        assertEquals(uuid, uuidArg.getValue());
    }

    @Test
    void updateUserDetailsWithDifferentEmail_shouldFail() {
        var request = getMockUserUpdateDetails(false);
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser("test2@gmail.com", request));
    }

    @Test
    void updateUserDetailsWithNoAddress_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser(uuid, false)));

        var userInfo = getMockUserUpdateDetails(false);
        userService.updateUser(uuid, userInfo);
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserRepresentation> userRepresentationArg = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(keycloakService, times(1))
                .performUserUpdate(uuidArg.capture(), userRepresentationArg.capture());
        assertTrue(userRepresentationArg.getValue().isEnabled());
        assertTrue(userRepresentationArg.getValue().isEmailVerified());
        assertEquals(userInfo.lastName(), userRepresentationArg.getValue().getLastName());
        assertEquals(userInfo.firstName(), userRepresentationArg.getValue().getFirstName());
        assertEquals(uuid, uuidArg.getValue());
    }

    @Test
    void checkUserExists() {
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.empty());

        Boolean result = userService.checkUserExists("test");
        assertFalse(result);
        ArgumentCaptor<String> userNameArg = ArgumentCaptor.forClass(String.class);
        verify(userRepository, times(1)).findUserDetailsByUserName(userNameArg.capture());
        assertEquals("test", userNameArg.getValue());
    }

    @Test
    @SneakyThrows
    void updateUserAvatar() {
        when(awsService.uploadAvatar(any(), anyString()))
                .thenReturn("avatar-uuid");
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(new UserDetails()));

        var result = userService.updateUserAvatar(mock(MultipartFile.class), "uuid");
        assertNotNull(result);
    }

    @Test
    @SneakyThrows
    void updateUserAvatar_shouldReturnNothing_whenAwsUploadFails() {
        when(awsService.uploadAvatar(any(), anyString()))
                .thenThrow(new IOException("Test exception."));

        var result = userService.updateUserAvatar(mock(MultipartFile.class), "uuid");
        assertEquals("unavailable", result.avatarUrl());
    }

    @Test
    void changeUserPassword() {
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser("test-uuid", true)));

        boolean result = userService.changePassword(new ChangePasswordDto("test", "test.1234"), "test-uuid");
        assertTrue(result);
        ArgumentCaptor<AuthInfo> authInfo = ArgumentCaptor.forClass(AuthInfo.class);
        verify(keycloakService, times(1)).performLogin(authInfo.capture());
        assertEquals("test@gmail.com", authInfo.getValue().email());
        assertEquals("test", authInfo.getValue().password());
        ArgumentCaptor<String> uuidArg = ArgumentCaptor.forClass(String.class);
        verify(keycloakService, times(1)).performUserUpdate(uuidArg.capture(), any());
        assertEquals("test-uuid", uuidArg.getValue());
    }

    @Test
    void changeUserPassword_shouldFail_whenKeycloakLoginFails() {
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockUser("test-uuid", true)));
        when(keycloakService.performLogin(any()))
                .thenThrow(mock(WebClientResponseException.class));

        var request = new ChangePasswordDto("test", "test.1234");
        assertThrows(ChangePasswordException.class,
                () -> userService.changePassword(request, "test-uuid"));
    }

    private UserDetailsInfo getMockUserDetails(boolean setAddress) {
        if (setAddress) {
            return new UserDetailsInfo("Joe", "Doe", "testPass",
                    "test@email.com", new AddressInfo("Suceava", "RO"), "0749599399", "");
        } else {
            return new UserDetailsInfo("Joe", "Doe", "testPass",
                    "test@email.com", null, "0749599399", "");
        }
    }

    private UpdateUserInfo getMockUserUpdateDetails(boolean setAddress) {
        if (setAddress) {
            return new UpdateUserInfo("Joe", "Doe",
                    new AddressInfo("Suceava", "RO"), "0749599399");
        } else {
            return new UpdateUserInfo("Joe", "Doe", null, "0749599399");
        }
    }

    private UserDetails mockUser(String randomUuid,
                                 boolean setAddress) {
        var user = new UserDetails();
        user.setId(1);
        user.setFirstName("Joe");
        user.setLastName("Doe");
        user.setUserName("test@gmail.com");

        if (setAddress) {
            var address = new Address();
            address.setId(1);
            address.setStreet("");
            address.setCity("Suceava");
            address.setCountry("Ro");
            user.addAddress(address);
        }
        var phone = new Contact();
        phone.setId(1);
        phone.setValue("0755444322");
        phone.setType(ContactType.PHONE);
        var email = new Contact();
        email.setId(2);
        email.setValue("test@gmail.com");
        email.setType(ContactType.EMAIL);
        user.setAccountUuid(randomUuid);
        user.addContact(phone);
        user.addContact(email);

        return user;
    }

    private void assertSaveIsPerformed(UserDetailsInfo request, boolean hasAddress) {
        ArgumentCaptor<UserDetails> saveUserArg = ArgumentCaptor.forClass(UserDetails.class);
        verify(userRepository, times(1)).save(saveUserArg.capture());
        var savedUserPhone = saveUserArg.getValue().getContacts()
                .stream()
                .filter(c -> c.getType().equals(ContactType.PHONE))
                .map(Contact::getValue)
                .findFirst()
                .orElse(null);
        assertEquals(request.phone(), savedUserPhone);
        var email = saveUserArg.getValue().getContacts()
                .stream()
                .filter(c -> c.getType().equals(ContactType.EMAIL))
                .map(Contact::getValue)
                .findFirst()
                .orElse(null);
        assertEquals(request.email(), email);

        if (hasAddress) {
            var address = saveUserArg.getValue().getAddresses()
                    .stream()
                    .findFirst()
                    .orElse(null);
            assertNotNull(address);
            assertEquals(request.address().city(), address.getCity());
            assertEquals(request.address().country(), address.getCountry());
        }
    }

    private void assertCreateUserKeycloakActionsArePerformed(UserDetailsInfo request, String uuid) {
        ArgumentCaptor<UserRepresentation> userRepresentationArg = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(keycloakService, times(1)).performCreateUser(userRepresentationArg.capture());
        assertTrue(userRepresentationArg.getValue().isEnabled());
        assertFalse(userRepresentationArg.getValue().isEmailVerified());
        assertFalse(userRepresentationArg.getValue().getCredentials().isEmpty());
        assertEquals(request.email(), userRepresentationArg.getValue().getEmail());
        assertEquals(request.email(), userRepresentationArg.getValue().getUsername());
        assertEquals(request.lastName(), userRepresentationArg.getValue().getLastName());
        assertEquals(request.firstName(), userRepresentationArg.getValue().getFirstName());

        ArgumentCaptor<String> getUserArg = ArgumentCaptor.forClass(String.class);
        verify(keycloakService, times(1)).getKeycloakUser(getUserArg.capture());
        assertEquals(request.email(), getUserArg.getValue());

        ArgumentCaptor<String> addUserRoleArg = ArgumentCaptor.forClass(String.class);
        verify(keycloakService, times(1)).addUserRole(addUserRoleArg.capture());
        assertEquals(uuid, addUserRoleArg.getValue());

        ArgumentCaptor<String> sendVerificationEmailArg = ArgumentCaptor.forClass(String.class);
        verify(keycloakService, times(1)).sendVerificationEmailLink(sendVerificationEmailArg.capture());
        assertEquals(uuid, sendVerificationEmailArg.getValue());
    }

}