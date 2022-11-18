/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Icon;
import org.springframework.stereotype.Component;

@Component
public class ContactResolver implements GraphQLResolver<Contact> {
    private final IconService iconService;

    public ContactResolver(IconService iconService) {
        this.iconService = iconService;
    }

    public Icon icon(Contact contact) {
        return contact.iconUuid() == null ?
                null : iconService.get(contact.iconUuid()).orElseThrow();
    }
}
