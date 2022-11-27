/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.controller;

import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.ContactInput;
import org.panteleyev.money.backend.service.ContactService;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Icon;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class ContactGraphQLController {
    private final IconService iconService;
    private final ContactService contactService;

    public ContactGraphQLController(
            IconService iconService,
            ContactService contactService
    ) {
        this.iconService = iconService;
        this.contactService = contactService;
    }

    @QueryMapping
    public List<Contact> contacts() {
        return contactService.getAll();
    }

    @QueryMapping
    public Contact contact(@Argument UUID uuid) {
        return contactService.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Contact", uuid));
    }

    @MutationMapping
    public Contact createContact(@Argument ContactInput input) {
        var contact = new Contact.Builder()
                .name(input.name())
                .type(input.type())
                .comment(input.comment())
                .phone(input.phone())
                .mobile(input.mobile())
                .email(input.email())
                .web(input.web())
                .street(input.street())
                .city(input.city())
                .country(input.country())
                .zip(input.zip())
                .iconUuid(input.iconUuid())
                .build();
        return contactService.put(contact)
                .orElseThrow(() -> new GraphQLCreateException("Contact"));
    }

    @MutationMapping
    public Contact updateContact(
            @Argument UUID uuid,
            @Argument ContactInput input
    ) {
        var builder = contactService.get(uuid)
                .map(Contact.Builder::new)
                .orElseThrow();

        builder.name(input.name())
                .type(input.type())
                .comment(input.comment())
                .phone(input.phone())
                .mobile(input.mobile())
                .email(input.email())
                .web(input.web())
                .street(input.street())
                .city(input.city())
                .country(input.country())
                .zip(input.zip())
                .iconUuid(input.iconUuid())
                .modified(System.currentTimeMillis());
        return contactService.put(builder.build())
                .orElseThrow(() -> new GraphQLUpdateException("Contact", uuid));
    }

    @SchemaMapping
    public Icon icon(Contact contact) {
        return contact.iconUuid() == null ?
                null : iconService.get(contact.iconUuid()).orElseThrow();
    }
}
