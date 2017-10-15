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

import org.panteleyev.money.persistence.MoneyRecord;
import org.panteleyev.money.xml.Import;
import org.panteleyev.persistence.Record;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Comparator;
import static org.panteleyev.money.persistence.MoneyDAO.IGNORE_PROGRESS;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class TestImportRecords extends BaseDaoTest {
    private static String RESOURCE_DIR = "src/test/resources/org/panteleyev/money/test/TestImportRecords";

    private static String INITIAL = RESOURCE_DIR + "/initial.xml";
    private static String UPDATE = RESOURCE_DIR + "/update.xml";
    private static String EXPECTED = RESOURCE_DIR + "/expected.xml";

    private static final Comparator<MoneyRecord> BY_ID = Comparator.comparingInt(Record::getId);

    @BeforeClass
    @Override
    public void setupAndSkip() throws Exception {
        try {
            super.setupAndSkip();
            getDao().createTables();
            getDao().preload(t -> {
            });
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        getDao().dropTables();
        super.cleanup();
    }

    @Test
    public void testImportRecords() throws Exception {
        File initial = new File(INITIAL);
        Assert.assertTrue(initial.exists());

        try (InputStream inputStream = new FileInputStream(initial)) {
            getDao().importFullDump(Import.doImport(inputStream), IGNORE_PROGRESS);
        }

        getDao().preload();
        compareDatabase(INITIAL);

        File update = new File(UPDATE);
        Assert.assertTrue(update.exists());

        try (InputStream inputStream = new FileInputStream(update)) {
            getDao().importRecords(Import.doImport(inputStream), IGNORE_PROGRESS);
        }

        getDao().preload();
        compareDatabase(EXPECTED);
    }

    private void compareDatabase(String expected) throws Exception {
        File file = new File(expected);
        Assert.assertTrue(file.exists());

        try (InputStream inputStream = new FileInputStream(file)) {
            Import imp = Import.doImport(inputStream);

            Assert.assertEquals(sortedById(getDao().getCategories()), sortedById(imp.getCategories()));
            Assert.assertEquals(sortedById(getDao().getAccounts()), sortedById(imp.getAccounts()));
            Assert.assertEquals(sortedById(getDao().getCurrencies()), sortedById(imp.getCurrencies()));
            Assert.assertEquals(sortedById(getDao().getContacts()), sortedById(imp.getContacts()));
            Assert.assertEquals(sortedById(getDao().getTransactionGroups()), sortedById(imp.getTransactionGroups()));
            Assert.assertEquals(sortedById(getDao().getTransactions()), sortedById(imp.getTransactions()));
        }
    }
}
