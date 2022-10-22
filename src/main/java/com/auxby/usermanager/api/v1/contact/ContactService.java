package com.auxby.usermanager.api.v1.contact;

import com.auxby.usermanager.entity.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public List<Contact> saveContacts(Set<Contact> contacts) {
        return contactRepository.saveAll(contacts);
    }
}
