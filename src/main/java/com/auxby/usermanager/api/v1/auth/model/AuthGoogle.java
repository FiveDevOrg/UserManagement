package com.auxby.usermanager.api.v1.auth.model;

import javax.validation.constraints.NotBlank;


public record AuthGoogle(@NotBlank(message = "Token is mandatory.") String token) {
}