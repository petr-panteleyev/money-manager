/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Currency;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.math.BigDecimal;

public class TestEquals extends BaseTest {
    @DataProvider(name="testEqualsDataProvider")
    public Object[][] testEqualsDataProvider() {
        CategoryType type = randomCategoryType();

        return new Object[][] {
            {
                new Currency(1, "2", "3", "4", 1, true, true, new BigDecimal("10.23"), 1, true),
                new Currency(1, "2", "3", "4", 1, true, true, new BigDecimal("10.23"), 1, true),
                true
            },
            {
                new Currency(1, "2", "3", "4", 1, true, true, new BigDecimal("10.23"), 1, true),
                new Currency(1, "2", "3", "4", 1, true, true, new BigDecimal("10.24"), 1, false),
                false
            },
            {
                new Account(10, "Account name", "Account comment", new BigDecimal("10.23"), new BigDecimal("100.23"), new BigDecimal("10.23"), type, 12, 13, true),
                new Account(10, "Account name", "Account comment", new BigDecimal("10.23"), new BigDecimal("100.23"), new BigDecimal("10.23"), type, 12, 13, true),
                true
            },
            {
                new Account(10, "Account name", "Account comment", new BigDecimal("10.23"), new BigDecimal("100.23"), new BigDecimal("10.23"), type, 12, 13, true),
                new Account(10, "Account name", "Account comment", new BigDecimal("10.23"), new BigDecimal("100.23"), new BigDecimal("10.23"), type, 12, 13, false),
                false
            },
        };
    }

    @Test(dataProvider="testEqualsDataProvider")
    public void testEquals(Object o1, Object o2, boolean result) {
        Assert.assertEquals(o1.equals(o2), result);
        Assert.assertEquals(o2.equals(o1), result);

        if (result) {
            Assert.assertEquals(o1.hashCode(), o2.hashCode());
        }
    }
}
