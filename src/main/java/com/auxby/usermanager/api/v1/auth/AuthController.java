package com.auxby.usermanager.api.v1.auth;

import com.auxby.usermanager.api.v1.auth.model.AuthGoogle;
import com.auxby.usermanager.api.v1.auth.model.AuthInfo;
import com.auxby.usermanager.api.v1.auth.model.AuthResponse;
import com.auxby.usermanager.utils.constant.AppConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = AppConstant.BASE_V1_URL)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthInfo authInfo) {
        log.info("Login user.");
        return authService.login(authInfo);
    }

    @PostMapping("/googleAuth")
    public Boolean googleAuth(@Valid @RequestBody AuthGoogle authGoogle) {
        log.info("POST - googleAuth");
        return authService.googleAuth(authGoogle);
    }

    @PostMapping("/reset")
    public Boolean resetPassword(@RequestParam String email) {
        log.info("Reset password");
       return authService.resetPassword(email);
    }

    @PostMapping("/resend-verification-link")
    public Boolean resendVerificationLink(@RequestParam String email) {
        log.info("Resend verification link.");
        return  authService.resendVerificationLink(email);
    }
}
