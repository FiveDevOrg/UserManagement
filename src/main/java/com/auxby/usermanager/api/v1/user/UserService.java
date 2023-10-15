package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.user.model.*;
import com.auxby.usermanager.entity.Address;
import com.auxby.usermanager.entity.Contact;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.entity.UserDevices;
import com.auxby.usermanager.utils.enums.ContactType;
import com.auxby.usermanager.utils.service.AmazonClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserDetailsRepository userDetailsRepository;
    private final AmazonClientService awsService;
    private final UserDevicesRepository devicesRepository;

    @Transactional
    public UserDetailsResponse createUser(UserDetailsInfo userInfo,
                                          Boolean isEmailVerified) {
        //TODO: call AUXBY-Platform
        return null;
    }

    public UserDetailsResponse getUser(String userUuid) {
        UserDetails userDetails = findUserDetails(userUuid);
        return mapToUserDetailsInfo(userDetails, userDetails.getContacts(), userDetails.getAddresses());
    }

    @Transactional
    public Boolean deleteUser(String userUuid) {
        UserDetails userDetails = findUserDetails(userUuid);
        // TODO notify Interrupted offer
        // TODO call AUXBY-Platform
        // notify all bidder
        deleteUserAwsResources(userUuid, userDetails);
        return true;
    }

    public Boolean checkUserExists(String userName) {
        Optional<UserDetails> userDetails = userDetailsRepository
                .findUserDetailsByUserName(userName);

        return userDetails.isPresent();
    }

    @Transactional
    public UserDetailsResponse updateUser(String userUuid, UpdateUserInfo userDetails) {
        UserDetails user = findUserDetails(userUuid);

        user.setLastName(userDetails.lastName());
        user.setFirstName(userDetails.firstName());

        var userAddresses = new ArrayList<>(user.getAddresses());
        var userContacts = new ArrayList<>(user.getContacts());
        userAddresses.forEach(user::removeAddress);
        userContacts.stream()
                .filter(c -> c.getType() == ContactType.PHONE)
                .forEach(user::removeContact);
        Set<Contact> contacts = getUserContacts(userDetails.phone());
        contacts.forEach(user::addContact);
        if (userDetails.address() != null) {
            Address addresses = getUserAddress(userDetails.address());
            user.addAddress(addresses);
        }

        return mapToUserDetailsInfo(user, user.getContacts(), user.getAddresses());
    }

    public Boolean isGoogleAccount(String email) {
        UserDetails userDetails = new UserDetails();
        userDetails.setIsGoogleAccount(false);
        UserDetails localUser = userDetailsRepository.findUserDetailsByUserName(email).orElse(userDetails);
        return localUser.getIsGoogleAccount();
    }

    @Transactional
    public UploadAvatarResponse updateUserAvatar(MultipartFile avatar, String userUuid) {
        try {
            String avatarUrl = awsService.uploadAvatar(avatar, userUuid);
            Optional<UserDetails> userDetails = userDetailsRepository.findUserDetailsByUserName(userUuid);
            userDetails.ifPresent(details -> details.setAvatarUrl(avatarUrl));

            return new UploadAvatarResponse(avatarUrl);
        } catch (IOException exception) {
            return new UploadAvatarResponse("unavailable");
        }
    }

    public Boolean changePassword(ChangePasswordDto changePasswordDto, String userUuid) {
        UserDetails userDetails = findUserDetails(userUuid);
        //TODO: call AUXBY-Platform
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

    @Transactional
    public void updateUserLastSeen(String username) {
        userDetailsRepository.updateUserLastSeen(username);
    }

    public UserDetails findUserDetails(String userUuid) {
        return userDetailsRepository.findUserDetailsByUserName(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
    }

    @Transactional
    public Boolean addDeviceToken(String userUuid, String deviceToken) {
        var userDevice = new UserDevices();
        userDevice.setUserId(findUserDetails(userUuid).getId());
        userDevice.setDeviceKey(deviceToken);
        devicesRepository.save(userDevice);
        return true;
    }

    private void deleteUserAwsResources(String userUuid, UserDetails userDetails) {
        awsService.deleteUserAvatar(userUuid);
        userDetails.getOffers()
                .forEach(
                        offer -> awsService.deleteOfferResources(userUuid, offer.getId())
                );
    }

    private UserDetailsResponse mapToUserDetailsInfo(UserDetails user, Set<Contact> contacts, Set<Address> addresses) {
        String phone = getPhoneNumber(contacts);
        String email = getEmailAddress(contacts);
        Optional<Address> address = addresses.stream()
                .findFirst();
        if (address.isEmpty()) {
            return new UserDetailsResponse(user.getLastName(), user.getFirstName(), email, null, phone, user.getAvatarUrl(), user.getAvailableCoins(), user.getIsGoogleAccount());

        }
        AddressInfo addressInfo = new AddressInfo(address.get().getCity(), address.get().getCountry());
        return new UserDetailsResponse(user.getLastName(), user.getFirstName(), email, addressInfo, phone, user.getAvatarUrl(), user.getAvailableCoins(), user.getIsGoogleAccount());
    }

    private String getPhoneNumber(Set<Contact> contacts) {
        Contact phone = contacts.stream()
                .filter(c -> c.getType() == ContactType.PHONE)
                .findFirst()
                .orElse(null);
        if (phone == null) {
            return "";
        }
        return phone.getValue();
    }

    private String getEmailAddress(Set<Contact> contacts) {
        Contact email = contacts.stream()
                .filter(c -> c.getType() == ContactType.EMAIL)
                .findFirst()
                .orElse(null);
        if (email == null) {
            return "";
        }
        return email.getValue();
    }

    private Address getUserAddress(AddressInfo address) {
        Address userAddress = new Address();
        userAddress.setCity(address.city());
        userAddress.setCountry(address.country());
        userAddress.setStreet("");

        return userAddress;
    }

    private Set<Contact> getUserContacts(String phone) {
        Set<Contact> contacts = new HashSet<>();
        contacts.add(getUserPhone(phone));

        return contacts;
    }

    private Contact getUserPhone(String phoneNumber) {
        Contact contact = new Contact();
        contact.setType(ContactType.PHONE);
        contact.setValue(phoneNumber);

        return contact;
    }

}
