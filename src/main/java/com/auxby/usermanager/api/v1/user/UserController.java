package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
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
    public UserDetailsInfo createUser(@Valid @RequestBody UserDetailsInfo detailsInfo) {
        log.info("POST - create new user");
        return userService.createUser(detailsInfo);
    }

    @GetMapping
    public UserDetailsInfo getUserInfo(@RequestParam String userName) {
        log.info("GET - get user.");
        return userService.getUser(userName);
    }

    @DeleteMapping
    public void deleteUser(@RequestParam String userName) {
        log.info("DELETE - delete user.");
        userService.deleteUser(userName);
    }
}
