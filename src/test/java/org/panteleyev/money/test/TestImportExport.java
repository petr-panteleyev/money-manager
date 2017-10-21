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
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import org.panteleyev.money.xml.Export;
import org.panteleyev.money.xml.Import;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This test covers XML export/import without database interaction.
 */
public class TestImportExport extends BaseTest {
    private final TransactionGroup tg = newTransactionGroup();

    private final Category cat1 = newCategory();
    private final Category cat2 = newCategory();
    private final Category cat3 = newCategory();

    private final Currency curr1 = newCurrency();
    private final Currency curr2 = newCurrency();
    private final Currency curr3 = newCurrency();

    private final Account acc1 = newAccount(randomId(), cat1, curr1);
    private final Account acc2 = newAccount(randomId(), cat2, curr1);
    private final Account acc3 = newAccount(randomId(), cat3, curr3);

    private final Contact con1 = newContact();
    private final Contact con2 = newContact();
    private final Contact con3 = newContact();

    private final Transaction tr1 = newTransaction(acc1, acc2, con1);
    private final Transaction tr2 = newTransaction(acc2, acc1, tg, con1);
    private final Transaction tr3 = newTransaction(acc2, acc3, tg);
    private final Transaction tr4 = newTransaction(acc1, acc3, con2);

    private final MoneyDAOMock mock = new MoneyDAOMock(
            Arrays.asList(cat1, cat2, cat3),
            Arrays.asList(acc1, acc2, acc3),
            Arrays.asList(con1, con2, con3),
            Arrays.asList(curr1, curr2, curr3),
            Collections.singletonList(tg),
            Arrays.asList(tr1, tr2, tr3, tr4)
    );

    @DataProvider(name = "importExportData")
    public Object[][] importExportData() {
        return new Object[][]{
                {
                        // Empty lists
                        Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST
                },
                {
                        Arrays.asList(newCategory(), newCategory()),
                        Arrays.asList(newAccount(), newAccount(), newAccount()),
                        Arrays.asList(newContact(), newContact()),
                        Arrays.asList(newCurrency(), newCurrency(), newCurrency()),
                        Arrays.asList(newTransactionGroup(), newTransactionGroup(), newTransactionGroup(),
                                newTransactionGroup()),
                        Arrays.asList(newTransaction(), newTransaction(), newTransaction(), newTransaction())
                }
        };
    }

    @Test(dataProvider = "importExportData")
    public void testExportAndImport(List<Category> categories,
                                    List<Account> accounts,
                                    List<Contact> contacts,
                                    List<Currency> currencies,
                                    List<TransactionGroup> transactionGroups,
                                    List<Transaction> transactions) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        new Export().withCategories(categories)
                .withAccounts(accounts, false)
                .withContacts(contacts)
                .withCurrencies(currencies)
                .withTransactionGroups(transactionGroups)
                .withTransactions(transactions, false)
                .doExport(outStream);

        Import imp = Import.doImport(new ByteArrayInputStream(outStream.toByteArray()));

        Assert.assertEquals(imp.getCategories(), categories);
        Assert.assertEquals(imp.getAccounts(), accounts);
        Assert.assertEquals(imp.getContacts(), contacts);
        Assert.assertEquals(imp.getCurrencies(), currencies);
        Assert.assertEquals(imp.getTransactionGroups(), transactionGroups);
        Assert.assertEquals(imp.getTransactions(), transactions);
    }

    @Test
    public void testAccountsWithDependencies() throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        new Export(mock).withAccounts(List.of(), true).doExport(outStream);
        byte[] bytes1 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes1));

        Import imp1 = Import.doImport(new ByteArrayInputStream(bytes1));
        assertEmpty(imp1.getTransactions());
        assertEmpty(imp1.getTransactionGroups());
        assertEmpty(imp1.getContacts());
        assertEmpty(imp1.getCategories());
        assertEmpty(imp1.getCurrencies());
        assertEmpty(imp1.getAccounts());

        outStream.reset();
        new Export(mock).withAccounts(Arrays.asList(acc1, acc2, acc3), true).doExport(outStream);
        byte[] bytes2 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes2));

        Import imp2 = Import.doImport(new ByteArrayInputStream(bytes2));
        assertEmpty(imp2.getTransactions());
        assertEmpty(imp2.getTransactionGroups());
        assertEmpty(imp2.getContacts());

        assertRecords(imp2.getCategories(), cat1, cat2, cat3);
        assertRecords(imp2.getCurrencies(), curr1, curr3);
        assertRecords(imp2.getAccounts(), acc1, acc2, acc3);


        outStream.reset();
        new Export(mock).withAccounts(Collections.singletonList(acc3), true).doExport(outStream);
        byte[] bytes3 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes3));

        Import imp3 = Import.doImport(new ByteArrayInputStream(bytes3));
        assertEmpty(imp3.getTransactions());
        assertEmpty(imp3.getTransactionGroups());
        assertEmpty(imp3.getContacts());

        assertRecords(imp3.getCategories(), cat3);
        assertRecords(imp3.getCurrencies(), curr3);
        assertRecords(imp3.getAccounts(), acc3);


        outStream.reset();
        new Export(mock).withAccounts(Arrays.asList(acc1, acc2), true).doExport(outStream);
        byte[] bytes4 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes4));

        Import imp4 = Import.doImport(new ByteArrayInputStream(bytes4));
        assertEmpty(imp4.getTransactions());
        assertEmpty(imp4.getTransactionGroups());
        assertEmpty(imp4.getContacts());

        assertRecords(imp4.getCategories(), cat1, cat2);
        assertRecords(imp4.getCurrencies(), curr1);
        assertRecords(imp4.getAccounts(), acc1, acc2);
    }

    @Test
    public void testTransactionsWithDependencies() throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        new Export(mock).withTransactions(Arrays.asList(tr1, tr2, tr3), true).doExport(outStream);
        byte[] bytes1 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes1));

        Import imp1 = Import.doImport(new ByteArrayInputStream(bytes1));
        assertRecords(imp1.getCategories(), cat1, cat2, cat3);
        assertRecords(imp1.getAccounts(), acc1, acc2, acc3);
        assertRecords(imp1.getCurrencies(), curr1, curr3);
        assertRecords(imp1.getContacts(), con1);
        assertRecords(imp1.getTransactionGroups(), tg);
        assertRecords(imp1.getTransactions(), tr1, tr2, tr3);

        outStream.reset();
        new Export(mock).withTransactions(Collections.singletonList(tr1), true).doExport(outStream);
        byte[] bytes2 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes2));

        Import imp2 = Import.doImport(new ByteArrayInputStream(bytes2));
        assertRecords(imp2.getCategories(), cat1, cat2);
        assertRecords(imp2.getAccounts(), acc1, acc2);
        assertRecords(imp2.getCurrencies(), curr1);
        assertRecords(imp2.getContacts(), con1);
        assertEmpty(imp2.getTransactionGroups());
        assertRecords(imp2.getTransactions(), tr1);


        outStream.reset();
        new Export(mock).withTransactions(Collections.singletonList(tr4), true).doExport(outStream);
        byte[] bytes3 = outStream.toByteArray();
        validateXML(new ByteArrayInputStream(bytes3));

        Import imp3 = Import.doImport(new ByteArrayInputStream(bytes3));
        assertRecords(imp3.getCategories(), cat1, cat3);
        assertRecords(imp3.getAccounts(), acc1, acc3);
        assertRecords(imp3.getCurrencies(), curr1, curr3);
        assertRecords(imp3.getContacts(), con2);
        assertEmpty(imp3.getTransactionGroups());
        assertRecords(imp3.getTransactions(), tr4);
    }
}
