package com.auxby.usermanager.api.v1.user.model;

import javax.validation.constraints.NotBlank;

public record ChangePasswordDto(@NotBlank(message = "The old password must be set.") String oldPassword,
                                @NotBlank(message = "The new password must be set.") String newPassword) {
}
