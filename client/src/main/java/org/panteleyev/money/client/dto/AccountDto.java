/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        CurrencyDto currency,
        boolean enabled,
        BigDecimal interest,
        LocalDate closingDate,
        IconDto icon,
        BigDecimal total,
        BigDecimal totalWaiting,
        long created,
        long modified
) implements MoneyDto {
}
