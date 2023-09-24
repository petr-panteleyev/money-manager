/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public record AccountDto(
        UUID uuid,
        String name,
        String comment,
        String accountNumber,
        BigDecimal openingBalance,
        BigDecimal accountLimit,
        BigDecimal currencyRate,
        CategoryDto category,
        Optional<CurrencyDto> currency,
        boolean enabled,
        BigDecimal interest,
        Optional<LocalDate> closingDate,
        Optional<IconDto> icon,
        BigDecimal total,
        BigDecimal totalWaiting,
        long created,
        long modified
) implements MoneyDto {
}
