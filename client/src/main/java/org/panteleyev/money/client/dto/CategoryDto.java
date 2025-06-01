/*
 Copyright © 2022-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import org.panteleyev.money.model.CategoryType;

import java.util.UUID;

public record CategoryDto(
        UUID uuid,
        String name,
        String comment,
        CategoryType type,
        IconDto icon,
        long created,
        long modified
) implements MoneyDto {
}
