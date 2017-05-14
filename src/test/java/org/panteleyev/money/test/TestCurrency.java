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
    public void testBuilder() throws Exception {
        Currency original = newCurrency();

        Currency.Builder builder = new Currency.Builder(original);
        Currency newCurrency = builder.build();
        Assert.assertEquals(newCurrency, original);
        Assert.assertEquals(newCurrency.hashCode(), original.hashCode());

        Currency.Builder emptyBuilder = new Currency.Builder()
                .id(original.getId())
                .symbol(original.getSymbol())
                .description(original.getDescription())
                .formatSymbol(original.getFormatSymbol())
                .formatSymbolPosition(original.getFormatSymbolPosition())
                .showFormatSymbol(original.isShowFormatSymbol())
                .def(original.isDef())
                .rate(original.getRate())
                .direction(original.getDirection())
                .useThousandSeparator(original.isUseThousandSeparator());

        Assert.assertEquals(emptyBuilder.id(), original.getId());

        newCurrency = emptyBuilder.build();
        Assert.assertEquals(newCurrency, original);
        Assert.assertEquals(newCurrency.hashCode(), original.hashCode());
    }

    @Test
    public void testEquals() {
        Integer id = RANDOM.nextInt();
        String symbol = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String formatSymbol = UUID.randomUUID().toString();
        Integer formatSymbolPosition = RANDOM.nextInt();
        Boolean showFormatSymbol = RANDOM.nextBoolean();
        Boolean def = RANDOM.nextBoolean();
        BigDecimal rate = new BigDecimal(RANDOM.nextDouble());
        Integer direction = RANDOM.nextInt();
        Boolean useSeparator = RANDOM.nextBoolean();

        Currency c1 = new Currency(id, symbol, description, formatSymbol,
                formatSymbolPosition, showFormatSymbol,
                def, rate, direction, useSeparator
        );

        Currency c2 = new Currency(id, symbol, description, formatSymbol,
                formatSymbolPosition, showFormatSymbol,
                def, rate, direction, useSeparator
        );

        Assert.assertEquals(c1, c2);
        Assert.assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void testBuilderNullId() {
        Currency.Builder builder = new Currency.Builder(newCurrency());
        builder.id(0).build();
    }
}
