package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.entity.UserDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDetails, Integer> {
    @EntityGraph(attributePaths = {"addresses", "contacts"})
    Optional<UserDetails> findUserDetailsByUserName(String userName);
}
