/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.input;

import org.panteleyev.money.model.ContactType;

import java.util.UUID;

public record ContactInput(
        String name,
        ContactType type,
        String phone,
        String mobile,
        String email,
        String web,
        String comment,
        String street,
        String city,
        String country,
        String zip,
        UUID iconUuid
) {
}
