package org.panteleyev.money.model;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;
import static org.panteleyev.money.test.BaseTestUtils.newCurrency;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.testng.Assert.assertEquals;

public class TestCurrency extends BaseTest {
    @Test
    public void testEquals() {
        var symbol = UUID.randomUUID().toString();
        var description = UUID.randomUUID().toString();
        var formatSymbol = UUID.randomUUID().toString();
        var formatSymbolPosition = RANDOM.nextInt();
        var showFormatSymbol = RANDOM.nextBoolean();
        var def = RANDOM.nextBoolean();
        var rate = randomBigDecimal();
        var direction = RANDOM.nextInt();
        var useSeparator = RANDOM.nextBoolean();
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
            .guid(uuid)
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
            .guid(uuid)
            .created(created)
            .modified(modified)
            .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testBuilder() {
        var original = newCurrency();

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
            .guid(original.uuid())
            .created(original.created())
            .modified(original.modified())
            .build();
        assertEquals(manualCopy, original);
    }
}
