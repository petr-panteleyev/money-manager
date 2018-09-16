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

package org.panteleyev.money.persistence;

import org.panteleyev.money.persistence.dto.AccountDto;
import org.panteleyev.money.persistence.dto.CategoryDto;
import org.panteleyev.money.persistence.dto.ContactDto;
import org.panteleyev.money.persistence.dto.CurrencyDto;
import org.panteleyev.money.persistence.dto.TransactionDto;
import org.panteleyev.money.persistence.dto.TransactionGroupDto;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionGroup;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newAccount;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newCategory;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newContact;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newCurrency;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newTransaction;
import static org.panteleyev.money.persistence.PersistenceTestUtils.newTransactionGroup;
import static org.testng.Assert.assertEquals;

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

    @Test
    public void testCategory() {
        int id = newCategoryId();
        Category category = newCategory(id);

        getDao().insertCategory(category);
        assertEquals(getDao().getCategory(id).orElseThrow(), category);
        CategoryDto dto = getDao().get(id, CategoryDto.class);
        assertEquals(dto.decrypt(PASSWORD), category);

        Category update = newCategory(id);
        getDao().updateCategory(update);
        assertEquals(getDao().getCategory(id).orElseThrow(), update);
        dto = getDao().get(id, CategoryDto.class);
        assertEquals(dto.decrypt(PASSWORD), update);
    }

    @Test
    public void testCurrency() {
        int id = newCurrencyId();
        Currency category = newCurrency(id);

        getDao().insertCurrency(category);
        assertEquals(getDao().getCurrency(id).orElseThrow(), category);
        CurrencyDto dto = getDao().get(id, CurrencyDto.class);
        assertEquals(dto.decrypt(PASSWORD), category);

        Currency update = newCurrency(id);
        getDao().updateCurrency(update);
        assertEquals(getDao().getCurrency(id).orElseThrow(), update);
        dto = getDao().get(id, CurrencyDto.class);
        assertEquals(dto.decrypt(PASSWORD), update);
    }

    @Test
    public void testContact() {
        int id = newContactId();
        Contact category = newContact(id);

        getDao().insertContact(category);
        assertEquals(getDao().getContact(id).orElseThrow(), category);
        ContactDto dto = getDao().get(id, ContactDto.class);
        assertEquals(dto.decrypt(PASSWORD), category);

        Contact update = newContact(id);
        getDao().updateContact(update);
        assertEquals(getDao().getContact(id).orElseThrow(), update);
        dto = getDao().get(id, ContactDto.class);
        assertEquals(dto.decrypt(PASSWORD), update);
    }

    @Test
    public void testAccount() {
        int id = newAccountId();
        Account category = newAccount(id);

        getDao().insertAccount(category);
        assertEquals(getDao().getAccount(id).orElseThrow(), category);
        AccountDto dto = getDao().get(id, AccountDto.class);
        assertEquals(dto.decrypt(PASSWORD), category);

        Account update = newAccount(id);
        getDao().updateAccount(update);
        assertEquals(getDao().getAccount(id).orElseThrow(), update);
        dto = getDao().get(id, AccountDto.class);
        assertEquals(dto.decrypt(PASSWORD), update);
    }

    @Test
    public void testTransaction() {
        int id = newTransactionId();
        Transaction category = newTransaction(id);

        getDao().insertTransaction(category);
        assertEquals(getDao().getTransaction(id).orElseThrow(), category);
        TransactionDto dto = getDao().get(id, TransactionDto.class);
        assertEquals(dto.decrypt(PASSWORD), category);

        Transaction update = newTransaction(id);
        getDao().updateTransaction(update);
        assertEquals(getDao().getTransaction(id).orElseThrow(), update);
        dto = getDao().get(id, TransactionDto.class);
        assertEquals(dto.decrypt(PASSWORD), update);
    }

    @Test
    public void testTransactionGroup() {
        int id = newTransactionGroupId();
        TransactionGroup category = newTransactionGroup(id);

        getDao().insertTransactionGroup(category);
        assertEquals(getDao().getTransactionGroup(id).orElseThrow(), category);
        TransactionGroupDto dto = getDao().get(id, TransactionGroupDto.class);
        assertEquals(dto.decrypt(PASSWORD), category);

        TransactionGroup update = newTransactionGroup(id);
        getDao().updateTransactionGroup(update);
        assertEquals(getDao().getTransactionGroup(id).orElseThrow(), update);
        dto = getDao().get(id, TransactionGroupDto.class);
        assertEquals(dto.decrypt(PASSWORD), update);
    }
}
