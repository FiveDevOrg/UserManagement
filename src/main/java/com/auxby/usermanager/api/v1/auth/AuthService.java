package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.address.model.AddressInfo;
import com.auxby.usermanager.api.v1.auth.model.*;
import com.auxby.usermanager.api.v1.user.UserService;
import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.exception.SignInException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import static com.auxby.usermanager.utils.constant.AppConstant.ANDROID_GOOGLE_CLIENT;
import static com.auxby.usermanager.utils.constant.AppConstant.IOS_GOOGLE_CLIENT;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final WebClient webClient;

    public AuthResponse login(AuthInfo authInfo) {
        //TODO: Call Auxby  Platform
        return null;
    }

    public boolean resetPassword(String email) {
        //TODO: Call Auxby-Platform
        return true;
    }

    public boolean resendVerificationLink(String email) {
        //TODO: Call Auxby-Platform
        return true;
    }

    public AuthResponse googleAuth(@Valid AuthGoogle authGoogle) {
        try {
            UserDetailsInfo googleUserDetails = getGoogleUserDetails(authGoogle);
            if (Boolean.FALSE.equals(userService.isGoogleAccount(googleUserDetails.email()))) {
                userService.createUser(googleUserDetails, true);
            }
            return login(new AuthInfo(googleUserDetails.email(), googleUserDetails.password()));
        } catch (Exception exception) {
            throw new SignInException(exception.getLocalizedMessage());
        }
    }

    private GoogleUserInfo getUserInfoByAccessToken(String accessToken) {
        try {
            return webClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(GoogleUserInfo.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new SignInException("Google failed: Invalid access token.");
        }
    }

    private UserDetailsInfo getUserDetailsByIdToken(String token) throws GeneralSecurityException, IOException {
        if (token.isEmpty()) {
            throw new SignInException("Google failed: Invalid ID token.");
        }

        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        List<String> clientIDs = Arrays.asList(
                IOS_GOOGLE_CLIENT,
                ANDROID_GOOGLE_CLIENT);
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(clientIDs)
                .build();

        GoogleIdToken idToken = GoogleIdToken.parse(verifier.getJsonFactory(), token);
        boolean tokenIsValid = verifier.verify(idToken);

        if (tokenIsValid) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userPwd = "Pwd." + payload.getSubject();
            String email = payload.getEmail();
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
            String userAvatar = (String) payload.get("picture");
            return new UserDetailsInfo(familyName, givenName, userPwd, email, new AddressInfo("", ""), "", userAvatar);
        } else {
            throw new SignInException("Google failed: Invalid ID token.");
        }
    }

    private UserDetailsInfo getGoogleUserDetails(@Valid AuthGoogle authGoogle) throws GeneralSecurityException, IOException {
        if (authGoogle.token() == null) {
            GoogleUserInfo googleUserInfo = getUserInfoByAccessToken(authGoogle.accessToken());
            String userPwd = "Pwd." + googleUserInfo.id();
            return new UserDetailsInfo(
                    googleUserInfo.family_name(),
                    googleUserInfo.given_name(),
                    userPwd,
                    googleUserInfo.email(),
                    new AddressInfo("", ""), "",
                    googleUserInfo.picture()
            );
        } else {
            return getUserDetailsByIdToken(authGoogle.token());
        }

    }
}
