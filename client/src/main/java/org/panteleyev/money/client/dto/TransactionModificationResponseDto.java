/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import java.util.List;
import java.util.UUID;

public record TransactionModificationResponseDto(
        TransactionDto transaction,
        ContactDto contact,
        List<AccountDto> accounts
) implements MoneyDto {
    @Override
    public UUID uuid() {
        return transaction.uuid();
    }

    @Override
    public long created() {
        return transaction.created();
    }

    @Override
    public long modified() {
        return transaction.modified();
    }
}
