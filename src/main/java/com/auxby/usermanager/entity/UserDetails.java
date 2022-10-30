package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "USER_DETAILS")
@NamedEntityGraph(name = "user-contacts-graph",
        attributeNodes = @NamedAttributeNode(value = "contacts"))
@NamedEntityGraph(name = "user-addresses-graph",
        attributeNodes = @NamedAttributeNode(value = "addresses"))
@SequenceGenerator(name = "seq_generator", sequenceName = "user_details_id_seq", allocationSize = 1)
public class UserDetails extends AuxbyBaseEntity {
    private String gender;
    private String lastName;
    private String firstName;
    private String accountUuid;
    private String userName;
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "owner_id")
    private List<Offer> offers = new ArrayList<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Contact> contacts = new HashSet<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Address> addresses = new HashSet<>();

    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setUser(this);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setUser(null);
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }
}
