/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.xml;

import org.panteleyev.money.persistence.BaseDaoTest;
import org.panteleyev.money.test.BaseTest;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.IGNORE_PROGRESS;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestImportRecords extends BaseDaoTest {
    private static final String RESOURCE_DIR = "src/test/resources/org/panteleyev/money/xml/TestImportRecords";

    private static final String INITIAL = RESOURCE_DIR + "/initial.xml";
    private static final String UPDATE = RESOURCE_DIR + "/update.xml";
    private static final String EXPECTED = RESOURCE_DIR + "/expected.xml";

    @BeforeClass
    @Override
    public void setupAndSkip() throws Exception {
        try {
            super.setupAndSkip();
            getDao().createTables();
            getDao().preload(t -> { });
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @Test
    public void testImportRecords() throws Exception {
        var initial = new File(INITIAL);
        assertTrue(initial.exists());

        try (var inputStream = new FileInputStream(initial)) {
            getDao().importFullDump(Import.doImport(inputStream), IGNORE_PROGRESS);
        }

        getDao().preload();
        compareDatabase(INITIAL);

        var update = new File(UPDATE);
        assertTrue(update.exists());

        try (var inputStream = new FileInputStream(update)) {
            getDao().importRecords(Import.doImport(inputStream), IGNORE_PROGRESS);
        }

        getDao().preload();
        compareDatabase(EXPECTED);
    }

    private void compareDatabase(String expected) throws Exception {
        var file = new File(expected);
        assertTrue(file.exists());

        try (var inputStream = new FileInputStream(file)) {
            var imp = Import.doImport(inputStream);

            assertEquals(BaseTest.sortedById(cache().getCategories()), BaseTest.sortedById(imp.getCategories()));
            assertEquals(BaseTest.sortedById(cache().getAccounts()), BaseTest.sortedById(imp.getAccounts()));
            assertEquals(BaseTest.sortedById(cache().getCurrencies()), BaseTest.sortedById(imp.getCurrencies()));
            assertEquals(BaseTest.sortedById(cache().getContacts()), BaseTest.sortedById(imp.getContacts()));
            assertEquals(BaseTest.sortedById(cache().getTransactions()), BaseTest.sortedById(imp.getTransactions()));
        }
    }
}
