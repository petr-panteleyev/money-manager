/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.xml;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_DOLLAR;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_EURO;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_JAVA;
import static org.panteleyev.money.test.BaseTestUtils.newAccount;
import static org.panteleyev.money.test.BaseTestUtils.newCategory;
import static org.panteleyev.money.test.BaseTestUtils.newContact;
import static org.panteleyev.money.test.BaseTestUtils.newCurrency;
import static org.panteleyev.money.test.BaseTestUtils.newDocument;
import static org.panteleyev.money.test.BaseTestUtils.newIcon;
import static org.panteleyev.money.test.BaseTestUtils.newTransaction;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;

/**
 * This test covers XML export/import without database interaction.
 */
public class TestImportExport extends BaseTest {
    private final Icon icon1 = newIcon(ICON_DOLLAR);
    private final Icon icon2 = newIcon(ICON_EURO);
    private final Icon icon3 = newIcon(ICON_JAVA);

    private final MoneyDocument document1 = newDocument(DocumentType.BILL);
    private final MoneyDocument document2 = newDocument(DocumentType.CONTRACT);
    private final MoneyDocument document3 = newDocument(DocumentType.RECEIPT);
    private final MoneyDocument document4 = newDocument(DocumentType.OTHER);

    private final Map<UUID, byte[]> blobs = Map.of(
            document1.uuid(), randomString().getBytes(StandardCharsets.UTF_8),
            document2.uuid(), randomString().getBytes(StandardCharsets.UTF_8),
            document3.uuid(), randomString().getBytes(StandardCharsets.UTF_8),
            document4.uuid(), randomString().getBytes(StandardCharsets.UTF_8)
    );

    private final Category cat1 = newCategory(icon1);
    private final Category cat2 = newCategory(icon1);
    private final Category cat3 = newCategory(icon2);

    private final Currency curr1 = newCurrency();
    private final Currency curr2 = newCurrency();
    private final Currency curr3 = newCurrency();

    private final Account acc1 = newAccount(cat1, curr1);
    private final Account acc2 = newAccount(cat2, curr1, icon2);
    private final Account acc3 = newAccount(cat3, curr2, icon3);

    private final MoneyDAO dao = Mockito.mock(MoneyDAO.class);

    @BeforeTest
    public void init() {
        when(dao.getDocumentBytes(any(MoneyDocument.class))).thenAnswer((Answer<byte[]>) invocation -> {
            var document = (MoneyDocument) invocation.getArgument(0);
            return blobs.get(document.uuid());
        });
    }

    @DataProvider(name = "importExportData")
    public Object[][] importExportData() {
        return new Object[][]{
                {
                        new DataCache()
                },
                {
                        new DataCache() {
                            {
                                getIcons().addAll(icon1, icon2, icon3);
                                getDocuments().addAll(document1, document2, document3, document4);
                                getCategories().addAll(cat1, cat2);
                                getAccounts().addAll(acc1, acc2, acc3);
                                getContacts().addAll(newContact(), newContact(), newContact("A & B <some@email.com>"));
                                getCurrencies().addAll(curr1, curr2, curr3);
                                getTransactions().addAll(newTransaction(acc1, acc2),
                                        newTransaction(acc2, acc3),
                                        newTransaction(acc1, acc3),
                                        newTransaction(acc1, acc2));

                            }
                        }
                }
        };
    }

    @Test(dataProvider = "importExportData")
    public void testExportAndImport(DataCache cache) throws IOException {
        try (var out = new ByteArrayOutputStream(); var zipOut = new ZipOutputStream(out)) {
            new Export(cache, dao).doExport(zipOut);

            var imp = Import.doImport(new ZipInputStream(new ByteArrayInputStream(out.toByteArray())));

            // Assert data
            assertEquals(imp.getIcons(), cache.getIcons());
            assertEquals(imp.getDocuments(), cache.getDocuments());
            assertEquals(imp.getCategories(), cache.getCategories());
            assertEquals(imp.getAccounts(), cache.getAccounts());
            assertEquals(imp.getContacts(), cache.getContacts());
            assertEquals(imp.getCurrencies(), cache.getCurrencies());
            assertEquals(imp.getTransactions(), cache.getTransactions());

            // Get blobs
            var actualBlobs = new ArrayList<BlobContent>();
            BlobContent blobContent;
            while ((blobContent = imp.getNextBlobContent()) != null) {
                actualBlobs.add(blobContent);
                assertEquals(blobContent.bytes(), blobs.get(blobContent.uuid()));
            }

            assertEquals(actualBlobs.size(), imp.getDocuments().size());
        }
    }
}
