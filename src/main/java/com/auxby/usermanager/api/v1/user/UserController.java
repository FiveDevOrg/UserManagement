package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
import com.auxby.usermanager.utils.constant.AppConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = AppConstant.BASE_V1_URL)
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDetailsResponse createUser(@Valid @RequestBody UserDetailsInfo detailsInfo) {
        log.info("POST - create new user");
        return userService.createUser(detailsInfo);
    }

    @GetMapping
    public UserDetailsResponse getUserInfo(@RequestParam String email) {
        log.info("GET - get user.");
        return userService.getUser(email);
    }

    @DeleteMapping
    public void deleteUser(@RequestParam String email) {
        log.info("DELETE - delete user.");
        userService.deleteUser(email);
    }
}
