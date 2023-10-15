package com.auxby.usermanager.config.security;


import com.auxby.usermanager.api.v1.user.repository.User;
import com.auxby.usermanager.api.v1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSecurityService {

    private final UserRepository repository;

    public User getUserByEmail(String email) {
        return repository.findUserByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with email %s not found.", email)));
    }
}
