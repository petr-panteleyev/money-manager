// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.ContactsV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDTO;
import org.panteleyev.money.backend.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class ContactsV1ApiImpl implements ContactsV1ApiDelegate {
    private final ContactService service;

    public ContactsV1ApiImpl(ContactService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<ContactFlatDTO>> getContacts() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<ContactFlatDTO> getContactByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<ContactFlatDTO> putContact(ContactFlatDTO currency) {
        return ResponseEntity.ok(service.put(currency));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getContactsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
