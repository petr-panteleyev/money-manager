// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.dto.IconFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_EURO;
import static org.panteleyev.money.backend.BaseTestUtils.newIconFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;

public class IconsApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var insert = newIconFlatDto(uuid, ICON_DOLLAR, created, created);
        insertAndCheck(insert, IconFlatDTO.class, IconFlatDTO[].class, ICON_ROOT, () -> uuid);

        var original = get(uuid, IconFlatDTO.class, ICON_ROOT);
        var update = newIconFlatDto(uuid, ICON_EURO, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, IconFlatDTO.class, ICON_ROOT, () -> uuid);
    }
}
