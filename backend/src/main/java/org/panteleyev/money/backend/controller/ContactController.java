/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.service.ContactService;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;
import static org.panteleyev.money.backend.controller.JsonUtil.writeStreamAsJsonArray;

@Tag(name = "Contacts")
@Controller
@CrossOrigin
@RequestMapping(CONTACT_ROOT)
public class ContactController {
    private final ContactRepository repository;
    private final ContactService service;
    private final ObjectMapper objectMapper;

    public ContactController(ContactRepository repository, ContactService service, ObjectMapper objectMapper) {
        this.repository = repository;
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Get all contacts")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Contact>> getContacts() {
        return ResponseEntity.ok(repository.getAll());
    }

    @Operation(summary = "Get contact")
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Contact> getContact(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Operation(summary = "Insert or update contact")
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Contact> putContact(@PathVariable("uuid") UUID uuid, @RequestBody Contact contact) {
        if (!uuid.equals(contact.uuid())) {
            return ResponseEntity.badRequest().build();
        } else {
            return service.put(contact)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.internalServerError().build());
        }
    }

    @Operation(summary = "Get all contacts as stream")
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        StreamingResponseBody body = (OutputStream out) -> {
            try (var stream = repository.getStream()) {
                writeStreamAsJsonArray(objectMapper, stream, out);
            }
        };
        return ResponseEntity.accepted().body(body);
    }
}
