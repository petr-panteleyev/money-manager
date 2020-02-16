package org.panteleyev.money.xml;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.persistence.BaseDaoTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileInputStream;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.IGNORE_PROGRESS;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class TestImportRecords extends BaseDaoTest {
    private static String RESOURCE_DIR = "src/test/resources/org/panteleyev/money/xml/TestImportRecords";

    private static String INITIAL = RESOURCE_DIR + "/initial.xml";
    private static String UPDATE = RESOURCE_DIR + "/update.xml";
    private static String EXPECTED = RESOURCE_DIR + "/expected.xml";

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

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        getDao().dropTables();
        super.cleanup();
    }

    @Test(enabled = false)
    public void testImportRecords() throws Exception {
        var initial = new File(INITIAL);
        Assert.assertTrue(initial.exists());

        try (var inputStream = new FileInputStream(initial)) {
            getDao().importFullDump(Import.doImport(inputStream), IGNORE_PROGRESS);
        }

        getDao().preload();
        compareDatabase(INITIAL);

        var update = new File(UPDATE);
        Assert.assertTrue(update.exists());

        try (var inputStream = new FileInputStream(update)) {
            getDao().importRecords(Import.doImport(inputStream), IGNORE_PROGRESS);
        }

        getDao().preload();
        compareDatabase(EXPECTED);
    }

    private void compareDatabase(String expected) throws Exception {
        var file = new File(expected);
        Assert.assertTrue(file.exists());

        try (var inputStream = new FileInputStream(file)) {
            var imp = Import.doImport(inputStream);

            Assert.assertEquals(sortedById(cache().getCategories()), sortedById(imp.getCategories()));
            Assert.assertEquals(sortedById(cache().getAccounts()), sortedById(imp.getAccounts()));
            Assert.assertEquals(sortedById(cache().getCurrencies()), sortedById(imp.getCurrencies()));
            Assert.assertEquals(sortedById(cache().getContacts()), sortedById(imp.getContacts()));
            Assert.assertEquals(sortedById(cache().getTransactions()), sortedById(imp.getTransactions()));
        }
    }
}
