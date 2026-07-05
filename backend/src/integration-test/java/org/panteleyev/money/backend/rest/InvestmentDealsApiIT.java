// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.dto.AccountFlatDTO;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.panteleyev.money.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newAccountFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCategoryFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newCurrencyFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newExchangeSecurityFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newInvestmentDealFlatDTO;
import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.EXCHANGE_SECURITY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.INVESTMENT_DEAL_ROOT;

public class InvestmentDealsApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();

        var category = newCategoryFlatDto(UUID.randomUUID(), null, created, created);
        put(category.getUuid(), category, CategoryFlatDTO.class, CATEGORY_ROOT);

        var currency = newCurrencyFlatDto(UUID.randomUUID(), created, created);
        put(currency.getUuid(), currency, CurrencyFlatDTO.class, CURRENCY_ROOT);

        var security = newExchangeSecurityFlatDto(UUID.randomUUID(), created, created);
        put(security.getUuid(), security, ExchangeSecurityFlatDTO.class, EXCHANGE_SECURITY_ROOT);

        var account = newAccountFlatDto(UUID.randomUUID(), category, currency, security, null, created, created);
        put(account.getUuid(), account, AccountFlatDTO.class, ACCOUNT_ROOT);

        var uuid = UUID.randomUUID();

        var insert = newInvestmentDealFlatDTO(uuid, account.getUuid(), currency.getUuid(), security.getUuid(),
                created, created);
        insertAndCheck(insert, InvestmentDealFlatDTO.class, InvestmentDealFlatDTO[].class,
                INVESTMENT_DEAL_ROOT, () -> uuid);

        var original = get(uuid, InvestmentDealFlatDTO.class, INVESTMENT_DEAL_ROOT);
        var update = newInvestmentDealFlatDTO(uuid, account.getUuid(), currency.getUuid(), security.getUuid(),
                original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, InvestmentDealFlatDTO.class, INVESTMENT_DEAL_ROOT, () -> uuid);
    }
}
