/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.app.GlobalContext.cache;

public class TestFreshFile extends BaseDaoTest {
    @BeforeAll
    public static void init() {
        var initialized = BaseDaoTest.setupAndSkip();
        Assumptions.assumeTrue(initialized);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        BaseDaoTest.tearDown();
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
