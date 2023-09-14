/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import org.panteleyev.money.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public record TransactionDto(
        UUID uuid,
        BigDecimal amount,
        BigDecimal creditAmount,
        int day,
        int month,
        int year,
        TransactionType type,
        String comment,
        boolean checked,
        AccountDto accountDebited,
        AccountDto accountCredited,
        Optional<ContactDto> contact,
        String invoiceNumber,
        Optional<TransactionDto> parent,
        boolean detailed,
        LocalDate statementDate,
        long created,
        long modified
) implements MoneyDto {
}
