/*
 * Copyright (c) 2018, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import org.testng.SkipException;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class BaseDaoTest extends BaseTest {
    private static final String TEST_DB_NAME = "TestDB";

    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";
    public static final String ICON_JAVA = "java.png";

    public void setupAndSkip() throws Exception {
        var dbName = System.getProperty("mysql.database", TEST_DB_NAME);
        var host = System.getProperty("mysql.host", "localhost");
        var user = System.getProperty("mysql.user", null);
        var password = System.getProperty("mysql.password", null);

        if (user == null || password == null) {
            throw new SkipException("Test config is not set");
        }

        var dataSource = new MySQLBuilder()
            .host(host)
            .user(user)
            .password(password)
            .name(dbName)
            .build();

        getDao().initialize(dataSource);
    }

    public void cleanup() throws Exception {
    }

    protected void initializeEmptyMoneyFile() {
        getDao().createTables();
        getDao().preload(t -> { });
    }
}
