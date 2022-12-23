package com.auxby.usermanager.payment.model;

import com.auxby.usermanager.utils.enums.Currency;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record PaymentRequest(@NotNull @Min(value = 0, message = "Amount must set.") Double amount,
                             @NotNull Currency currency,
                             @NotBlank(message = "Stipe token must be provided.") String paymentMethod) {
}
