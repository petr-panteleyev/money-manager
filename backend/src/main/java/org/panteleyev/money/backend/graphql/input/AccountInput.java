/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.input;

import org.panteleyev.money.model.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountInput(
        String name,
        String comment,
        String accountNumber,
        BigDecimal openingBalance,
        BigDecimal accountLimit,
        BigDecimal currencyRate,
        UUID categoryUuid,
        UUID currencyUuid,
        boolean enabled,
        BigDecimal interest,
        LocalDate closingDate,
        UUID iconUuid,
        CardType cardType,
        String cardNumber,
        BigDecimal total,
        BigDecimal totalWaiting
) {
}
