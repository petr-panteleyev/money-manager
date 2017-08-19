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
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.persistence.Record
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.math.BigDecimal
import java.util.UUID

class TestMoneyDAO : BaseDaoTest() {
    @BeforeClass
    @Throws(Exception::class)
    override fun setupAndSkip() {
        try {
            super.setupAndSkip()
            with (MoneyDAO) {
                createTables()
                preload()
            }
        } catch (ex: Exception) {
            throw SkipException("Database not configured")
        }

    }

    @AfterClass
    @Throws(Exception::class)
    override fun cleanup() {
        MoneyDAO.dropTables()
        super.cleanup()
    }

    @DataProvider(name = "testMoneyDAODataProvider")
    fun testMoneyDAODataProvider(): Array<Array<Record>> {
        val catType = randomCategoryType()
        val catID = newCategoryId()
        val currID = newCurrencyId()
        val accID = newAccountId()
        val contactId = newContactId()
        val transactionType = randomTransactionType()
        val transactionGroupId = newTransactionGroupId()
        val transactionId = newTransactionId()

        return arrayOf(
                arrayOf<Record>(newCategory(catID, catType)),
                arrayOf<Record>(newCurrency(currID)),
                arrayOf<Record>(Currency(
                        newCurrencyId(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        BaseTest.RANDOM.nextInt(),
                        BaseTest.RANDOM.nextBoolean(),
                        BaseTest.RANDOM.nextBoolean(),
                        BigDecimal.ZERO,
                        BaseTest.RANDOM.nextInt(),
                        BaseTest.RANDOM.nextBoolean(),
                        guid = UUID.randomUUID().toString(),
                        modified = System.currentTimeMillis()
                )),
                arrayOf<Record>(Currency(
                        newCurrencyId(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        BaseTest.RANDOM.nextInt(),
                        BaseTest.RANDOM.nextBoolean(),
                        BaseTest.RANDOM.nextBoolean(),
                        BigDecimal.TEN,
                        BaseTest.RANDOM.nextInt(),
                        BaseTest.RANDOM.nextBoolean(),
                        guid = UUID.randomUUID().toString(),
                        modified = System.currentTimeMillis()
                )),
                arrayOf<Record>(newContact(contactId)),
                arrayOf<Record>(newAccount(accID, catType, catID, currID)),
                arrayOf<Record>(Account(
                        newAccountId(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        BigDecimal.ZERO,
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        randomCategoryType().id,
                        catID,
                        currID,
                        BaseTest.RANDOM.nextBoolean(),
                        guid = UUID.randomUUID().toString(),
                        modified = System.currentTimeMillis()
                )),
                arrayOf<Record>(newTransactionGroup(transactionGroupId)),
                arrayOf<Record>(newTransaction(
                        transactionId,
                        transactionType,
                        accID,
                        accID,
                        catType,
                        catType,
                        catID,
                        catID,
                        transactionGroupId,
                        contactId)),
                arrayOf<Record>(newTransaction(
                        newTransactionId(),
                        BigDecimal.TEN,
                        BigDecimal.ONE,
                        transactionType,
                        accID,
                        accID,
                        catType,
                        catType,
                        catID,
                        catID,
                        transactionGroupId,
                        contactId))
        )
    }

    @Test(dataProvider = "testMoneyDAODataProvider")
    @Throws(Exception::class)
    fun testMoneyDAOInsert(item: Record) {
        MoneyDAO.insert(item)
        val newItem = MoneyDAO.get(item.id, item.javaClass.kotlin)
        Assert.assertEquals(newItem, item)
        Assert.assertEquals(newItem?.hashCode()?:0, item.hashCode())
    }

    @DataProvider(name = "testCurrencyUpdateDataProvider")
    fun testCurrencyUpdateDataProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf<Any>(
                        Currency(id = 0,
                                symbol = "2",
                                description = "3",
                                formatSymbol = "4",
                                formatSymbolPosition = 1,
                                showFormatSymbol = true,
                                def = true,
                                rate = BigDecimal("10.230000"),
                                direction = 1,
                                useThousandSeparator = true,
                                guid = UUID.randomUUID().toString(),
                                modified = System.currentTimeMillis()
                        )
                ),
                arrayOf<Any>(
                        Currency(id = 0,
                                symbol = "2",
                                description = "3",
                                formatSymbol = "4",
                                formatSymbolPosition = 1,
                                showFormatSymbol = false,
                                def = true,
                                rate = BigDecimal("10.230000"),
                                direction = -1,
                                useThousandSeparator = false,
                                guid = UUID.randomUUID().toString(),
                                modified = System.currentTimeMillis()
                        )
                )
        )
    }

    @Test(dataProvider = "testCurrencyUpdateDataProvider")
    @Throws(Exception::class)
    fun testCurrencyUpdate(c: Currency) {
        val original = c.copy(id = newCurrencyId())

        MoneyDAO.insertCurrency(original)

        val updated = original.copy(description = UUID.randomUUID().toString())

        MoneyDAO.updateCurrency(updated)

        val newC = MoneyDAO.getCurrency(original.id)
        Assert.assertEquals(newC, updated)
    }
}
