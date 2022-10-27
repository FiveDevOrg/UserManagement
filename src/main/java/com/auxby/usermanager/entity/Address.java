package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ADDRESS")
@EqualsAndHashCode(callSuper = true)
public class Address extends AuxbyBaseEntity {
    private String city;
    @Column(name = "country")
    private String country;
    private String street;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails user;
}
