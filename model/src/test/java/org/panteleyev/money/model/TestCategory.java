// Copyright © 2017-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.dto.CategoryType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.model.BaseTestUtils.randomEnum;
import static org.panteleyev.money.model.BaseTestUtils.randomString;

public class TestCategory {

    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var name = randomString();
        var comment = randomString();
        var type = randomEnum(CategoryType.class);
        var iconUuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = created + 1000;

        return List.of(
                Arguments.of(
                        new Category.Builder()
                                .uuid(uuid)
                                .name(name)
                                .comment(comment)
                                .type(type)
                                .iconUuid(iconUuid)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new Category(uuid, name, comment, type, iconUuid, created, modified)
                ),
                Arguments.of(
                        new Category(uuid, name, null, type, iconUuid, created, modified),
                        new Category(uuid, name, "", type, iconUuid, created, modified)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(Category actual, Category expected) {
        assertEquals(expected, actual);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }

    @Test
    public void testEquals() {
        var name = randomString();
        var comment = randomString();
        var type = randomEnum(CategoryType.class);
        var iconUuid = UUID.randomUUID();
        var uuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        var c1 = new Category.Builder()
                .name(name)
                .comment(comment)
                .type(type)
                .iconUuid(iconUuid)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();
        var c2 = new Category.Builder()
                .name(name)
                .comment(comment)
                .type(type)
                .iconUuid(iconUuid)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        var original = new Category.Builder()
                .name(randomString())
                .comment(randomString())
                .type(randomEnum(CategoryType.class))
                .iconUuid(UUID.randomUUID())
                .uuid(UUID.randomUUID())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();

        var copy = new Category.Builder(original).build();
        assertEquals(copy, original);

        var manualCopy = new Category.Builder()
                .name(original.name())
                .comment(original.comment())
                .type(original.type())
                .iconUuid(original.iconUuid())
                .uuid(original.uuid())
                .created(original.created())
                .modified(original.modified())
                .build();
        assertEquals(original, manualCopy);
    }

    @Test
    public void testNegativeBuilder() {
        assertThrows(IllegalStateException.class, () -> new Category.Builder().build());
    }
}
