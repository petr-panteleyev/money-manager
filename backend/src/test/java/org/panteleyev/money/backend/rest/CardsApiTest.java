/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDto;
import org.panteleyev.money.backend.openapi.dto.CardFlatDto;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDto;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDto;
import org.panteleyev.money.backend.openapi.dto.IconFlatDto;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.newAccountFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCardFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCategoryFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCurrencyFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newIconFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CARD_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;

public class CardsApiTest extends BaseControllerTest {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var icon = newIconFlatDto(UUID.randomUUID(), ICON_DOLLAR, created, created);
        put(icon.getUuid(), icon, IconFlatDto.class, ICON_ROOT);
        var category = newCategoryFlatDto(UUID.randomUUID(), icon.getUuid(), created, created);
        put(category.getUuid(), category, CategoryFlatDto.class, CATEGORY_ROOT);
        var currency = newCurrencyFlatDto(UUID.randomUUID(), created, created);
        put(currency.getUuid(), currency, CurrencyFlatDto.class, CURRENCY_ROOT);
        var account = newAccountFlatDto(uuid, category, currency, icon, created, created);
        put(account.getUuid(), account, AccountFlatDto.class, ACCOUNT_ROOT);

        var insert = newCardFlatDto(uuid, account.getUuid(), created, created);
        insertAndCheck(insert, CardFlatDto.class, CardFlatDto[].class, CARD_ROOT, () -> uuid);

        var original = get(uuid, CardFlatDto.class, CARD_ROOT);
        var update = newCardFlatDto(uuid, account.getUuid(), original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, CardFlatDto.class, CARD_ROOT, () -> uuid);
    }
}
