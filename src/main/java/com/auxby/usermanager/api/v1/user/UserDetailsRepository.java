package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.entity.UserDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {
    @EntityGraph(attributePaths = {"addresses", "contacts"})
    Optional<UserDetails> findUserDetailsByUserName(String userName);

    @Modifying
    @Query(value = "update user_details set last_seen = current_timestamp " +
            "where username = :username", nativeQuery = true)
    void updateUserLastSeen(String username);
}
