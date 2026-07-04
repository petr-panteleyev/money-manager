// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecurityFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newExchangeSecurityFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.EXCHANGE_SECURITY_ROOT;

public class ExchangeSecuritiesApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var insert = newExchangeSecurityFlatDto(uuid, created, created);
        insertAndCheck(insert, ExchangeSecurityFlatDTO.class, ExchangeSecurityFlatDTO[].class,
                EXCHANGE_SECURITY_ROOT, () -> uuid);

        var original = get(uuid, ExchangeSecurityFlatDTO.class, EXCHANGE_SECURITY_ROOT);
        var update = newExchangeSecurityFlatDto(uuid, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, ExchangeSecurityFlatDTO.class, EXCHANGE_SECURITY_ROOT, () -> uuid);
    }
}
