package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "USER_DEVICES")
@SequenceGenerator(name = "seq_generator", sequenceName = "user_details_id_seq", allocationSize = 1)
public class UserDevices extends AuxbyBaseEntity {

    private String deviceKey;
    private Integer userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDevices)) return false;

        return getId() != null && getId().equals(((UserDevices) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
