/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MySQLBuilder;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.testng.SkipException;
import javax.sql.DataSource;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class BaseDaoTest extends BaseTest {
    private static final String TEST_DB_NAME = "TestDB";

    private DataSource dataSource = null;
    private String dbName = null;

    public void setupAndSkip() throws Exception {
        dbName = System.getProperty("mysql.database", TEST_DB_NAME);
        String host = System.getProperty("mysql.host", "localhost");
        String user = System.getProperty("mysql.user");
        String password = System.getProperty("mysql.password");

        if (user == null || password == null) {
            throw new SkipException("Test config is not set");
        }

        dataSource = new MySQLBuilder()
                .host(host)
                .user(user)
                .password(password)
                .name(dbName)
                .build();

        getDao().initialize(dataSource);
    }

    public void cleanup() throws Exception {
    }

    void initializeEmptyMoneyFile() {
        getDao().createTables();
        getDao().preload((t) -> {
        });
    }

    int newCategoryId() {
        return getDao().generatePrimaryKey(Category.class);
    }

    int newAccountId() {
        return getDao().generatePrimaryKey(Account.class);
    }

    int newCurrencyId() {
        return getDao().generatePrimaryKey(Currency.class);
    }

    int newContactId() {
        return getDao().generatePrimaryKey(Contact.class);
    }

    int newTransactionGroupId() {
        return getDao().generatePrimaryKey(TransactionGroup.class);
    }

    int newTransactionId() {
        return getDao().generatePrimaryKey(Transaction.class);
    }
}
