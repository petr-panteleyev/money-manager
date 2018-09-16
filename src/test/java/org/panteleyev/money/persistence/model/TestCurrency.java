/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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
import java.math.BigDecimal;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.BaseTestUtils.randomId;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newCurrency;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class TestCurrency extends BaseTest {
    @Test
    public void testEquals() {
        int id = randomId();
        String symbol = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String formatSymbol = UUID.randomUUID().toString();
        int formatSymbolPosition = RANDOM.nextInt();
        boolean showFormatSymbol = RANDOM.nextBoolean();
        boolean def = RANDOM.nextBoolean();
        BigDecimal rate = randomBigDecimal();
        int direction = RANDOM.nextInt();
        boolean useSeparator = RANDOM.nextBoolean();
        String uuid = UUID.randomUUID().toString();
        long modified = System.currentTimeMillis();

        Currency c1 = new Currency(id, symbol, description, formatSymbol,
                formatSymbolPosition, showFormatSymbol,
                def, rate, direction, useSeparator,
                uuid, modified
        );

        Currency c2 = new Currency(id, symbol, description, formatSymbol,
                formatSymbolPosition, showFormatSymbol,
                def, rate, direction, useSeparator,
                uuid, modified
        );

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        Currency currency = newCurrency();

        int newId = randomId();
        Currency copy = currency.copy(newId);

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
}
