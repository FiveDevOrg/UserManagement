package com.auxby.usermanager.api.v1.user;

import com.auxby.usermanager.entity.UserDevices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDevicesRepository extends JpaRepository<UserDevices, Integer> {
}
