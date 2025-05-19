/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.ContactsApiDelegate;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDto;
import org.panteleyev.money.backend.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class ContactsApiImpl implements ContactsApiDelegate {
    private final ContactService service;

    public ContactsApiImpl(ContactService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<ContactFlatDto>> getContacts() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<ContactFlatDto> getContactByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<ContactFlatDto> putContact(ContactFlatDto currency) {
        return ResponseEntity.ok(service.put(currency));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getContactsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
