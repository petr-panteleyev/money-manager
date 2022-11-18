/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import java.util.UUID;

public sealed interface MoneyDto permits
        IconDto, CategoryDto, CurrencyDto, ContactDto, AccountDto, TransactionDto, TransactionModificationResponseDto {
    UUID uuid();

    long created();

    long modified();
}
