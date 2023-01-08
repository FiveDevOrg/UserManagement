package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.entity.UserDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDetails, Integer> {
    @EntityGraph(attributePaths = {"addresses", "contacts"})
    Optional<UserDetails> findUserDetailsByUserName(String userName);

    @EntityGraph(attributePaths = {"addresses", "contacts"})
    Optional<UserDetails> findUserDetailsByAccountUuid(String uuid);

    @Query(value = "SELECT DISTINCT ON(product_id) user_id FROM bid ORDER BY product_id , price DESC", nativeQuery = true)
    List<Integer> getTopBidderIdForOffers();
}
