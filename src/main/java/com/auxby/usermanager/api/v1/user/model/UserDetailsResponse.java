package com.auxby.usermanager.api.v1.user.model;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.auxby.usermanager.utils.constant.AppConstant.VALID_EMAIL_REGEX;
import static com.auxby.usermanager.utils.constant.AppConstant.VALID_PHONE_REGEX;

public record UserDetailsResponse(@NotBlank(message = "Last name is mandatory.") String lastName,
                                  @NotBlank(message = "First name is mandatory.") String firstName,
                                  @NotBlank(message = "Email address is mandatory.")
                                  @Pattern(regexp = VALID_EMAIL_REGEX, message = "Invalid email.") String email,
                                  AddressInfo address,
                                  @NotBlank(message = "Phone number is mandatory.")
                                  @Pattern(regexp = VALID_PHONE_REGEX, message = "Invalid phone number.")
                                  String phone,
                                  String avatar,
                                  Integer availableCoins,
                                  Boolean isGoogleAccount
) {
}
