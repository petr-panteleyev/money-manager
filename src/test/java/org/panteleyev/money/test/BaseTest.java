package org.panteleyev.money.test;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.mysqlapi.TableRecord;
import org.testng.Assert;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class BaseTest {

    protected static <T extends MoneyRecord> List<T> sortedById(Collection<T> list) {
        return list.stream()
            .sorted(Comparator.comparing(MoneyRecord::uuid))
            .toList();
    }

    protected static void assertEmpty(Collection c) {
        Assert.assertTrue(c.isEmpty());
    }

    protected static <T extends TableRecord> void assertRecords(Collection<T> c, TableRecord... records) {
        Assert.assertEquals(c.size(), records.length);
        Assert.assertTrue(c.containsAll(List.of(records)));
    }
}
