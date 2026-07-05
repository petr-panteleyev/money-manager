// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.dto.CurrencyFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newCurrencyFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;

public class CurrenciesApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var insert = newCurrencyFlatDto(uuid, created, created);
        insertAndCheck(insert, CurrencyFlatDTO.class, CurrencyFlatDTO[].class, CURRENCY_ROOT, () -> uuid);

        var original = get(uuid, CurrencyFlatDTO.class, CURRENCY_ROOT);
        var update = newCurrencyFlatDto(uuid, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, CurrencyFlatDTO.class, CURRENCY_ROOT, () -> uuid);
    }
}
