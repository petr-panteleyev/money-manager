/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.testng.Assert.assertEquals;

public class TestCurrency extends ModelTestBase {

    @DataProvider
    @Override
    public Object[][] testBuildDataProvider() {
        UUID uuid = UUID.randomUUID();
        String symbol = BaseTestUtils.randomString();
        String description = BaseTestUtils.randomString();
        String formatSymbol = BaseTestUtils.randomString();
        int formatSymbolPosition = BaseTestUtils.randomInt();
        boolean showFormatSymbol = BaseTestUtils.randomBoolean();
        boolean def = BaseTestUtils.randomBoolean();
        BigDecimal rate = BaseTestUtils.randomBigDecimal();
        int direction = BaseTestUtils.randomInt();
        boolean useThousandSeparator = BaseTestUtils.randomBoolean();
        long created = System.currentTimeMillis();
        var modified = created + 1000;

        return new Object[][]{
            {
                new Currency.Builder()
                    .uuid(uuid)
                    .symbol(symbol)
                    .description(description)
                    .formatSymbol(formatSymbol)
                    .formatSymbolPosition(formatSymbolPosition)
                    .showFormatSymbol(showFormatSymbol)
                    .def(def)
                    .rate(rate)
                    .direction(direction)
                    .useThousandSeparator(useThousandSeparator)
                    .created(created)
                    .modified(modified)
                    .build(),
                new Currency(
                    uuid, symbol, description, formatSymbol, formatSymbolPosition,
                    showFormatSymbol, def, rate, direction, useThousandSeparator, created,
                    modified
                )
            },
            {
                new Currency(
                    uuid, symbol, null, null, formatSymbolPosition,
                    showFormatSymbol, def, null, direction, useThousandSeparator, created,
                    modified
                ),
                new Currency(
                    uuid, symbol, "", "", formatSymbolPosition,
                    showFormatSymbol, def, BigDecimal.ONE, direction, useThousandSeparator, created,
                    modified
                )
            }
        };
    }

    @Test
    public void testEquals() {
        var symbol = UUID.randomUUID().toString();
        var description = UUID.randomUUID().toString();
        var formatSymbol = UUID.randomUUID().toString();
        var formatSymbolPosition = BaseTestUtils.RANDOM.nextInt();
        var showFormatSymbol = BaseTestUtils.RANDOM.nextBoolean();
        var def = BaseTestUtils.RANDOM.nextBoolean();
        var rate = BaseTestUtils.randomBigDecimal();
        var direction = BaseTestUtils.RANDOM.nextInt();
        var useSeparator = BaseTestUtils.RANDOM.nextBoolean();
        var uuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        var c1 = new Currency.Builder()
            .symbol(symbol)
            .description(description)
            .formatSymbol(formatSymbol)
            .formatSymbolPosition(formatSymbolPosition)
            .showFormatSymbol(showFormatSymbol)
            .def(def)
            .rate(rate)
            .direction(direction)
            .useThousandSeparator(useSeparator)
            .uuid(uuid)
            .created(created)
            .modified(modified)
            .build();

        var c2 = new Currency.Builder()
            .symbol(symbol)
            .description(description)
            .formatSymbol(formatSymbol)
            .formatSymbolPosition(formatSymbolPosition)
            .showFormatSymbol(showFormatSymbol)
            .def(def)
            .rate(rate)
            .direction(direction)
            .useThousandSeparator(useSeparator)
            .uuid(uuid)
            .created(created)
            .modified(modified)
            .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        var original = BaseTestUtils.newCurrency();

        var copy = new Currency.Builder(original).build();
        assertEquals(copy, original);

        var manualCopy = new Currency.Builder()
            .symbol(original.symbol())
            .description(original.description())
            .formatSymbol(original.formatSymbol())
            .formatSymbolPosition(original.formatSymbolPosition())
            .showFormatSymbol(original.showFormatSymbol())
            .def(original.def())
            .rate(original.rate())
            .direction(original.direction())
            .useThousandSeparator(original.useThousandSeparator())
            .uuid(original.uuid())
            .created(original.created())
            .modified(original.modified())
            .build();
        assertEquals(manualCopy, original);
    }
}
