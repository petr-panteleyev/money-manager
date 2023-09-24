/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.model.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.model.BaseTestUtils.randomCardType;
import static org.panteleyev.money.model.BaseTestUtils.randomString;

public class TestCard {
    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var accountUuid = UUID.randomUUID();
        var type = randomCardType();
        var number = randomString();
        var expiration = LocalDate.now();
        var comment = randomString();
        var enabled = randomBoolean();
        var created = System.currentTimeMillis();
        var modified = created + 1000;

        return List.of(
                Arguments.of(
                        new Card.Builder()
                                .uuid(uuid)
                                .accountUuid(accountUuid)
                                .type(type)
                                .number(number)
                                .expiration(expiration)
                                .comment(comment)
                                .enabled(enabled)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new Card(uuid, accountUuid, type, number, expiration, comment, enabled,
                                created, modified)
                ),
                Arguments.of(
                        new Card(uuid, accountUuid, type, number, expiration, "", enabled,
                                created, modified),
                        new Card(uuid, accountUuid, type, number, expiration, null, enabled,
                                created, modified)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(Card actual, Card expected) {
        assertEquals(expected, actual);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }

    @Test
    public void testEquals() {
        var uuid = UUID.randomUUID();
        var accountUuid = UUID.randomUUID();
        var type = randomCardType();
        var number = randomString();
        var expiration = LocalDate.now();
        var comment = randomString();
        var enabled = randomBoolean();
        var created = System.currentTimeMillis();
        var modified = created + 1000;

        var c1 = new Card.Builder()
                .uuid(uuid)
                .accountUuid(accountUuid)
                .type(type)
                .number(number)
                .expiration(expiration)
                .comment(comment)
                .enabled(enabled)
                .created(created)
                .modified(modified)
                .build();
        var c2 = new Card.Builder()
                .uuid(uuid)
                .accountUuid(accountUuid)
                .type(type)
                .number(number)
                .expiration(expiration)
                .comment(comment)
                .enabled(enabled)
                .created(created)
                .modified(modified)
                .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        var original = new Card.Builder()
                .uuid(UUID.randomUUID())
                .accountUuid(UUID.randomUUID())
                .type(randomCardType())
                .number(randomString())
                .expiration(LocalDate.now())
                .comment(randomString())
                .enabled(randomBoolean())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis() + 1000)
                .build();

        var copy = new Card.Builder(original).build();
        assertEquals(copy, original);

        var manualCopy = new Card.Builder()
                .uuid(original.uuid())
                .accountUuid(original.accountUuid())
                .type(original.type())
                .number(original.number())
                .expiration(original.expiration())
                .comment(original.comment())
                .enabled(original.enabled())
                .created(original.created())
                .modified(original.modified())
                .build();
        assertEquals(original, manualCopy);
    }

    @Test
    public void testNegativeBuilder() {
        assertThrows(IllegalStateException.class, () -> new Card.Builder().build());
    }
}
