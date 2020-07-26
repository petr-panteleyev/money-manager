/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.panteleyev.money.persistence.DataCache.cache;

public class TestFreshFile extends BaseDaoTest {
    @BeforeClass
    @Override
    public void setupAndSkip() {
        try {
            super.setupAndSkip();
        } catch (Exception ex) {
            throw new SkipException(ex.getMessage());
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        super.cleanup();
    }

    @Test
    public void testNewFileCreation() {
        initializeEmptyMoneyFile();

        var currencies = cache().getCurrencies();
        Assert.assertTrue(currencies.isEmpty());

        var accounts = cache().getAccounts();
        Assert.assertTrue(accounts.isEmpty());

        var transactions = cache().getTransactions();
        Assert.assertTrue(transactions.isEmpty());

        var contacts = cache().getContacts();
        Assert.assertTrue(contacts.isEmpty());
    }
}
