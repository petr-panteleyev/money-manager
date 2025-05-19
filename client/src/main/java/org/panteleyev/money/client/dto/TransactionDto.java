/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import org.panteleyev.money.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDto(
        UUID uuid,
        BigDecimal amount,
        BigDecimal creditAmount,
        LocalDate transactionDate,
        TransactionType type,
        String comment,
        boolean checked,
        AccountDto accountDebited,
        AccountDto accountCredited,
        ContactDto contact,
        String invoiceNumber,
        TransactionDto parent,
        boolean detailed,
        LocalDate statementDate,
        CardDto card,
        long created,
        long modified
) implements MoneyDto {
}
