/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.model.Contact;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;

@Controller
@CrossOrigin
@RequestMapping(CONTACT_ROOT)
public class ContactController {
    private final ContactRepository contactRepository;

    public ContactController(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Contact>> getContacts() {
        return ResponseEntity.ok(contactRepository.getAll());
    }

    @GetMapping(
        value = "/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Contact> getContact(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.of(contactRepository.get(uuid));
    }

    @PutMapping(
        value = "/{uuid}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Contact> putContact(@PathVariable("uuid") UUID uuid, @RequestBody Contact contact) {
        if (!uuid.equals(contact.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        var rows = 0;
        if (contactRepository.get(contact.uuid()).isEmpty()) {
            rows = contactRepository.insert(contact);
        } else {
            rows = contactRepository.update(contact);
        }
        return rows == 1 ? ResponseEntity.ok(contact) : ResponseEntity.internalServerError().build();
    }
}
