package com.auxby.usermanager.entity;

import com.auxby.usermanager.utils.enums.ContactType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsTest {

    @Test
    void addContact() {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(1L);
        Contact contact = new Contact();
        contact.setType(ContactType.PHONE);
        contact.setValue("0740400200");
        contact.setId(2L);

        userDetails.addContact(contact);

        assertEquals(1, userDetails.getContacts().size());
        assertTrue(userDetails.getContacts().contains(contact));
        assertEquals(contact.getUser().getId(), userDetails.getId());
    }

    @Test
    void removeContact() {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(1L);
        Contact contact = new Contact();
        contact.setType(ContactType.PHONE);
        contact.setValue("0740400200");
        contact.setId(2L);

        userDetails.addContact(contact);
        assertEquals(1, userDetails.getContacts().size());
        userDetails.removeContact(contact);
        assertEquals(0, userDetails.getContacts().size());
        assertNull(contact.getUser());
    }

    @Test
    void addAddress() {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(1L);
        Address address = new Address();
        address.setCountry("Ro");
        address.setCity("SV");
        address.setId(2L);

        userDetails.addAddress(address);

        assertEquals(1, userDetails.getAddresses().size());
        assertTrue(userDetails.getAddresses().contains(address));
        assertEquals(address.getUser().getId(), userDetails.getId());
    }

    @Test
    void removeAddress() {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(1L);
        Address address = new Address();
        address.setCountry("Ro");
        address.setCity("SV");
        address.setId(2L);

        userDetails.addAddress(address);
        assertEquals(1, userDetails.getAddresses().size());
        userDetails.removeAddress(address);
        assertEquals(0, userDetails.getAddresses().size());
        assertNull(address.getUser());
    }
}