/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Currency;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.UUID;

public class TestCurrency extends BaseTest {
    @Test
    public void testEquals() throws Exception {
        int id = randomId();
        String symbol = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String formatSymbol = UUID.randomUUID().toString();
        int formatSymbolPosition = BaseTest.RANDOM.nextInt();
        boolean showFormatSymbol = BaseTest.RANDOM.nextBoolean();
        boolean def = BaseTest.RANDOM.nextBoolean();
        BigDecimal rate = randomBigDecimal();
        int direction = BaseTest.RANDOM.nextInt();
        boolean useSeparator = BaseTest.RANDOM.nextBoolean();
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

        Assert.assertEquals(c1, c2);
        Assert.assertEquals(c1.hashCode(), c2.hashCode());
    }
}
