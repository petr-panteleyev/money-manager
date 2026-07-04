// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDTO;
import org.panteleyev.money.backend.openapi.dto.IconFlatDTO;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.newCategoryFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newIconFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;

public class CategoriesApiIT extends BaseControllerIT {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var icon = newIconFlatDto(UUID.randomUUID(), ICON_DOLLAR, created, created);
        put(icon.getUuid(), icon, IconFlatDTO.class, ICON_ROOT);

        var insert = newCategoryFlatDto(uuid, icon.getUuid(), created, created);
        insertAndCheck(insert, CategoryFlatDTO.class, CategoryFlatDTO[].class, CATEGORY_ROOT, () -> uuid);

        var original = get(uuid, CategoryFlatDTO.class, CATEGORY_ROOT);
        var update = newCategoryFlatDto(uuid, null, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, CategoryFlatDTO.class, CATEGORY_ROOT, () -> uuid);
    }
}
