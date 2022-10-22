package com.auxby.usermanager.api.v1.auth.model;

import javax.validation.constraints.NotBlank;

public record AuthInfo(@NotBlank(message = "UserName is mandatory.") String userName,
                       @NotBlank(message = "Password is mandatory.") String password) {
}
