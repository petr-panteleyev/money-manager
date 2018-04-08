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

package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.TransactionType;
import org.panteleyev.persistence.Record;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;

public class TestMoneyDAO extends BaseDaoTest {
    @BeforeClass
    @Override
    public void setupAndSkip() {
        try {
            super.setupAndSkip();
            getDao().createTables();
            getDao().preload();
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        getDao().dropTables();
        super.cleanup();
    }

    @DataProvider(name = "testMoneyDAODataProvider")
    public Object[][] testMoneyDAODataProvider() {
        CategoryType catType = randomCategoryType();
        int catID = newCategoryId();
        int currID = newCurrencyId();
        int accID = newAccountId();
        int contactId = newContactId();
        TransactionType transactionType = randomTransactionType();
        int transactionGroupId = newTransactionGroupId();
        int transactionId = newTransactionId();

        return new Object[][]{
                {
                        newCategory(catID, catType)
                },
                {
                        newCurrency(currID)
                },
                {
                        new Currency(
                                newCurrencyId(),
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                RANDOM.nextInt(),
                                RANDOM.nextBoolean(),
                                RANDOM.nextBoolean(),
                                BigDecimal.ZERO,
                                RANDOM.nextInt(),
                                RANDOM.nextBoolean(),
                                UUID.randomUUID().toString(),
                                System.currentTimeMillis()
                        )
                },
                {
                        new Currency(
                                newCurrencyId(),
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                RANDOM.nextInt(),
                                RANDOM.nextBoolean(),
                                RANDOM.nextBoolean(),
                                BigDecimal.TEN,
                                RANDOM.nextInt(),
                                RANDOM.nextBoolean(),
                                UUID.randomUUID().toString(),
                                System.currentTimeMillis()
                        )
                },
                {
                        newContact(contactId)
                },
                {
                        newAccount(accID, catType, catID, currID)
                },
                {
                        new Account(
                                newAccountId(),
                                UUID.randomUUID().toString(),
                                UUID.randomUUID().toString(),
                                BigDecimal.ZERO,
                                BigDecimal.TEN,
                                BigDecimal.ONE,
                                randomCategoryType().getId(),
                                catID,
                                currID,
                                RANDOM.nextBoolean(),
                                UUID.randomUUID().toString(),
                                System.currentTimeMillis()
                        )
                },
                {
                        newTransactionGroup(transactionGroupId)
                },
                {
                        newTransaction(
                                transactionId,
                                transactionType,
                                accID,
                                accID,
                                catType,
                                catType,
                                catID,
                                catID,
                                transactionGroupId,
                                contactId)
                },
                {
                        newTransaction(
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
                                contactId)
                }
        };
    }

    @Test(dataProvider = "testMoneyDAODataProvider")
    public void testMoneyDAOInsert(Record item) {
        getDao().insert(item);
        Record newItem = getDao().get(item.getId(), item.getClass());
        Assert.assertEquals(newItem, item);
        Assert.assertEquals(newItem != null ? item.hashCode() : 0, item.hashCode());
    }

    @DataProvider(name = "testCurrencyUpdateDataProvider")
    public Object[][] testCurrencyUpdateDataProvider() {
        return new Object[][]{
                {
                        new Currency(0,
                                "2",
                                "3",
                                "4",
                                1,
                                true,
                                true,
                                new BigDecimal("10.230000"),
                                1,
                                true,
                                UUID.randomUUID().toString(),
                                System.currentTimeMillis()
                        )
                },
                {
                        new Currency(0,
                                "2",
                                "3",
                                "4",
                                1,
                                false,
                                true,
                                new BigDecimal("10.230000"),
                                -1,
                                false,
                                UUID.randomUUID().toString(),
                                System.currentTimeMillis()
                        )
                }
        };
    }

    @Test(dataProvider = "testCurrencyUpdateDataProvider")
    public void testCurrencyUpdate(Currency c) {
        Currency original = c.copy(newCurrencyId());
        getDao().insertCurrency(original);

        Currency updated = original.setDescription(UUID.randomUUID().toString());
        getDao().updateCurrency(updated);

        Currency newC = getDao().getCurrency(original.getId()).orElseThrow(IllegalStateException::new);
        Assert.assertEquals(newC, updated);
    }
}
