/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.model.BaseTestUtils.randomString;

public class TestCurrency {

    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var type = CurrencyType.SECURITY;
        var symbol = randomString();
        var description = randomString();
        var formatSymbol = randomString();
        int formatSymbolPosition = BaseTestUtils.randomInt();
        var showFormatSymbol = BaseTestUtils.randomBoolean();
        var def = BaseTestUtils.randomBoolean();
        var rate = BaseTestUtils.randomBigDecimal();
        var direction = BaseTestUtils.randomInt();
        var useThousandSeparator = BaseTestUtils.randomBoolean();
        var isin = randomString();
        var registry = randomString();
        var created = System.currentTimeMillis();
        var modified = created + 1000;

        return List.of(
                Arguments.of(
                        new Currency.Builder()
                                .uuid(uuid)
                                .type(CurrencyType.SECURITY)
                                .symbol(symbol)
                                .description(description)
                                .formatSymbol(formatSymbol)
                                .formatSymbolPosition(formatSymbolPosition)
                                .showFormatSymbol(showFormatSymbol)
                                .def(def)
                                .rate(rate)
                                .direction(direction)
                                .useThousandSeparator(useThousandSeparator)
                                .isin(isin)
                                .registry(registry)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new Currency(
                                uuid, type, symbol, description, formatSymbol, formatSymbolPosition,
                                showFormatSymbol, def, rate, direction, useThousandSeparator, isin, registry,
                                created, modified
                        )
                ),
                Arguments.of(
                        new Currency(
                                uuid, type, symbol, null, null, formatSymbolPosition,
                                showFormatSymbol, def, null, direction, useThousandSeparator, isin, registry,
                                created, modified
                        ),
                        new Currency(
                                uuid, type, symbol, "", "", formatSymbolPosition,
                                showFormatSymbol, def, BigDecimal.ONE, direction, useThousandSeparator, isin, registry,
                                created, modified
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(Currency actual, Currency expected) {
        assertEquals(actual, expected);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
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
        assertEquals(original, copy);

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
        assertEquals(original, manualCopy);
    }
}
