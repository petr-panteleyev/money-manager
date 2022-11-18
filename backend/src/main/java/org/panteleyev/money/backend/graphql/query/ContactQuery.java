/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.service.ContactService;
import org.panteleyev.money.model.Contact;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ContactQuery implements GraphQLQueryResolver {
    private final ContactService service;

    public ContactQuery(ContactService service) {
        this.service = service;
    }

    public List<Contact> contacts() {
        return service.getAll();
    }

    public Contact contact(UUID uuid) {
        return service.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Contact", uuid));
    }
}
