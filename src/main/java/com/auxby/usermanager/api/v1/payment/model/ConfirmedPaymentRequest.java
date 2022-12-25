package com.auxby.usermanager.api.v1.payment.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record ConfirmedPaymentRequest(@NotBlank String clientSecret,
                                      @NotNull Integer coins) {
}
