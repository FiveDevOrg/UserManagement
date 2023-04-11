package com.auxby.usermanager.api.v1.auth.model;

public record GoogleUserInfo(
        String id,
        String email,
        String name,
        String given_name,
        String family_name,
        String picture
) {
}