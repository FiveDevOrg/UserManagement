package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.api.v1.user.model.*;
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
@RequestMapping(AppConstant.BASE_V1_URL)
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDetailsResponse createUser(@Valid @RequestBody UserDetailsInfo detailsInfo) {
        log.info("POST - create new user");
        return userService.createUser(detailsInfo);
    }

    @GetMapping
    public UserDetailsResponse getUserInfo() {
        log.info("GET - get user.");
        return userService.getUser(SecurityContextUtil.getUserId());
    }

    @PutMapping()
    public UserDetailsResponse updateUser(@Valid @RequestBody UpdateUserInfo userDto) {
        log.info("PUT - update user profile.");
        return userService.updateUser(SecurityContextUtil.getUserId(), userDto);
    }

    @DeleteMapping
    public void deleteUser() {
        log.info("DELETE - delete user.");
        userService.deleteUser(SecurityContextUtil.getUserId());
    }

    @GetMapping("/email/check")
    public Boolean checkUserExists(@RequestParam String email) {
        log.info("POST - check user exists.");
        return userService.checkUserExists(email);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadAvatarResponse updateAvatar(@Parameter(description = "Avatar file.") @RequestPart MultipartFile file) {
        log.info("POST - update user avatar.");
        return userService.updateUserAvatar(file, SecurityContextUtil.getUserId());
    }

    @PutMapping(value = "/password")
    public Boolean changeUserPassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        log.info("POST - change user password.");
        return userService.changePassword(changePasswordDto, SecurityContextUtil.getUserId());
    }

    //TODO: New endpoint for user rating : { int : starts , comment: String}
    // return owner rating on offer details
}
