// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDTO;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.backend.openapi.dto.IconFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.newAccountFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCategoryFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCurrencyFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newExchangeSecurityFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newIconFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.EXCHANGE_SECURITY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;

public class AccountsApiIT extends BaseControllerIT {
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
        var security = newExchangeSecurityFlatDto(UUID.randomUUID(), created, created);
        put(security.getUuid(), security, ExchangeSecurityFlatDTO.class, EXCHANGE_SECURITY_ROOT);

        var insert = newAccountFlatDto(uuid, category, currency, security, icon, created, created);
        insertAndCheck(insert, AccountFlatDTO.class, AccountFlatDTO[].class, ACCOUNT_ROOT, () -> uuid);

        var original = get(uuid, AccountFlatDTO.class, ACCOUNT_ROOT);
        var update = newAccountFlatDto(uuid, category, null, null, null,
                original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, AccountFlatDTO.class, ACCOUNT_ROOT, () -> uuid);
    }
}
