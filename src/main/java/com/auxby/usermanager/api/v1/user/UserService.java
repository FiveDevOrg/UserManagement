package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.address.AddressService;
import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.contact.ContactService;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.entity.Address;
import com.auxby.usermanager.entity.Contact;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.utils.enums.ContactType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String UNKNOWN = "Unknown";
    private final UserRepository userRepository;
    private final ContactService contactService;
    private final AddressService addressService;

    @Transactional
    public UserDetailsInfo createUser(UserDetailsInfo userInfo) {
        UserDetails userDetails = mapToUserDetails(userInfo);
        UserDetails newUser = userRepository.save(userDetails);
        List<Contact> contacts = contactService.saveContacts(getUserContacts(newUser, userInfo.email(), userInfo.phone()));
        List<Address> addresses = new ArrayList<>();
        if (userInfo.address() != null) {
            addresses.addAll(addressService.saveAddress(getUserAddress(newUser, userInfo.address())));
        }

        return mapToUserDetailsInfo(newUser, contacts, addresses);
    }

    public UserDetailsInfo getUser(String userName) {
        UserDetails userDetails = userRepository.findUserDetailsByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Username %s not found.", userName)));
        return mapToUserDetailsInfo(userDetails, userDetails.getContacts().stream().toList(), userDetails.getAddresses().stream().toList());
    }

    public void deleteUser(String userName) {
        UserDetails userDetails = userRepository.findUserDetailsByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Username %s not found.", userName)));
        userRepository.delete(userDetails);
    }

    private UserDetailsInfo mapToUserDetailsInfo(UserDetails user, List<Contact> contacts, List<Address> addresses) {
        String phone = getPhoneNumber(contacts);
        String email = getEmailAddress(contacts);
        Address address = addresses.stream()
                .findFirst()
                .orElse(null);
        if (address == null) {
            return new UserDetailsInfo(user.getLastName(), user.getFirstName(), user.getUserName(), email, null, phone);

        }
        AddressInfo addressInfo = new AddressInfo(address.getCity(), address.getCounty(), address.getStreet());
        return new UserDetailsInfo(user.getLastName(), user.getFirstName(), user.getUserName(), email, addressInfo, phone);
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
        userDetails.setUserName(userInfo.userName());
        //TODO: update with Keycloak uuid - after Keycloak save it's performed
        userDetails.setAccountUuid(UUID.randomUUID().toString());

        return userDetails;
    }

    private Set<Address> getUserAddress(UserDetails userDetails, AddressInfo address) {
        Set<Address> addresses = new HashSet<>();
        Address userAddress = new Address();
        userAddress.setUser(userDetails);
        userAddress.setCity(address.city());
        userAddress.setCounty(address.county());
        userAddress.setStreet(address.street());
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
}
