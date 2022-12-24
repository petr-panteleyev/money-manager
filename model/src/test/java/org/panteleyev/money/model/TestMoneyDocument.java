/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.model.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.model.BaseTestUtils.randomDocumentType;
import static org.panteleyev.money.model.BaseTestUtils.randomInt;
import static org.panteleyev.money.model.BaseTestUtils.randomString;

public class TestMoneyDocument {
    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var ownerUuid = UUID.randomUUID();
        var contactUuid = UUID.randomUUID();
        var documentType = randomDocumentType();
        var fileName = randomString();
        var date = LocalDate.now();
        var mimeType = randomString();
        var size = randomInt();
        var compressed = randomBoolean();
        var description = randomString();
        long created = System.currentTimeMillis();
        long modified = created + 1000L;

        return List.of(
                Arguments.of(
                        new MoneyDocument.Builder()
                                .uuid(uuid)
                                .ownerUuid(ownerUuid)
                                .contactUuid(contactUuid)
                                .documentType(documentType)
                                .fileName(fileName)
                                .date(date)
                                .size(size)
                                .compressed(compressed)
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
                                compressed,
                                mimeType,
                                description,
                                created,
                                modified
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(MoneyRecord actual, MoneyRecord expected) {
        assertEquals(expected, actual);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }
}
