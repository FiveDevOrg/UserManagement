package com.auxby.usermanager.api.v1.auth.model;

import javax.validation.constraints.NotBlank;


public record AuthGoogle(String token, String accessToken) {
}