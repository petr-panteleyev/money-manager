package org.panteleyev.money.model;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.test.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;

public class TestCategory extends BaseTest {
    @Test
    public void testEquals() {
        var name = UUID.randomUUID().toString();
        var comment = UUID.randomUUID().toString();
        var type = randomCategoryType();
        var iconUuid = UUID.randomUUID();
        var uuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        var c1 = new Category.Builder()
            .name(name)
            .comment(comment)
            .type(type)
            .iconUuid(iconUuid)
            .guid(uuid)
            .created(created)
            .modified(modified)
            .build();
        var c2 = new Category.Builder()
            .name(name)
            .comment(comment)
            .type(type)
            .iconUuid(iconUuid)
            .guid(uuid)
            .created(created)
            .modified(modified)
            .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testBuilder() {
        var original = new Category.Builder()
            .name(randomString())
            .comment(randomString())
            .type(randomCategoryType())
            .iconUuid(UUID.randomUUID())
            .guid(UUID.randomUUID())
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
            .guid(original.uuid())
            .created(original.created())
            .modified(original.modified())
            .build();
        assertEquals(manualCopy, original);
    }
}
