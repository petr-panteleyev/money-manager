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

package org.panteleyev.money.test

import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Currency
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.math.BigDecimal

class TestEquals : BaseTest() {
    @DataProvider(name = "testEqualsDataProvider")
    fun testEqualsDataProvider(): Array<Array<Any>> {
        val type = randomCategoryType()

        return arrayOf(
                arrayOf(Currency(1, "2", "3", "4", 1, true, true, BigDecimal("10.23"), 1, true),
                        Currency(1, "2", "3", "4", 1, true, true, BigDecimal("10.23"), 1, true),
                        true),
                arrayOf(Currency(1, "2", "3", "4", 1, true, true, BigDecimal("10.23"), 1, true),
                        Currency(1, "2", "3", "4", 1, true, true, BigDecimal("10.24"), 1, false),
                        false),
                arrayOf<Any>(Account(10, "Account name", "Account comment", BigDecimal("10.23"), BigDecimal("100.23"), BigDecimal("10.23"), type.id, 12, 13, true), Account(10, "Account name", "Account comment", BigDecimal("10.23"), BigDecimal("100.23"), BigDecimal("10.23"), type.id, 12, 13, true), true), arrayOf<Any>(Account(10, "Account name", "Account comment", BigDecimal("10.23"), BigDecimal("100.23"), BigDecimal("10.23"), type.id, 12, 13, true),
                    Account(10, "Account name", "Account comment", BigDecimal("10.23"), BigDecimal("100.23"), BigDecimal("10.23"), type.id, 12, 13, false),
                    false)
        )
    }

    @Test(dataProvider = "testEqualsDataProvider")
    fun testEquals(o1: Any, o2: Any, result: Boolean) {
        Assert.assertEquals(o1 == o2, result)
        Assert.assertEquals(o2 == o1, result)

        if (result) {
            Assert.assertEquals(o1.hashCode(), o2.hashCode())
        }
    }
}
