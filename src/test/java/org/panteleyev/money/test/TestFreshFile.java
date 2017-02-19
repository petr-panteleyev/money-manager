/*
 * Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import java.util.Collection;
import java.util.stream.IntStream;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.money.persistence.TransactionType;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestFreshFile extends BaseDaoTest {
    @BeforeMethod
    @Override
    public void setupAndSkip() throws Exception {
        try {
            super.setupAndSkip();
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterMethod
    @Override
    public void cleanup() throws Exception {
        super.cleanup();
    }

    @Test
    public void testNewFileCreation() throws Exception {
        initializeEmptyMoneyFile();

        Collection<Currency> currencies = getDao().getCurrencies();
        Assert.assertTrue(currencies.isEmpty());

        Collection<Account> accounts = getDao().getAccounts();
        Assert.assertTrue(accounts.isEmpty());

        Collection<Transaction> transactions = getDao().getTransactions();
        Assert.assertTrue(transactions.isEmpty());

        Collection<TransactionGroup> groups = getDao().getTransactionGroups();
        Assert.assertTrue(groups.isEmpty());

        Collection<Contact> contacts = getDao().getContacts();
        Assert.assertTrue(contacts.isEmpty());
    }
}
