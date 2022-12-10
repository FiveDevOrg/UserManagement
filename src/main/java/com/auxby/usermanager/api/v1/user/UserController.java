package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.UserDetailsInfo;
import com.auxby.usermanager.api.v1.user.model.UserDetailsResponse;
import com.auxby.usermanager.utils.SecurityContextUtil;
import com.auxby.usermanager.utils.constant.AppConstant;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("{email}")
    public void updateUser(@PathVariable("email") String email,
                           @Valid @RequestBody UserDetailsInfo userDto) {
        log.info("PUT - update user profile.");
        userService.updateUser(email, userDto);
    }

    @DeleteMapping
    public void deleteUser(@RequestParam String email) {
        log.info("DELETE - delete user.");
        userService.deleteUser(email);
    }

    @GetMapping("/email/check")
    public Boolean checkUserExists(@RequestParam String email) {
        log.info("POST - check user exists.");
        return userService.checkUserExists(email);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateAvatar(@Parameter(description = "Avatar file.") @RequestPart MultipartFile file) {
        log.info("PUT - update user avatar.");
        return userService.updateUserAvatar(file, SecurityContextUtil.getUserId());
    }
}
