// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CardFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDTO;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDTO;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newAccountFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCardFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCategoryFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newContactFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCurrencyFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newTransactionFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CARD_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.TRANSACTION_ROOT;

public class TransactionsApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var debitedCategory = newCategoryFlatDto(UUID.randomUUID(), null, created, created);
        put(debitedCategory.getUuid(), debitedCategory, CategoryFlatDTO.class, CATEGORY_ROOT);
        var creditedCategory = newCategoryFlatDto(UUID.randomUUID(), null, created, created);
        put(creditedCategory.getUuid(), creditedCategory, CategoryFlatDTO.class, CATEGORY_ROOT);

        var currency = newCurrencyFlatDto(UUID.randomUUID(), created, created);
        put(currency.getUuid(), currency, CurrencyFlatDTO.class, CURRENCY_ROOT);

        var debitedAccount = newAccountFlatDto(UUID.randomUUID(), debitedCategory, currency, null, null,
                created, created);
        put(debitedAccount.getUuid(), debitedAccount, AccountFlatDTO.class, ACCOUNT_ROOT);
        var creditedAccount = newAccountFlatDto(UUID.randomUUID(), creditedCategory, currency, null, null,
                created, created);
        put(creditedAccount.getUuid(), creditedAccount, AccountFlatDTO.class, ACCOUNT_ROOT);

        var card = newCardFlatDto(UUID.randomUUID(), debitedAccount.getUuid(), created, created);
        put(card.getUuid(), card, CardFlatDTO.class, CARD_ROOT);

        var contact = newContactFlatDto(UUID.randomUUID(), null, created, created);
        put(contact.getUuid(), contact, ContactFlatDTO.class, CONTACT_ROOT);

        var insert = newTransactionFlatDto(uuid, debitedAccount, creditedAccount,
                null, null, created, created);
        insertAndCheck(insert, TransactionFlatDTO.class, TransactionFlatDTO[].class, TRANSACTION_ROOT, () -> uuid);

        var original = get(uuid, TransactionFlatDTO.class, TRANSACTION_ROOT);
        var update = newTransactionFlatDto(uuid, debitedAccount, creditedAccount,
                contact, card, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, TransactionFlatDTO.class, TRANSACTION_ROOT, () -> uuid);
    }
}
