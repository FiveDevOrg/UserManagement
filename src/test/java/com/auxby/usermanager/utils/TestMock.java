package com.auxby.usermanager.utils;

import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.entity.UserDetails;

public class TestMock {
    public static KeycloakAuthResponse mockKeycloakAuthResponse() {
        return new KeycloakAuthResponse("access_token",
                10L,
                10L,
                "refresh_token",
                "bearer",
                "unknown_state");
    }

    public static UserDetails mockUserDetails() {
        UserDetails mockUserDetails = new UserDetails();
        mockUserDetails.setAccountUuid("uuid-test-acc");
        mockUserDetails.setUserName("testAcc@gmail.com");
        mockUserDetails.setFirstName("Joe");
        mockUserDetails.setLastName("Doe");

        return mockUserDetails;
    }
}
