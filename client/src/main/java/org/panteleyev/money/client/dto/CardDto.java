/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import org.panteleyev.money.model.CardType;

import java.time.LocalDate;
import java.util.UUID;

public record CardDto(
        UUID uuid,
        AccountDto account,
        CardType type,
        String number,
        LocalDate expiration,
        String comment,
        boolean enabled,
        long created,
        long modified
) {
}
