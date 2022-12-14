package com.auxby.usermanager.api.v1.user.model;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static com.auxby.usermanager.utils.constant.AppConstant.VALID_PHONE_REGEX;

public record UpdateUserInfo(@NotBlank(message = "Last name is mandatory.") String lastName,
                             @NotBlank(message = "First name is mandatory.") String firstName,
                             AddressInfo address,
                             @NotBlank(message = "Phone number is mandatory.")
                             @Pattern(regexp = VALID_PHONE_REGEX, message = "Invalid phone number.")
                             String phone) {
}
