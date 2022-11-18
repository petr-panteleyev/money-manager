/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.ContactInput;
import org.panteleyev.money.backend.service.ContactService;
import org.panteleyev.money.model.Contact;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContactMutation implements GraphQLMutationResolver {
    private final ContactService service;

    public ContactMutation(ContactService service) {
        this.service = service;
    }

    public Contact createContact(ContactInput input) {
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
        return service.put(contact)
                .orElseThrow(() -> new GraphQLCreateException("Contact"));
    }

    public Contact updateContact(UUID uuid, ContactInput input) {
        var builder = service.get(uuid)
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
        return service.put(builder.build())
                .orElseThrow(() -> new GraphQLUpdateException("Contact", uuid));
    }
}
