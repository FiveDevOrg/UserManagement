package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "USER_DETAILS")
@NamedEntityGraph(name = "user-contacts-graph",
        attributeNodes = @NamedAttributeNode(value = "contacts"))
@NamedEntityGraph(name = "user-addresses-graph",
        attributeNodes = @NamedAttributeNode(value = "addresses"))
public class UserDetails extends AuxbyBaseEntity {
    private String gender;
    private String lastName;
    private String firstName;
    private String accountUuid;
    private String userName;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Contact> contacts = new HashSet<>();
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "user",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private Set<Address> addresses = new HashSet<>();
}
