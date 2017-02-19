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

import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.TransactionType;
import org.panteleyev.persistence.Record;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

public class TestMoneyDAO extends BaseDaoTest {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @BeforeClass
    @Override
    public void setupAndSkip() throws Exception {
        try {
            super.setupAndSkip();
            getDao().createTables();
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        super.cleanup();
    }

    @DataProvider(name="testMoneyDAODataProvider")
    public Object[][] testMoneyDAODataProvider() {
        CategoryType catType = randomCategoryType();
        Integer catID = RANDOM.nextInt();
        Integer currID = RANDOM.nextInt();
        Integer accID = RANDOM.nextInt();
        Integer contactId = RANDOM.nextInt();
        TransactionType transactionType = randomTransactionType();
        Integer transactionGroupId = RANDOM.nextInt();
        Integer transactionId = RANDOM.nextInt();

        return new Object[][] {
                { newCategory(catID, catType ) },
                { newCurrency(currID) },
                { newContact(contactId) },
                { newAccount(accID, catType, catID, currID) },
                { newTransactionGroup(transactionGroupId) },
                { newTransaction(
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
        };
    }

    @Test(dataProvider="testMoneyDAODataProvider")
    public void testMoneyDAOInsert(Record item) throws Exception {
        MoneyDAO dao = getDao();

        dao.insert(item);
        Record newItem = dao.get(item.getId(), item.getClass());
        Assert.assertEquals(newItem, item);
    }

    @DataProvider(name="testCurrencyUpdateDataProvider")
    public Object[][] testCurrencyUpdateDataProvider() {
        return new Object[][] {
            { new Currency(null, "2", "3", "4", 1, true, true, new BigDecimal("10.230000"), 1, true) },
            { new Currency(null, "2", "3", "4", 1, false, true, new BigDecimal("10.230000"), -1, false) },
        };
    }

    @Test(dataProvider="testCurrencyUpdateDataProvider")
    public void testCurrencyUpdate(Currency c) throws Exception {
        MoneyDAO dao = getDao();

        Currency.Builder builder = new Currency.Builder(c)
                .id(dao.generatePrimaryKey(Currency.class));

        Currency original = builder.build();

        dao.insertCurrency(original);

        Currency updated = builder.description(UUID.randomUUID().toString()).build();

        dao.updateCurrency(updated);

        Currency newC = dao.getCurrency(original.getId()).orElse(null);
        Assert.assertEquals(newC, updated);
    }
}
