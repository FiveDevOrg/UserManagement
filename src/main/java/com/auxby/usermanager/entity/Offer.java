package com.auxby.usermanager.entity;


import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "OFFER")
@EqualsAndHashCode(callSuper = true)
@SequenceGenerator(name = "seq_generator", sequenceName = "product_id_seq", allocationSize = 1)
public class Offer extends AuxbyBaseEntity {
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "offer_id")
    private Set<Contact> contacts = new HashSet<>();
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "offer_id")
    private Set<Address> addresses = new HashSet<>();
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "offer_id")
    private Set<OfferData> offerDetails = new HashSet<>();
}
