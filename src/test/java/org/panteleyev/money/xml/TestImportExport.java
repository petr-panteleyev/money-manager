package org.panteleyev.money.xml;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_DOLLAR;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_EURO;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_JAVA;
import static org.panteleyev.money.test.BaseTestUtils.newAccount;
import static org.panteleyev.money.test.BaseTestUtils.newCategory;
import static org.panteleyev.money.test.BaseTestUtils.newContact;
import static org.panteleyev.money.test.BaseTestUtils.newCurrency;
import static org.panteleyev.money.test.BaseTestUtils.newIcon;
import static org.panteleyev.money.test.BaseTestUtils.newTransaction;
import static org.testng.Assert.assertEquals;

/**
 * This test covers XML export/import without database interaction.
 */
public class TestImportExport extends BaseTest implements XsdUtil {
    private final Icon icon1 = newIcon(ICON_DOLLAR);
    private final Icon icon2 = newIcon(ICON_EURO);
    private final Icon icon3 = newIcon(ICON_JAVA);

    private final Category cat1 = newCategory(icon1);
    private final Category cat2 = newCategory(icon1);
    private final Category cat3 = newCategory(icon2);

    private final Currency curr1 = newCurrency();
    private final Currency curr2 = newCurrency();
    private final Currency curr3 = newCurrency();

    private final Account acc1 = newAccount(cat1, curr1);
    private final Account acc2 = newAccount(cat2, curr1, icon2);
    private final Account acc3 = newAccount(cat3, curr3, icon3);

    private final Contact con1 = newContact();
    private final Contact con2 = newContact();
    private final Contact con3 = newContact();

    private final Transaction tr1 = newTransaction(acc1, acc2, con1);
    private final Transaction tr2 = newTransaction(acc2, acc1, con1);
    private final Transaction tr3 = newTransaction(acc2, acc3);
    private final Transaction tr4 = newTransaction(acc1, acc3, con2);

    private final Transaction detailedTransaction = new Transaction.Builder(newTransaction(acc1, acc2, con1))
        .detailed(true)
        .build();
    private final Transaction detail1 = new Transaction.Builder(newTransaction(acc2, acc1, con1))
        .parentUuid(detailedTransaction.uuid())
        .build();
    private final Transaction detail2 = new Transaction.Builder(newTransaction(acc2, acc3))
        .parentUuid(detailedTransaction.uuid())
        .build();

    private final DataCache mock = new DataCache() {
        {
            getIcons().addAll(icon1, icon2, icon3);
            getCategories().addAll(cat1, cat2, cat3);
            getAccounts().addAll(acc1, acc2, acc3);
            getContacts().addAll(con1, con2, con3);
            getCurrencies().addAll(curr1, curr2, curr3);
            getTransactions().addAll(tr1, tr2, tr3, tr4, detailedTransaction, detail1, detail2);
        }
    };

    @DataProvider(name = "importExportData")
    public Object[][] importExportData() {
        return new Object[][]{
            {
                // Empty lists
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
            },
            {
                List.of(icon1, icon2, icon3),
                List.of(cat1, cat2),
                List.of(acc1, acc2, acc3),
                List.of(newContact(), newContact(), newContact("A & B <some@email.com>")),
                List.of(newCurrency(), newCurrency(), newCurrency()),
                List.of(newTransaction(acc1, acc2),
                    newTransaction(acc2, acc3),
                    newTransaction(acc1, acc3),
                    newTransaction(acc1, acc2))
            }
        };
    }

    @Test(dataProvider = "importExportData")
    public void testExportAndImport(List<Icon> icons,
                                    List<Category> categories,
                                    List<Account> accounts,
                                    List<Contact> contacts,
                                    List<Currency> currencies,
                                    List<Transaction> transactions)
    {
        var outStream = new ByteArrayOutputStream();

        new Export().withIcons(icons)
            .withCategories(categories, false)
            .withAccounts(accounts, false)
            .withContacts(contacts, false)
            .withCurrencies(currencies)
            .withTransactions(transactions, false)
            .doExport(outStream);

        var imp = Import.doImport(new ByteArrayInputStream(outStream.toByteArray()));

        assertEquals(imp.getIcons(), icons);
        assertEquals(imp.getCategories(), categories);
        assertEquals(imp.getAccounts(), accounts);
        assertEquals(imp.getContacts(), contacts);
        assertEquals(imp.getCurrencies(), currencies);
        assertEquals(imp.getTransactions(), transactions);
    }

    @Test
    public void testCategoriesWithDependencies() throws Exception {
        var outStream = new ByteArrayOutputStream();

        new Export(mock).withCategories(List.of(), true).doExport(outStream);
        var bytes1 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes1));

        var imp1 = Import.doImport(new ByteArrayInputStream(bytes1));
        assertEmpty(imp1.getIcons());
        assertEmpty(imp1.getTransactions());
        assertEmpty(imp1.getContacts());
        assertEmpty(imp1.getCategories());
        assertEmpty(imp1.getCurrencies());
        assertEmpty(imp1.getAccounts());

        outStream.reset();
        new Export(mock).withCategories(List.of(cat1, cat2), true).doExport(outStream);
        var bytes2 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes2));

        var imp2 = Import.doImport(new ByteArrayInputStream(bytes2));
        assertEmpty(imp2.getAccounts());
        assertEmpty(imp2.getTransactions());
        assertEmpty(imp2.getContacts());

        assertRecords(imp2.getIcons(), icon1);
        assertRecords(imp2.getCategories(), cat1, cat2);

        outStream.reset();
        new Export(mock).withCategories(List.of(cat1, cat3), true).doExport(outStream);
        var bytes3 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes3));

        var imp3 = Import.doImport(new ByteArrayInputStream(bytes3));
        assertEmpty(imp3.getAccounts());
        assertEmpty(imp3.getTransactions());
        assertEmpty(imp3.getContacts());

        assertRecords(imp3.getIcons(), icon1, icon2);
        assertRecords(imp3.getCategories(), cat1, cat3);
    }

    @Test
    public void testAccountsWithDependencies() throws Exception {
        var outStream = new ByteArrayOutputStream();

        new Export(mock).withAccounts(List.of(), true).doExport(outStream);
        var bytes1 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes1));

        var imp1 = Import.doImport(new ByteArrayInputStream(bytes1));
        assertEmpty(imp1.getIcons());
        assertEmpty(imp1.getTransactions());
        assertEmpty(imp1.getContacts());
        assertEmpty(imp1.getCategories());
        assertEmpty(imp1.getCurrencies());
        assertEmpty(imp1.getAccounts());

        outStream.reset();
        new Export(mock).withAccounts(List.of(acc1, acc2, acc3), true).doExport(outStream);
        var bytes2 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes2));

        var imp2 = Import.doImport(new ByteArrayInputStream(bytes2));
        assertEmpty(imp2.getTransactions());
        assertEmpty(imp2.getContacts());

        assertRecords(imp2.getIcons(), icon1, icon2, icon3);
        assertRecords(imp2.getCategories(), cat1, cat2, cat3);
        assertRecords(imp2.getCurrencies(), curr1, curr3);
        assertRecords(imp2.getAccounts(), acc1, acc2, acc3);


        outStream.reset();
        new Export(mock).withAccounts(Collections.singletonList(acc3), true).doExport(outStream);
        var bytes3 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes3));

        var imp3 = Import.doImport(new ByteArrayInputStream(bytes3));
        assertEmpty(imp3.getTransactions());
        assertEmpty(imp3.getContacts());

        assertRecords(imp3.getIcons(), icon2, icon3);
        assertRecords(imp3.getCategories(), cat3);
        assertRecords(imp3.getCurrencies(), curr3);
        assertRecords(imp3.getAccounts(), acc3);


        outStream.reset();
        new Export(mock).withAccounts(List.of(acc1, acc2), true).doExport(outStream);
        var bytes4 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes4));

        var imp4 = Import.doImport(new ByteArrayInputStream(bytes4));
        assertEmpty(imp4.getTransactions());
        assertEmpty(imp4.getContacts());

        assertRecords(imp4.getIcons(), icon1, icon2);
        assertRecords(imp4.getCategories(), cat1, cat2);
        assertRecords(imp4.getCurrencies(), curr1);
        assertRecords(imp4.getAccounts(), acc1, acc2);
    }

    @Test
    public void testTransactionsWithDependencies() throws Exception {
        var outStream = new ByteArrayOutputStream();

        new Export(mock).withTransactions(List.of(tr1, tr2, tr3), true).doExport(outStream);
        var bytes1 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes1));

        var imp1 = Import.doImport(new ByteArrayInputStream(bytes1));
        assertRecords(imp1.getIcons(), icon1, icon2, icon3);
        assertRecords(imp1.getCategories(), cat1, cat2, cat3);
        assertRecords(imp1.getAccounts(), acc1, acc2, acc3);
        assertRecords(imp1.getCurrencies(), curr1, curr3);
        assertRecords(imp1.getContacts(), con1);
        assertRecords(imp1.getTransactions(), tr1, tr2, tr3);

        outStream.reset();
        new Export(mock).withTransactions(Collections.singletonList(tr1), true).doExport(outStream);
        var bytes2 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes2));

        var imp2 = Import.doImport(new ByteArrayInputStream(bytes2));
        assertRecords(imp2.getCategories(), cat1, cat2);
        assertRecords(imp2.getAccounts(), acc1, acc2);
        assertRecords(imp2.getCurrencies(), curr1);
        assertRecords(imp2.getContacts(), con1);
        assertRecords(imp2.getTransactions(), tr1);

        outStream.reset();
        new Export(mock)
            .withTransactions(Collections.singletonList(tr4), true)
            .doExport(outStream);
        var bytes3 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes3));

        var imp3 = Import.doImport(new ByteArrayInputStream(bytes3));
        assertRecords(imp3.getCategories(), cat1, cat3);
        assertRecords(imp3.getAccounts(), acc1, acc3);
        assertRecords(imp3.getCurrencies(), curr1, curr3);
        assertRecords(imp3.getContacts(), con2);
        assertRecords(imp3.getTransactions(), tr4);
    }

    @Test
    public void testTransactionsWithDetails() throws Exception {
        var outStream = new ByteArrayOutputStream();

        new Export(mock).withTransactions(List.of(detailedTransaction), true).doExport(outStream);
        var bytes = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes));

        var imp = Import.doImport(new ByteArrayInputStream(bytes));
        assertRecords(imp.getCategories(), cat1, cat2, cat3);
        assertRecords(imp.getAccounts(), acc1, acc2, acc3);
        assertRecords(imp.getCurrencies(), curr1, curr3);
        assertRecords(imp.getContacts(), con1);
        assertRecords(imp.getTransactions(), detailedTransaction, detail1, detail2);
    }
}
