/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.input;

import org.panteleyev.money.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionInput(
        BigDecimal amount,
        int day,
        int month,
        int year,
        TransactionType type,
        String comment,
        boolean checked,
        UUID accountDebitedUuid,
        UUID accountCreditedUuid,
        UUID contactUuid,
        String contactName,
        BigDecimal rate,
        int rateDirection,
        String invoiceNumber,
        UUID parentUuid,
        boolean detailed,
        LocalDate statementDate
) {
}
