package com.auxby.usermanager.api.v1.payment.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Amount must be set.") @Min(value = 0, message = "Amount must positive.") Double amount,
        @NotNull(message = "Payment type must be set.") String paymentType,
        @NotBlank(message = "Currency must be set.") String currency
) {
}
