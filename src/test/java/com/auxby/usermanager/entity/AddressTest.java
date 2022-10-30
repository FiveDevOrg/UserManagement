package com.auxby.usermanager.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void testEquals() {
        var address1 = new Address();
        var address2 = new Address();
        var address3 = new Address();

        address1.setId(1);
        address1.setCountry("Ro");
        address2.setId(2);
        address2.setCountry("UK");
        address3.setId(1);
        address3.setCountry("US");

        assertSame(address1.getId(), address3.getId());
        assertEquals(address1, address3);
        assertNotEquals(address1, address2);
        assertNotEquals(address1.getId(), address2.getId());
        assertTrue(address1.equals(address1));
        assertFalse(address1.equals(null));
        assertFalse(address1.equals(new Contact()));
        assertFalse(address1.equals(new Address()));
        assertFalse(new Address().equals(address1));
    }
}