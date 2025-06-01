/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import org.panteleyev.money.model.ContactType;

import java.util.UUID;

public record ContactDto(
        UUID uuid,
        String name,
        ContactType type,
        String comment,
        String phone,
        String mobile,
        String email,
        String web,
        String street,
        String city,
        String country,
        String zip,
        IconDto icon,
        long created,
        long modified
) implements MoneyDto {
}
