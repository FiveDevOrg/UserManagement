package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import com.auxby.usermanager.utils.enums.ContactType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CONTACT")
@EqualsAndHashCode(callSuper = true)
public class Contact extends AuxbyBaseEntity {
    @Enumerated(EnumType.STRING)
    private ContactType type;
    private String value;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails user;
}
