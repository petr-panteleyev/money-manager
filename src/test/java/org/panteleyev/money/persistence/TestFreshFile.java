/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.testng.Assert.assertTrue;

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

    @Test
    public void testNewFileCreation() {
        initializeEmptyMoneyFile();

        var icons = cache().getIcons();
        assertTrue(icons.isEmpty());

        var currencies = cache().getCurrencies();
        assertTrue(currencies.isEmpty());

        var categories = cache().getCategories();
        assertTrue(categories.isEmpty());

        var accounts = cache().getAccounts();
        assertTrue(accounts.isEmpty());

        var transactions = cache().getTransactions();
        assertTrue(transactions.isEmpty());

        var contacts = cache().getContacts();
        assertTrue(contacts.isEmpty());
    }
}
