// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.dto.ExchangeSecuritySplitFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newExchangeSecurityFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newExchangeSecuritySplitFlatDTO;
import static org.panteleyev.money.backend.WebmoneyApplication.EXCHANGE_SECURITY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.EXCHANGE_SECURITY_SPLIT_ROOT;

public class ExchangeSecuritySplitsApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();

        var security = newExchangeSecurityFlatDto(UUID.randomUUID(), created, created);
        put(security.getUuid(), security, ExchangeSecurityFlatDTO.class, EXCHANGE_SECURITY_ROOT);

        var uuid = UUID.randomUUID();

        var insert = newExchangeSecuritySplitFlatDTO(uuid, security.getUuid(), created, created);
        insertAndCheck(insert, ExchangeSecuritySplitFlatDTO.class, ExchangeSecuritySplitFlatDTO[].class,
                EXCHANGE_SECURITY_SPLIT_ROOT, () -> uuid);

        var original = get(uuid, ExchangeSecuritySplitFlatDTO.class, EXCHANGE_SECURITY_SPLIT_ROOT);
        var update = newExchangeSecuritySplitFlatDTO(uuid, security.getUuid(),
                original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, ExchangeSecuritySplitFlatDTO.class, EXCHANGE_SECURITY_SPLIT_ROOT, () -> uuid);
    }
}
