package com.auxby.usermanager.entity;

import com.auxby.usermanager.entity.base.AuxbyBaseEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "payments_history")
@SequenceGenerator(name = "seq_generator", sequenceName = "payments_history_id_seq", allocationSize = 1)
public class PaymentHistory extends AuxbyBaseEntity {
    private String status;
    private String accountUuid;
    private String paymentSecret;
    private Date intentDate;
}
