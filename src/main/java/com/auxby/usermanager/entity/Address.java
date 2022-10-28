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
    @Column(name = "county")
    private String country;
    private String street;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserDetails user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;

        return getId() != null && getId().equals(((Address) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
