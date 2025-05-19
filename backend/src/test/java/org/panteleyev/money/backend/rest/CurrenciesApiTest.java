/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDto;
import org.panteleyev.money.client.dto.CurrencyDto;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newCurrencyFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;

public class CurrenciesApiTest extends BaseControllerTest {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var insert = newCurrencyFlatDto(uuid, created, created);
        insertAndCheck(insert, CurrencyFlatDto.class, CurrencyFlatDto[].class, CURRENCY_ROOT, () -> uuid);

        var original = get(uuid, CurrencyDto.class, CURRENCY_ROOT);
        var update = newCurrencyFlatDto(uuid, original.created(), System.currentTimeMillis());
        updateAndCheck(update, CurrencyFlatDto.class, CURRENCY_ROOT, () -> uuid);
    }
}
