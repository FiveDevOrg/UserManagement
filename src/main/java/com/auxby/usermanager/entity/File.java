package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "FILES")
@SequenceGenerator(name = "seq_generator", sequenceName = "files_id_seq", allocationSize = 1)
public class File extends AuxbyBaseEntity {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof File)) return false;

        return getId() != null && getId().equals(((File) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
