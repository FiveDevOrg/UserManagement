package com.auxby.usermanager.api.v1.address;

import com.auxby.usermanager.entity.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;

    public List<Address> saveAddress(Set<Address> addresses) {
        return addressRepository.saveAll(addresses);
    }
}
