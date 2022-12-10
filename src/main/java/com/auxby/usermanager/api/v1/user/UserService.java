package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.entity.Address;
import com.auxby.usermanager.entity.Contact;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.RegistrationException;
import com.auxby.usermanager.utils.enums.ContactType;
import com.auxby.usermanager.utils.service.AmazonClientService;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private static final String UNKNOWN = "Unknown";
    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    private final UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    private final AmazonClientService awsService;

    @Transactional
    public UserDetailsResponse createUser(UserDetailsInfo userInfo) {
        try (Response response = keycloakClient.getKeycloakRealmUsersResources().create(createUserRepresentation(userInfo, false))) {
            if (response.getStatus() != HttpStatus.CREATED.value()) {
                throw new RegistrationException("User registration failed. " + response.getStatusInfo().getReasonPhrase());
            }
            UserDetails userDetails = mapToUserDetails(userInfo);
            try {
                Set<Contact> contacts = getUserContacts(userInfo.email(), userInfo.phone());
                contacts.forEach(userDetails::addContact);
                if (userInfo.address() != null) {
                    Address addresses = getUserAddress(userInfo.address());
                    userDetails.addAddress(addresses);
                }
                UserDetails newUser = userRepository.save(userDetails);
                sendVerificationEmailLink(userDetails.getAccountUuid());

                return mapToUserDetailsInfo(newUser, newUser.getContacts(), newUser.getAddresses());
            } catch (Exception ex) {
                deleteKeycloakUser(userDetails.getAccountUuid());
                throw new RegistrationException("Something went wrong. User registration failed:" + ex.getMessage());
            }
        }
    }

    public UserDetailsResponse getUser(String userUuid) {
        UserDetails userDetails = userRepository.findUserDetailsByAccountUuid(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        return mapToUserDetailsInfo(userDetails, userDetails.getContacts(), userDetails.getAddresses());
    }

    @Transactional
    public void deleteUser(String userName) {
        UserDetails userDetails = findUser(userName);
        deleteKeycloakUser(userDetails.getAccountUuid());
        userRepository.deleteById(userDetails.getId());
    }

    public Boolean checkUserExists(String userName) {
        Optional<UserDetails> userDetails = userRepository
                .findUserDetailsByUserName(userName);

        return userDetails.isPresent();
    }

    @Transactional
    public void updateUser(String userEmail, UserDetailsInfo userDetails) {
        UserDetails user = findUser(userEmail);
        boolean isEmailVerifiedAlready = userEmail.equals(userDetails.email());
        keycloakClient.getKeycloakRealmUsersResources()
                .get(user.getAccountUuid())
                .update(createUserRepresentation(userDetails, isEmailVerifiedAlready));
        user.setUserName(userDetails.email());
        user.setLastName(userDetails.lastName());
        user.setFirstName(userDetails.firstName());
        var userAddresses = new ArrayList<>(user.getAddresses());
        var userContacts = new ArrayList<>(user.getContacts());
        userAddresses.forEach(user::removeAddress);
        userContacts.forEach(user::removeContact);

        Set<Contact> contacts = getUserContacts(userDetails.email(), userDetails.phone());
        contacts.forEach(user::addContact);
        if (userDetails.address() != null) {
            Address addresses = getUserAddress(userDetails.address());
            user.addAddress(addresses);
        }
        if (!isEmailVerifiedAlready) {
            sendVerificationEmailLink(user.getAccountUuid());
        }
    }

    public boolean sendResetPasswordLink(String email) {
        UserDetails userDetails = findUser(email);
        keycloakClient.getKeycloakRealmUsersResources()
                .get(userDetails.getAccountUuid())
                .executeActionsEmail(List.of(UPDATE_PASSWORD));

        return true;
    }

    public void sendVerificationEmailLink(String userId) {
        try {
            UserResource user = keycloakClient.getKeycloakRealmUsersResources()
                    .get(userId);
            user.sendVerifyEmail();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public UserDetails findUser(String userName) {
        return userRepository.findUserDetailsByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Username %s not found.", userName)));
    }

    @Transactional
    public String updateUserAvatar(MultipartFile avatar, String userUuid) {
        try {
            String avatarUrl = awsService.uploadAvatar(avatar, userUuid);
            Optional<UserDetails> userDetails = userRepository.findUserDetailsByAccountUuid(userUuid);
            userDetails.ifPresent(details -> details.setAvatarUrl(avatarUrl));

            return avatarUrl;
        } catch (IOException exception) {
            return "";
        }
    }

    private UserRepresentation createUserRepresentation(UserDetailsInfo userInfo, boolean isEmailVerified) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(isEmailVerified);
        userRepresentation.setEmail(userInfo.email());
        userRepresentation.setUsername(userInfo.email());
        userRepresentation.setLastName(userInfo.lastName());
        userRepresentation.setFirstName(userInfo.firstName());
        userRepresentation.setCreatedTimestamp(System.currentTimeMillis());
        userRepresentation.setCredentials(Collections.singletonList(getCredentialRepresentation(userInfo.password())));

        return userRepresentation;
    }

    private CredentialRepresentation getCredentialRepresentation(@NotBlank String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(OAuth2Constants.PASSWORD);
        credential.setTemporary(false);
        credential.setValue(password);

        return credential;
    }

    private UserDetailsResponse mapToUserDetailsInfo(UserDetails user, Set<Contact> contacts, Set<Address> addresses) {
        String phone = getPhoneNumber(contacts);
        String email = getEmailAddress(contacts);
        Address address = addresses.stream()
                .findFirst()
                .orElse(null);
        if (address == null) {
            return new UserDetailsResponse(user.getLastName(), user.getFirstName(), email, null, phone, user.getAvatarUrl());

        }
        AddressInfo addressInfo = new AddressInfo(address.getCity(), address.getCountry());
        return new UserDetailsResponse(user.getLastName(), user.getFirstName(), email, addressInfo, phone, user.getAvatarUrl());
    }

    private String getPhoneNumber(Set<Contact> contacts) {
        Contact phone = contacts.stream()
                .filter(c -> c.getType().equals(ContactType.PHONE))
                .findFirst()
                .orElse(null);
        if (phone == null) {
            return "";
        }
        return phone.getValue();
    }

    private String getEmailAddress(Set<Contact> contacts) {
        Contact email = contacts.stream()
                .filter(c -> c.getType().equals(ContactType.EMAIL))
                .findFirst()
                .orElse(null);
        if (email == null) {
            return "";
        }
        return email.getValue();
    }

    private UserDetails mapToUserDetails(UserDetailsInfo userInfo) {
        UserDetails userDetails = new UserDetails();
        userDetails.setGender(UNKNOWN);
        userDetails.setLastName(userInfo.lastName());
        userDetails.setFirstName(userInfo.firstName());
        userDetails.setUserName(userInfo.email());
        var keycloakUser = getKeycloakUser(userInfo.email());
        if (keycloakUser.isEmpty()) {
            throw new RegistrationException("User not found.");
        }
        userDetails.setAccountUuid(keycloakUser.get().getId());
        keycloakClient.getKeycloakRealmUsersResources()
                .get(keycloakUser.get().getId())
                .roles()
                .realmLevel()
                .add(Collections.singletonList(keycloakClient.getRealmRoleRepresentation("auxby_user")));
        return userDetails;
    }

    private Address getUserAddress(AddressInfo address) {
        Address userAddress = new Address();
        userAddress.setCity(address.city());
        userAddress.setCountry(address.country());
        userAddress.setStreet("");

        return userAddress;
    }

    private Set<Contact> getUserContacts(String email, String phone) {
        Set<Contact> contacts = new HashSet<>();
        contacts.add(getUserEmail(email));
        contacts.add(getUserPhone(phone));

        return contacts;
    }

    private Contact getUserEmail(String email) {
        Contact contact = new Contact();
        contact.setType(ContactType.EMAIL);
        contact.setValue(email);

        return contact;
    }

    private Contact getUserPhone(String phoneNumber) {
        Contact contact = new Contact();
        contact.setType(ContactType.PHONE);
        contact.setValue(phoneNumber);

        return contact;
    }

    private Optional<UserRepresentation> getKeycloakUser(String userName) {
        return keycloakClient.getKeycloakRealmUsersResources()
                .search(userName, true)
                .stream()
                .findFirst();
    }

    private void deleteKeycloakUser(String userId) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(userId)
                .remove();
    }
}
