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

package org.panteleyev.money.persistence;

import org.panteleyev.money.BaseTest;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionGroup;
import org.testng.SkipException;
import javax.sql.DataSource;
import java.util.UUID;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.persistence.dto.Dto.dtoClass;

public class BaseDaoTest extends BaseTest {
    private static final String TEST_DB_NAME = "TestDB";
    static final String PASSWORD = UUID.randomUUID().toString();

    public void setupAndSkip() throws Exception {
        String dbName = System.getProperty("mysql.database", TEST_DB_NAME);
        String host = System.getProperty("mysql.host", "localhost");
        String user = System.getProperty("mysql.user");
        String password = System.getProperty("mysql.password");

        if (user == null || password == null) {
            throw new SkipException("Test config is not set");
        }

        DataSource dataSource = new MySQLBuilder()
                .host(host)
                .user(user)
                .password(password)
                .name(dbName)
                .build();

        getDao().setEncryptionKey(PASSWORD);
        getDao().initialize(dataSource);
    }

    public void cleanup() throws Exception {
    }

    protected void initializeEmptyMoneyFile() {
        getDao().createTables();
        getDao().preload((t) -> {
        });
    }

    protected int newCategoryId() {
        return getDao().generatePrimaryKey(dtoClass(Category.class));
    }

    protected int newAccountId() {
        return getDao().generatePrimaryKey(dtoClass(Account.class));
    }

    protected int newCurrencyId() {
        return getDao().generatePrimaryKey(dtoClass(Currency.class));
    }

    protected int newContactId() {
        return getDao().generatePrimaryKey(dtoClass(Contact.class));
    }

    protected int newTransactionGroupId() {
        return getDao().generatePrimaryKey(dtoClass(TransactionGroup.class));
    }

    protected int newTransactionId() {
        return getDao().generatePrimaryKey(dtoClass(Transaction.class));
    }
}
