package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public AuthResponse login(AuthInfo authInfo) {
        //TODO: use Keycloak to authenticate and get user token
        UserDetailsInfo userDetailsInfo = userService.getUser(authInfo.userName());
        return new AuthResponse(String.format("%s-%s", userDetailsInfo.userName(), UUID.randomUUID()));
    }
}
