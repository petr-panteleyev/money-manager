/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.panteleyev.money.model.BaseTestUtils.randomDocumentType;
import static org.panteleyev.money.model.BaseTestUtils.randomInt;
import static org.panteleyev.money.model.BaseTestUtils.randomString;

@Test
public class TestMoneyDocument extends ModelTestBase {
    @DataProvider
    @Override
    public Object[][] testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var ownerUuid = UUID.randomUUID();
        var contactUuid = UUID.randomUUID();
        var documentType = randomDocumentType();
        var fileName = randomString();
        var date = LocalDate.now();
        var mimeType = randomString();
        var size = randomInt();
        var description = randomString();
        long created = System.currentTimeMillis();
        long modified = created + 1000L;

        return new Object[][]{
                {
                        new MoneyDocument.Builder()
                                .uuid(uuid)
                                .ownerUuid(ownerUuid)
                                .contactUuid(contactUuid)
                                .documentType(documentType)
                                .fileName(fileName)
                                .date(date)
                                .size(size)
                                .mimeType(mimeType)
                                .description(description)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new MoneyDocument(
                                uuid,
                                ownerUuid,
                                contactUuid,
                                documentType,
                                fileName,
                                date,
                                size,
                                mimeType,
                                description,
                                created,
                                modified
                        )
                }
        };
    }
}
