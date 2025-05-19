/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDto;
import org.panteleyev.money.backend.openapi.dto.IconFlatDto;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.newContactFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newIconFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;

public class ContactsApiTest extends BaseControllerTest {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var icon = newIconFlatDto(UUID.randomUUID(), ICON_DOLLAR, created, created);
        put(icon.getUuid(), icon, IconFlatDto.class, ICON_ROOT);

        var insert = newContactFlatDto(uuid, icon.getUuid(), created, created);
        insertAndCheck(insert, ContactFlatDto.class, ContactFlatDto[].class, CONTACT_ROOT, () -> uuid);

        var original = get(uuid, ContactFlatDto.class, CONTACT_ROOT);
        var update = newContactFlatDto(uuid, null, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, ContactFlatDto.class, CONTACT_ROOT, () -> uuid);
    }
}
