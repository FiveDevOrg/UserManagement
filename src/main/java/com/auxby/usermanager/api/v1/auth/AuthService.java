package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidatedException;
import com.auxby.usermanager.utils.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final KeycloakService keycloakService;

    public AuthResponse login(AuthInfo authInfo) {
        verifyUserValidateEmailAddress(authInfo.email());
        try {
            KeycloakAuthResponse response = keycloakService.performLogin(authInfo);
            return new AuthResponse(response.access_token());
        } catch (WebClientResponseException | NullPointerException exception) {
            throw new SignInException("Login user " + authInfo.email() + " failed.");
        }
    }

    public boolean resetPassword(String email) {
        return userService.sendResetPasswordLink(email);
    }

    public boolean resendVerificationLink(String email) {
        UserDetails user = userService.findUser(email);
        keycloakService.sendVerificationEmailLink(user.getAccountUuid());

        return true;
    }

    private void verifyUserValidateEmailAddress(String email) {
        UserDetails user = userService.findUser(email);
        UserRepresentation userRepresentation = keycloakService.getUserRepresentation(user.getAccountUuid());
        if (Boolean.FALSE.equals(userRepresentation.isEmailVerified())) {
            throw new UserEmailNotValidatedException(email);
        }
    }
}
