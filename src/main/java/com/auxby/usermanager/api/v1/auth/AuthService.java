package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthGoogle;
import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.api.v1.auth.model.KeycloakAuthResponse;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
import com.auxby.usermanager.config.properties.KeycloakProps;
import com.auxby.usermanager.entity.UserDetails;
import com.auxby.usermanager.exception.SignInException;
import com.auxby.usermanager.exception.UserEmailNotValidatedException;
import com.auxby.usermanager.utils.service.KeycloakService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import static com.auxby.usermanager.utils.constant.AppConstant.ANDROID_GOOGLE_CLIENT;
import static com.auxby.usermanager.utils.constant.AppConstant.IOS_GOOGLE_CLIENT;
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

    public AuthResponse googleAuth(@Valid AuthGoogle authGoogle) {
        try {
            UserDetailsInfo googleUserDetails = getGoogleUserDetails(authGoogle);
            registerGoogleUser(googleUserDetails);
            return login(new AuthInfo(googleUserDetails.email(), googleUserDetails.password()));
        } catch (Exception exception) {
            throw new SignInException(exception.getLocalizedMessage());
        }
    }

    private UserDetailsResponse registerGoogleUser(UserDetailsInfo userDetailsInfo) {
        return userService.createUser(userDetailsInfo, true);
    }

    private UserDetailsInfo getGoogleUserDetails(@Valid AuthGoogle authGoogle) throws GeneralSecurityException, IOException {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        List<String> clientIDs = Arrays.asList(
                IOS_GOOGLE_CLIENT,
                ANDROID_GOOGLE_CLIENT);
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(clientIDs)
                .build();

        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), authGoogle.token());
        boolean tokenIsValid = verifier.verify(idToken);

        if (tokenIsValid) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userPwd = "Pwd." + payload.getSubject();
            String email = payload.getEmail();
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            return new UserDetailsInfo(familyName, givenName, userPwd, email, new AddressInfo("", ""), "");
        } else {
            throw new SignInException("Google failed: Invalid ID token.");
        }
    }

    private void verifyUserValidateEmailAddress(String email) {
        UserDetails user = userService.findUser(email);
        UserRepresentation userRepresentation = keycloakService.getUserRepresentation(user.getAccountUuid());
        if (Boolean.FALSE.equals(userRepresentation.isEmailVerified())) {
            throw new UserEmailNotValidatedException(email);
        }
    }
}
