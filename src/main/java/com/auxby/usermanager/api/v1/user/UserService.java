package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.AddressService;
import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.contact.ContactService;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
import com.auxby.usermanager.config.KeycloakClient;
import com.auxby.usermanager.entity.Address;
import com.auxby.usermanager.entity.Contact;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.RegistrationException;
import com.auxby.usermanager.utils.enums.ContactType;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.core.Response;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String UNKNOWN = "Unknown";
    private final UserRepository userRepository;
    private final ContactService contactService;
    private final AddressService addressService;
    private final KeycloakClient keycloakClient;

    @Transactional
    public UserDetailsResponse createUser(UserDetailsInfo userInfo) {
        Response response = keycloakClient.getKeycloakRealmUsersResources().create(createUserRepresentation(userInfo));
        if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new RegistrationException("User registration failed. " + response.getStatusInfo().getReasonPhrase());
        }
        UserDetails userDetails = mapToUserDetails(userInfo);
        try {
            UserDetails newUser = userRepository.save(userDetails);
            List<Contact> contacts = contactService.saveContacts(getUserContacts(newUser, userInfo.email(), userInfo.phone()));
            List<Address> addresses = new ArrayList<>();
            if (userInfo.address() != null) {
                addresses.addAll(addressService.saveAddress(getUserAddress(newUser, userInfo.address())));
            }
            return mapToUserDetailsInfo(newUser, contacts, addresses);
        } catch (Exception ex) {
            deleteKeycloakUser(userDetails.getAccountUuid());
            throw new RegistrationException("Something went wrong. User registration failed:" + ex.getMessage());
        }
    }

    public UserDetailsResponse getUser(String userName) {
        UserDetails userDetails = userRepository.findUserDetailsByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Username %s not found.", userName)));
        return mapToUserDetailsInfo(userDetails, userDetails.getContacts().stream().toList(), userDetails.getAddresses().stream().toList());
    }

    public void deleteUser(String userName) {
        UserDetails userDetails = userRepository.findUserDetailsByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Username %s not found.", userName)));
        deleteKeycloakUser(userDetails.getAccountUuid());
        userRepository.delete(userDetails);
    }

    private UserRepresentation createUserRepresentation(UserDetailsInfo userInfo) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(false);
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

    private UserDetailsResponse mapToUserDetailsInfo(UserDetails user, List<Contact> contacts, List<Address> addresses) {
        String phone = getPhoneNumber(contacts);
        String email = getEmailAddress(contacts);
        Address address = addresses.stream()
                .findFirst()
                .orElse(null);
        if (address == null) {
            return new UserDetailsResponse(user.getLastName(), user.getFirstName(), email, null, phone);

        }
        AddressInfo addressInfo = new AddressInfo(address.getCity(), address.getCountry());
        return new UserDetailsResponse(user.getLastName(), user.getFirstName(), email, addressInfo, phone);
    }

    private String getPhoneNumber(List<Contact> contacts) {
        Contact phone = contacts.stream()
                .filter(c -> c.getType().equals(ContactType.PHONE))
                .findFirst()
                .orElse(null);
        if (phone == null) {
            return "";
        }
        return phone.getValue();
    }

    private String getEmailAddress(List<Contact> contacts) {
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

        return userDetails;
    }

    private Set<Address> getUserAddress(UserDetails userDetails, AddressInfo address) {
        Set<Address> addresses = new HashSet<>();
        Address userAddress = new Address();
        userAddress.setUser(userDetails);
        userAddress.setCity(address.city());
        userAddress.setCountry(address.country());
        userAddress.setStreet("");
        addresses.add(userAddress);

        return addresses;
    }

    private Set<Contact> getUserContacts(UserDetails userDetails, String email, String phone) {
        Set<Contact> contacts = new HashSet<>();
        contacts.add(getUserEmail(userDetails, email));
        contacts.add(getUserPhone(userDetails, phone));

        return contacts;
    }

    private Contact getUserEmail(UserDetails userDetails, String email) {
        Contact contact = new Contact();
        contact.setType(ContactType.EMAIL);
        contact.setValue(email);
        contact.setUser(userDetails);

        return contact;
    }

    private Contact getUserPhone(UserDetails userDetails, String phoneNumber) {
        Contact contact = new Contact();
        contact.setType(ContactType.PHONE);
        contact.setValue(phoneNumber);
        contact.setUser(userDetails);

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

    private void sendResetPasswordLink(String userId) {
        keycloakClient.getKeycloakRealmUsersResources()
                .get(userId)
                .executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));
    }

    private void sendVerificationPasswordLink(String userId) {
        UserResource user = keycloakClient.getKeycloakRealmUsersResources()
                .get(userId);
        if (Boolean.FALSE.equals(user.toRepresentation().isEmailVerified())) {
            user.sendVerifyEmail();
        }
    }
}
