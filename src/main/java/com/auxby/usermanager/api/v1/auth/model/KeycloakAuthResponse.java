package com.auxby.usermanager.api.v1.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakAuthResponse(String access_token,
                                   Long expires_in,
                                   Long refresh_expires_in,
                                   String refresh_token,
                                   String token_type,
                                   String session_state) {
}
