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

import org.panteleyev.money.persistence.MoneyDAO;
import javax.sql.DataSource;
import java.io.File;

public class BaseDaoTest extends BaseTest {
    static final int CATEGORY_TYPES_SIZE = 7;
    static final int TRANSACTION_TYPES_SIZE = 21;
    static final int CONTACT_TYPES_SIZE = 6;

    private File TEST_DB_FILE;

    private MoneyDAO dao;

    public void setupAndSkip() throws Exception {
        TEST_DB_FILE = File.createTempFile("money-manager-test", "db");

        DataSource ds = new MoneyDAO.Builder()
                .file(TEST_DB_FILE.getAbsolutePath())
                .build();

        dao = MoneyDAO.initialize(ds);
    }

    public void cleanup() throws Exception {
        if (TEST_DB_FILE != null && TEST_DB_FILE.exists()) {
            TEST_DB_FILE.delete();
        }
    }

    MoneyDAO getDao() {
        return dao;
    }

    void initializeEmptyMoneyFile() throws Exception {
        dao.createTables();
        dao.setupNewDatabase();
        dao.preload();
    }
}
