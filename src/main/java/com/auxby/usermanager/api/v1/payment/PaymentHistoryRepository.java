package com.auxby.usermanager.api.v1.payment;

import com.auxby.usermanager.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Integer> {
    Optional<PaymentHistory> findByPaymentSecretAndAccountUuidAndStatus(String secret, String accountUuid, String status);
}
