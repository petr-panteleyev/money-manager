/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDto;
import org.panteleyev.money.backend.openapi.dto.DocumentFlatDto;

import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.newContactFlatDto;
import static org.panteleyev.money.backend.BaseTestUtils.newDocumentFlatDto;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.DOCUMENT_ROOT;

public class DocumentsApiTest extends BaseControllerTest {
    @Test
    public void test() {
        var created = System.currentTimeMillis();
        var uuid = UUID.randomUUID();

        var contact = newContactFlatDto(UUID.randomUUID(), null, created, created);
        put(contact.getUuid(), contact, ContactFlatDto.class, CONTACT_ROOT);


        var insert = newDocumentFlatDto(uuid, contact, created, created);
        insertAndCheck(insert, DocumentFlatDto.class, DocumentFlatDto[].class, DOCUMENT_ROOT, () -> uuid);

        var original = get(uuid, DocumentFlatDto.class, DOCUMENT_ROOT);
        var update = newDocumentFlatDto(uuid, contact, original.getCreated(), System.currentTimeMillis());
        updateAndCheck(update, DocumentFlatDto.class, DOCUMENT_ROOT, () -> uuid);
    }
}
