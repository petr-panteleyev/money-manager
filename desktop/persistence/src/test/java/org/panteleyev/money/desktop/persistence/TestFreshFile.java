/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

        var icons = cache.getIcons();
        Assertions.assertTrue(icons.isEmpty());

        var currencies = cache.getCurrencies();
        Assertions.assertTrue(currencies.isEmpty());

        var categories = cache.getCategories();
        Assertions.assertTrue(categories.isEmpty());

        var accounts = cache.getAccounts();
        Assertions.assertTrue(accounts.isEmpty());

        var transactions = cache.getTransactions();
        Assertions.assertTrue(transactions.isEmpty());

        var contacts = cache.getContacts();
        Assertions.assertTrue(contacts.isEmpty());
    }
}
