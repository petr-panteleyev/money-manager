/*
 Copyright (C) 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.test;

import org.panteleyev.money.model.MoneyRecord;
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

    protected static <T extends MoneyRecord> void assertRecords(Collection<T> c, MoneyRecord... records) {
        Assert.assertEquals(c.size(), records.length);
        Assert.assertTrue(c.containsAll(List.of(records)));
    }
}
