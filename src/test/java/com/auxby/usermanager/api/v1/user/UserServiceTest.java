package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.user.model.UpdateUserInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.entity.Address;
import com.auxby.usermanager.entity.Contact;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.RegistrationException;
import com.auxby.usermanager.utils.enums.ContactType;
import com.auxby.usermanager.utils.service.AmazonClientService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
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
    private KeycloakClient keycloakClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AmazonClientService awsService;

    @Test
    void createUser_shouldSucceed() {
        var randomUuid = UUID.randomUUID().toString();
        var mockUserDetails = getMockUserDetails(false);
        var mockResponse = mock(Response.class);
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        var mockUserRepresentation = mock(UserRepresentation.class);
        mockKeycloakClientActions(randomUuid, mockResponse, mockUserResource, mockRealmUserResources, mockUserRepresentation);
        when(userRepository.save(any()))
                .thenReturn(new UserDetails());
        doNothing().when(mockUserResource).sendVerifyEmail();

        var result = userService.createUser(mockUserDetails);
        assertNotNull(result);
        ArgumentCaptor<UserDetails> saveUserArg = ArgumentCaptor.forClass(UserDetails.class);
        verify(userRepository, times(1)).save(saveUserArg.capture());
        var savedUserPhone = saveUserArg.getValue().getContacts()
                .stream()
                .filter(c -> c.getType().equals(ContactType.PHONE))
                .map(Contact::getValue)
                .findFirst()
                .orElse(null);
        assertEquals(mockUserDetails.phone(), savedUserPhone);

        var savedUserEmail = saveUserArg.getValue().getContacts()
                .stream()
                .filter(c -> c.getType().equals(ContactType.EMAIL))
                .map(Contact::getValue)
                .findFirst()
                .orElse(null);
        assertEquals(mockUserDetails.email(), savedUserEmail);
        assertEquals(randomUuid, saveUserArg.getValue().getAccountUuid());
        assertEquals(mockUserDetails.firstName(), saveUserArg.getValue().getFirstName());
        assertEquals(mockUserDetails.lastName(), saveUserArg.getValue().getLastName());
        verify(mockUserResource, times(1)).sendVerifyEmail();
    }

    @Test
    void createUserWithAddress_shouldSucceed() {
        var randomUuid = UUID.randomUUID().toString();
        var mockUserDetails = getMockUserDetails(true);
        var mockResponse = mock(Response.class);
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        var mockUserRepresentation = mock(UserRepresentation.class);
        mockKeycloakClientActions(randomUuid, mockResponse, mockUserResource, mockRealmUserResources, mockUserRepresentation);
        when(userRepository.save(any()))
                .thenReturn(new UserDetails());
        doNothing().when(mockUserResource).sendVerifyEmail();

        var result = userService.createUser(mockUserDetails);
        assertNotNull(result);
        ArgumentCaptor<UserDetails> saveUserArg = ArgumentCaptor.forClass(UserDetails.class);
        verify(userRepository, times(1)).save(saveUserArg.capture());
        var savedUserPhone = saveUserArg.getValue().getContacts()
                .stream()
                .filter(c -> c.getType().equals(ContactType.PHONE))
                .map(Contact::getValue)
                .findFirst()
                .orElse(null);
        assertEquals(mockUserDetails.phone(), savedUserPhone);

        var savedUserEmail = saveUserArg.getValue().getContacts()
                .stream()
                .filter(c -> c.getType().equals(ContactType.EMAIL))
                .map(Contact::getValue)
                .findFirst()
                .orElse(null);
        assertEquals(mockUserDetails.email(), savedUserEmail);
        assertEquals(randomUuid, saveUserArg.getValue().getAccountUuid());
        assertEquals(mockUserDetails.firstName(), saveUserArg.getValue().getFirstName());
        assertEquals(mockUserDetails.lastName(), saveUserArg.getValue().getLastName());
        assertEquals(1, saveUserArg.getValue().getAddresses().size());
        verify(mockUserResource, times(1)).sendVerifyEmail();
    }

    @Test
    void createUser_shouldDeleteUser_whenPersistToDbFails() {
        var randomUuid = UUID.randomUUID().toString();
        var mockUserDetails = getMockUserDetails(true);
        var mockResponse = mock(Response.class);
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        var mockUserRepresentation = mock(UserRepresentation.class);
        mockKeycloakClientActions(randomUuid, mockResponse, mockUserResource, mockRealmUserResources, mockUserRepresentation);
        when(userRepository.save(any()))
                .thenThrow(new RuntimeException("Test exception."));
        doNothing().when(mockUserResource).remove();

        assertThrows(RegistrationException.class, () -> userService.createUser(mockUserDetails));
        verify(mockUserResource, times(1)).remove();
    }


    @Test
    void createUser_shouldFail_whenKeycloakRegistrationFails() {
        var mockUserDetails = getMockUserDetails(false);
        var mockResponse = mock(Response.class);
        var mockRealmUserResources = mock(UsersResource.class);

        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.create(any()))
                .thenReturn(mockResponse);
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CONFLICT.value());
        when(mockResponse.getStatusInfo())
                .thenReturn(Response.Status.CONFLICT);

        assertThrows(RegistrationException.class, () -> userService.createUser(mockUserDetails));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void getUser_shouldReturnUserDetails() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByAccountUuid(anyString()))
                .thenReturn(Optional.of(mockSavedUser(uuid)));

        var result = userService.getUser("uuid");
        assertNotNull(result);
        assertEquals("test@gmail.com", result.email());
        assertEquals("Suceava", result.address().city());
        assertEquals("Ro", result.address().country());
    }

    @Test
    void deleteUser_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.of(mockSavedUser(uuid)));
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);
        doNothing().when(mockUserResource).remove();

        userService.deleteUser("test@gmail.com");

        verify(mockUserResource, times(1))
                .remove();
        verify(userRepository, times(1))
                .deleteById(any());
    }

    @Test
    void sendResetPasswordLink_shouldSucceed() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.of(mockSavedUser(uuid)));
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);
        doNothing().when(mockUserResource).executeActionsEmail(anyList());

        userService.sendResetPasswordLink("test@gmail.com");
        ArgumentCaptor<List<String>> actionsArg = ArgumentCaptor.forClass(List.class);
        verify(mockUserResource, times(1))
                .executeActionsEmail(actionsArg.capture());
        assertEquals(1, actionsArg.getValue().size());
        assertEquals("UPDATE_PASSWORD", actionsArg.getValue().get(0));
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

        userService.sendVerificationEmailLink(uuid);

        verify(mockUserResource, times(1)).sendVerifyEmail();
    }

    @Test
    void sendVerificationPasswordLink_shouldSucceed_whenExceptionIsThrown() {
        var uuid = UUID.randomUUID().toString();
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenThrow(new RuntimeException("Test exception."));

        assertDoesNotThrow(() -> userService.sendVerificationEmailLink(uuid));
    }

    @Test
    void findUser_shouldReturnUserDetails() {
        var uuid = UUID.randomUUID().toString();
        when(userRepository.findUserDetailsByUserName(anyString()))
                .thenReturn(Optional.of(mockSavedUser(uuid)));

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
                .thenReturn(Optional.of(mockSavedUser(uuid)));
        var mockUserResource = mock(UserResource.class);
        var mockRealmUserResources = mock(UsersResource.class);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.get(uuid))
                .thenReturn(mockUserResource);
        doNothing().when(mockUserResource).update(any());

        userService.updateUser("uuid", getMockUserUpdateDetails(true));
        verify(mockUserResource, times(1)).update(any());
    }

    @Test
    void updateUserDetailsWithDifferentEmail_shouldSucceed() {
        var request = getMockUserUpdateDetails(false);
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser("test2@gmail.com", request));
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

        String result = userService.updateUserAvatar(mock(MultipartFile.class), "uuid");
        assertNotNull(result);
    }

    private UserDetailsInfo getMockUserDetails(boolean setAddress) {
        if (setAddress) {
            return new UserDetailsInfo("Joe", "Doe", "testPass",
                    "test@email.com", new AddressInfo("Suceava", "RO"), "0749599399");
        } else {
            return new UserDetailsInfo("Joe", "Doe", "testPass",
                    "test@email.com", null, "0749599399");
        }
    }

    private UpdateUserInfo getMockUserUpdateDetails(boolean setAddress) {
        if (setAddress) {
            return new UpdateUserInfo("Joe", "Doe",
                    new AddressInfo("Suceava", "RO"), "0749599399");
        } else {
            return new UpdateUserInfo("Joe", "Doe", new AddressInfo("Suceava", "RO"),
                    "0749599399");
        }
    }

    private void mockKeycloakClientActions(String randomUuid,
                                           Response mockResponse,
                                           UserResource mockUserResource,
                                           UsersResource mockRealmUserResources,
                                           UserRepresentation mockUserRepresentation) {
        when(keycloakClient.getRealmRoleRepresentation(anyString()))
                .thenReturn(mock(RoleRepresentation.class));
        when(mockUserRepresentation.getId())
                .thenReturn(randomUuid);
        when(keycloakClient.getKeycloakRealmUsersResources())
                .thenReturn(mockRealmUserResources);
        when(mockRealmUserResources.create(any()))
                .thenReturn(mockResponse);
        when(mockRealmUserResources.search("test@email.com", true))
                .thenReturn(List.of(mockUserRepresentation));
        when(mockResponse.getStatus())
                .thenReturn(HttpStatus.CREATED.value());
        when(mockRealmUserResources.get(randomUuid))
                .thenReturn(mockUserResource);
        var mockRoleMappingResource = mock(RoleMappingResource.class);
        var mockRoleScopeResource = mock(RoleScopeResource.class);
        when(mockRoleMappingResource.realmLevel())
                .thenReturn(mockRoleScopeResource);
        when(mockUserResource.roles())
                .thenReturn(mockRoleMappingResource);
    }

    private UserDetails mockSavedUser(String randomUuid) {
        var user = new UserDetails();
        user.setId(1);
        user.setFirstName("Joe");
        user.setLastName("Doe");
        var address = new Address();
        address.setId(1);
        address.setStreet("");
        address.setCity("Suceava");
        address.setCountry("Ro");
        user.addAddress(address);
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

}