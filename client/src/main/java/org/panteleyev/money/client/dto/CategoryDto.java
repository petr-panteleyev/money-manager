/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import org.panteleyev.money.model.CategoryType;

import java.util.Optional;
import java.util.UUID;

public record CategoryDto(
        UUID uuid,
        String name,
        String comment,
        CategoryType type,
        Optional<IconDto> icon,
        long created,
        long modified
) implements MoneyDto {
}
