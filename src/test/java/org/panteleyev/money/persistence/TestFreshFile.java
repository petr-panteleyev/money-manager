/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.persistence;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.panteleyev.money.app.GlobalContext.cache;
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
