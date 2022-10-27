package com.auxby.usermanager.api.v1.auth.model;

import javax.validation.constraints.NotBlank;

public record AuthInfo(@NotBlank(message = "UserName is mandatory.") String email,
                       @NotBlank(message = "Password is mandatory.") String password) {
}
