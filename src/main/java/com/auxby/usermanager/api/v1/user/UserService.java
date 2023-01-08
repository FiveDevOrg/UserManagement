package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.user.model.*;
import com.auxby.usermanager.entity.*;
import com.auxby.usermanager.exception.ActionNotAllowException;
import com.auxby.usermanager.exception.ChangePasswordException;
import com.auxby.usermanager.exception.RegistrationException;
import com.auxby.usermanager.utils.enums.ContactType;
import com.auxby.usermanager.utils.service.AmazonClientService;
import com.auxby.usermanager.utils.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
    private final UserRepository userRepository;
    private final AmazonClientService awsService;
    private final KeycloakService keycloakService;

    @Transactional
    public UserDetailsResponse createUser(UserDetailsInfo userInfo) {
        try (Response response = keycloakService.performCreateUser(createUserRepresentation(userInfo))) {
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
                keycloakService.sendVerificationEmailLink(userDetails.getAccountUuid());

                return mapToUserDetailsInfo(newUser, newUser.getContacts(), newUser.getAddresses());
            } catch (Exception ex) {
                keycloakService.deleteKeycloakUser(userDetails.getAccountUuid());
                throw new RegistrationException("Something went wrong. User registration failed:" + ex.getMessage());
            }
        }
    }

    public UserDetailsResponse getUser(String userUuid) {
        UserDetails userDetails = findUserDetails(userUuid);
        return mapToUserDetailsInfo(userDetails, userDetails.getContacts(), userDetails.getAddresses());
    }

    @Transactional
    public void deleteUser(String userUuid) {
        UserDetails userDetails = findUserDetails(userUuid);
        List<Offer> onAuctionOffers = userDetails.getOffers()
                .stream()
                .filter(o -> o.isAvailable() && o.isOnAuction())
                .toList();
        var topBidders = userRepository.getTopBidderIdForOffers();
        if (!onAuctionOffers.isEmpty() || topBidders.contains(userDetails.getId())) {
            throw new ActionNotAllowException("Delete account not allow. User has active offers on auction or is top bidder for and offer.");
        }
        keycloakService.deleteKeycloakUser(userDetails.getAccountUuid());
        deleteUserAwsResources(userUuid, userDetails);
        userRepository.deleteById(userDetails.getId());
    }

    public Boolean checkUserExists(String userName) {
        Optional<UserDetails> userDetails = userRepository
                .findUserDetailsByUserName(userName);

        return userDetails.isPresent();
    }

    @Transactional
    public UserDetailsResponse updateUser(String userUuid, UpdateUserInfo userDetails) {
        UserDetails user = findUserDetails(userUuid);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setLastName(userDetails.lastName());
        userRepresentation.setFirstName(userDetails.firstName());

        keycloakService.performUserUpdate(user.getAccountUuid(), userRepresentation);
        user.setLastName(userDetails.lastName());
        user.setFirstName(userDetails.firstName());

        var userAddresses = new ArrayList<>(user.getAddresses());
        var userContacts = new ArrayList<>(user.getContacts());
        userAddresses.forEach(user::removeAddress);
        userContacts.stream().filter(c -> c.getType().equals(ContactType.PHONE))
                .forEach(user::removeContact);

        Set<Contact> contacts = getUserContacts(null, userDetails.phone());
        contacts.forEach(user::addContact);
        if (userDetails.address() != null) {
            Address addresses = getUserAddress(userDetails.address());
            user.addAddress(addresses);
        }

        return mapToUserDetailsInfo(user, user.getContacts(), user.getAddresses());
    }

    public boolean sendResetPasswordLink(String email) {
        UserDetails userDetails = findUser(email);
        keycloakService.sendResetPasswordLink(userDetails.getAccountUuid());

        return true;
    }

    public UserDetails findUser(String userName) {
        return userRepository.findUserDetailsByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Username %s not found.", userName)));
    }

    @Transactional
    public UploadAvatarResponse updateUserAvatar(MultipartFile avatar, String userUuid) {
        try {
            String avatarUrl = awsService.uploadAvatar(avatar, userUuid);
            Optional<UserDetails> userDetails = userRepository.findUserDetailsByAccountUuid(userUuid);
            userDetails.ifPresent(details -> details.setAvatarUrl(avatarUrl));

            return new UploadAvatarResponse(avatarUrl);
        } catch (IOException exception) {
            return new UploadAvatarResponse("unavailable");
        }
    }

    public Boolean changePassword(ChangePasswordDto changePasswordDto, String userUuid) {
        UserDetails userDetails = findUserDetails(userUuid);
        AuthInfo authInfo = new AuthInfo(userDetails.getUserName(), changePasswordDto.oldPassword());
        try {
            keycloakService.performLogin(authInfo);
        } catch (WebClientResponseException exception) {
            throw new ChangePasswordException(userDetails.getUserName());
        }
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setLastName(userDetails.getLastName());
        userRepresentation.setFirstName(userDetails.getFirstName());
        userRepresentation.setCredentials(Collections.singletonList(getCredentialRepresentation(changePasswordDto.newPassword())));
        keycloakService.performUserUpdate(userUuid, userRepresentation);

        return true;
    }

    @Transactional
    public void addUserResources(Integer coins, String userUuid) {
        UserDetails user = findUserDetails(userUuid);
        if (Objects.isNull(user.getAvailableCoins())) {
            user.setAvailableCoins(coins);
        } else {
            Integer totalCoins = coins + user.getAvailableCoins();
            user.setAvailableCoins(totalCoins);
        }
    }

    private void deleteUserAwsResources(String userUuid, UserDetails userDetails) {
        awsService.deleteUserAvatar(userUuid);
        userDetails.getOffers()
                .forEach(
                        offer -> awsService.deleteOfferResources(userUuid, offer.getId())
                );
    }

    private UserDetails findUserDetails(String userUuid) {
        return userRepository.findUserDetailsByAccountUuid(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
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
        var keycloakUser = keycloakService.getKeycloakUser(userInfo.email());
        if (keycloakUser.isEmpty()) {
            throw new RegistrationException("User not found.");
        }
        userDetails.setAccountUuid(keycloakUser.get().getId());
        keycloakService.addUserRole(keycloakUser.get().getId());
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
        if (email != null) {
            contacts.add(getUserEmail(email));
        }
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
}
