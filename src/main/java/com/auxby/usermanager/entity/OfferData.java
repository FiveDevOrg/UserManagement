package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "OFFER_DATA")
@SequenceGenerator(name = "seq_generator", sequenceName = "product_data_id_seq", allocationSize = 1)
public class OfferData extends AuxbyBaseEntity {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfferData)) return false;

        return getId() != null && getId().equals(((OfferData) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
