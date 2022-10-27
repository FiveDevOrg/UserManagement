package com.auxby.usermanager.api.v1.auth;

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

    @PostMapping("/reset")
    public void resetPassword(@RequestParam String email) {
        log.info("Reset password");
        authService.resetPassword(email);
    }

    @PostMapping("/resend-verification-link")
    public void resendVerificationLink(@RequestParam String email) {
        log.info("Resend verification link.");
        authService.resendVerificationLink(email);
    }
}
