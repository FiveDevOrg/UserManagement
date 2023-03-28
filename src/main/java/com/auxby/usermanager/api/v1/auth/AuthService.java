package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthGoogle;
import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.config.properties.KeycloakProps;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidatedException;
import com.auxby.usermanager.utils.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.validation.Valid;

import static org.keycloak.OAuth2Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final KeycloakService keycloakService;

    private final WebClient webClient;
    private final KeycloakProps keycloakProps;

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

    // TODO should enable token exchange for this method
    public Boolean googleAuthTokenExchange(@Valid AuthGoogle authGoogle) {
        try {
            KeycloakAuthResponse response = webClient.post()
                    .uri(keycloakProps.getAuthUrl())
                    .body(BodyInserters.fromFormData(SUBJECT_TOKEN, authGoogle.token())
                            .with(CLIENT_SECRET, keycloakProps.getClientSecret())
                            .with(CLIENT_ID, keycloakProps.getClientId())
                            .with(GRANT_TYPE, TOKEN_EXCHANGE_GRANT_TYPE)
                            .with(SUBJECT_TOKEN_TYPE, JWT_TOKEN_TYPE)
                            .with(SUBJECT_ISSUER, "google")
                    )
                    .retrieve()
                    .bodyToFlux(KeycloakAuthResponse.class)
                    .blockFirst();
            assert response != null;
            log.info("Google Auth " + response.access_token());
            return true;
        } catch (WebClientResponseException exception) {
            throw new SignInException("Google auth token exchange failed.");
        }
    }

    public Boolean googleAuth(@Valid AuthGoogle authGoogle) {
//        NetHttpTransport transport = new NetHttpTransport();
//        JsonFactory jsonFactory = new GsonFactory();
//
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                .setAudience(Collections.singletonList("156615882044-rdmjaosndk9ovbsno56imkkomgr799bq.apps.googleusercontent.com"))
//                .build();
//
//        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), googleToken.token());
//        boolean tokenIsValid = verifier.verify(idToken);
//
//        if (tokenIsValid) {
//            GoogleIdToken.Payload payload = idToken.getPayload();
//
//            // Print user identifier
//            String userId = payload.getSubject();
//            System.out.println("User ID: " + userId);
//
//            // Get profile information from payload
//            String email = payload.getEmail();
//            boolean emailVerified = payload.getEmailVerified();
//            String name = (String) payload.get("name");
//            String pictureUrl = (String) payload.get("picture");
//            String locale = (String) payload.get("locale");
//            String familyName = (String) payload.get("family_name");
//            String givenName = (String) payload.get("given_name");
//            System.out.println(" email: " + email);
//            System.out.println(" givenName: " + givenName);
//        } else {
//            System.out.println("Invalid ID token.");
//        }
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
