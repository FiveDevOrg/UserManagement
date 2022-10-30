package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import com.auxby.usermanager.utils.enums.ContactType;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "CONTACT")
@SequenceGenerator(name = "seq_generator", sequenceName = "contact_id_seq", allocationSize = 1)
public class Contact extends AuxbyBaseEntity {
    @Enumerated(EnumType.STRING)
    private ContactType type;
    private String value;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;

        return getId() != null && getId().equals(((Contact) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
