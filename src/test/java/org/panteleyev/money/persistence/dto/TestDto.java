/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence.dto;

import org.panteleyev.money.persistence.BaseDaoTest;
import org.panteleyev.persistence.Record;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newAccount;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newCategory;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newContact;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newCurrency;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newTransaction;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newTransactionGroup;
import static org.panteleyev.money.persistence.dto.Dto.dtoClass;
import static org.panteleyev.money.persistence.dto.Dto.newDto;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestDto extends BaseDaoTest {
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


    @DataProvider(name = "dto")
    public Object[][] testEncryptDecryptDataProvider() {
        return new Object[][]{
                {newCategory(newCategoryId()), null},
                {newCategory(newCategoryId()), ""},
                {newCategory(newCategoryId()), UUID.randomUUID().toString()},
                {newCurrency(newCurrencyId()), null},
                {newCurrency(newCurrencyId()), ""},
                {newCurrency(newCurrencyId()), UUID.randomUUID().toString()},
                {newAccount(newAccountId()), null},
                {newAccount(newAccountId()), ""},
                {newAccount(newAccountId()), UUID.randomUUID().toString()},
                {newContact(newContactId()), null},
                {newContact(newContactId()), ""},
                {newContact(newContactId()), UUID.randomUUID().toString()},
                {newTransactionGroup(newTransactionGroupId()), null},
                {newTransactionGroup(newTransactionGroupId()), ""},
                {newTransactionGroup(newTransactionGroupId()), UUID.randomUUID().toString()},
                {newTransaction(newTransactionId()), null},
                {newTransaction(newTransactionId()), ""},
                {newTransaction(newTransactionId()), UUID.randomUUID().toString()},
        };
    }

    @Test(dataProvider = "dto")
    public void testEncryptDecrypt(Record model, String password) {
        Dto dto = newDto(model, password);
        assertEquals(model, dto.decrypt(password));
    }

    @Test(dataProvider = "dto")
    public void testDao(Object model, String password) {
        Dto<?> dto = Dto.newDto(model, password);
        getDao().insert(dto);

        Dto<?> newItem = getDao().get(dto.getId(), dto.getClass());
        assertNotNull(newItem);

        Object newModel = newItem.decrypt(password);

        assertEquals(newModel, model);
        assertEquals(newModel.hashCode(), model.hashCode());
    }

    @DataProvider(name = "dtoClassTestDataProvider")
    public Object[][] dtoClassTestDataProvider() {
        return new Object[][]{
                {newCategory(newCategoryId()), CategoryDto.class},
                {newCurrency(newCurrencyId()), CurrencyDto.class},
                {newContact(newContactId()), ContactDto.class},
                {newAccount(newAccountId()), AccountDto.class},
                {newTransactionGroup(newTransactionGroupId()), TransactionGroupDto.class},
                {newTransaction(newTransactionId()), TransactionDto.class},
        };
    }

    @Test(dataProvider = "dtoClassTestDataProvider")
    public void testDtoClass(Record record, Class<?> dtoClazz) {
        Class<?> classFromRecord = dtoClass(record);
        Class<?> classFromClass = dtoClass(record.getClass());

        assertEquals(classFromRecord, dtoClazz);
        assertEquals(classFromClass, dtoClazz);
    }
}
