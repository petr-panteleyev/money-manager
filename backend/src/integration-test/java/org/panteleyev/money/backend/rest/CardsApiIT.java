// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CardFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDTO;
import org.panteleyev.money.backend.openapi.dto.IconFlatDTO;

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

public class CardsApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var icon = newIconFlatDto(UUID.randomUUID(), ICON_DOLLAR, created, created);
        put(icon.getUuid(), icon, IconFlatDTO.class, ICON_ROOT);
        var category = newCategoryFlatDto(UUID.randomUUID(), icon.getUuid(), created, created);
        put(category.getUuid(), category, CategoryFlatDTO.class, CATEGORY_ROOT);
        var currency = newCurrencyFlatDto(UUID.randomUUID(), created, created);
        put(currency.getUuid(), currency, CurrencyFlatDTO.class, CURRENCY_ROOT);
        var account = newAccountFlatDto(uuid, category, currency, null, icon, created, created);
        put(account.getUuid(), account, AccountFlatDTO.class, ACCOUNT_ROOT);

        var insert = newCardFlatDto(uuid, account.getUuid(), created, created);
        insertAndCheck(insert, CardFlatDTO.class, CardFlatDTO[].class, CARD_ROOT, () -> uuid);

        var original = get(uuid, CardFlatDTO.class, CARD_ROOT);
        var update = newCardFlatDto(uuid, account.getUuid(), original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, CardFlatDTO.class, CARD_ROOT, () -> uuid);
    }
}
