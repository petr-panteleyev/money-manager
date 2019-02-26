/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.persistence.model;

import org.panteleyev.money.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.BaseTestUtils.newCurrency;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.BaseTestUtils.randomId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class TestCurrency extends BaseTest {
    @Test
    public void testEquals() {
        var id = randomId();
        var symbol = UUID.randomUUID().toString();
        var description = UUID.randomUUID().toString();
        var formatSymbol = UUID.randomUUID().toString();
        var formatSymbolPosition = RANDOM.nextInt();
        var showFormatSymbol = RANDOM.nextBoolean();
        var def = RANDOM.nextBoolean();
        var rate = randomBigDecimal();
        var direction = RANDOM.nextInt();
        var useSeparator = RANDOM.nextBoolean();
        var uuid = UUID.randomUUID().toString();
        var modified = System.currentTimeMillis();

        var c1 = new Currency.Builder(id)
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
            .modified(modified)
            .build();

        var c2 = new Currency.Builder(id)
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
            .modified(modified)
            .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        var currency = newCurrency();

        var newId = randomId();
        var copy = currency.copy(newId);

        assertNotEquals(currency, copy);

        assertNotEquals(currency.getId(), copy.getId());
        assertEquals(currency.getSymbol(), copy.getSymbol());
        assertEquals(currency.getDescription(), copy.getDescription());
        assertEquals(currency.getDef(), copy.getDef());
        assertEquals(currency.getFormatSymbol(), copy.getFormatSymbol());
        assertEquals(currency.getDirection(), copy.getDirection());
        assertEquals(currency.getFormatSymbolPosition(), copy.getFormatSymbolPosition());
        assertEquals(currency.getGuid(), copy.getGuid());
        assertEquals(currency.getModified(), copy.getModified());
        assertEquals(currency.getRate(), copy.getRate());
    }

    @Test
    public void testBuilder() {
        var original = newCurrency();

        var copy = new Currency.Builder(original).build();
        assertEquals(copy, original);

        var manualCopy = new Currency.Builder()
            .id(original.getId())
            .symbol(original.getSymbol())
            .description(original.getDescription())
            .formatSymbol(original.getFormatSymbol())
            .formatSymbolPosition(original.getFormatSymbolPosition())
            .showFormatSymbol(original.getShowFormatSymbol())
            .def(original.getDef())
            .rate(original.getRate())
            .direction(original.getDirection())
            .useThousandSeparator(original.getUseThousandSeparator())
            .guid(original.getGuid())
            .modified(original.getModified())
            .build();
        assertEquals(manualCopy, original);
    }
}
