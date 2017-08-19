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

package org.panteleyev.money.test

import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup
import org.panteleyev.money.xml.Export
import org.panteleyev.money.xml.Import
import org.panteleyev.persistence.Record
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun Collection<Record>.assertRecords(vararg records: Record) {
    Assert.assertEquals(this.size, records.size)
    Assert.assertTrue(this.containsAll(records.toList()))
}

private fun Collection<Record>.assertEmpty() = Assert.assertTrue(this.isEmpty())

/**
 * This test covers XML export/import without database interaction.
 */
class TestImportExport : BaseTest() {
    private val tg = newTransactionGroup()

    private val cat1 = newCategory()
    private val cat2 = newCategory()
    private val cat3 = newCategory()

    private val curr1 = newCurrency()
    private val curr2 = newCurrency()
    private val curr3 = newCurrency()

    private val acc1 = newAccount(category = cat1, currency = curr1)
    private val acc2 = newAccount(category = cat2, currency = curr1)
    private val acc3 = newAccount(category = cat3, currency = curr3)

    private val con1 = newContact()
    private val con2 = newContact()
    private val con3 = newContact()

    private val tr1 = newTransaction(accountDebited = acc1, accountCredited = acc2, contact = con1)
    private val tr2 = newTransaction(accountDebited = acc2, accountCredited = acc1, group = tg, contact = con1)
    private val tr3 = newTransaction(accountDebited = acc2, accountCredited = acc3, group = tg)
    private val tr4 = newTransaction(accountDebited = acc1, accountCredited = acc3, contact = con2)

    private val mock = MoneyDAOMock(
            categories = listOf(cat1, cat2, cat3),
            contacts = listOf(con1, con2, con3),
            currencies = listOf(curr1, curr2, curr3),
            accounts = listOf(acc1, acc2, acc3),
            transactionGroups = listOf(tg),
            transactions = listOf(tr1, tr2, tr3, tr4)
    )

    @DataProvider(name = "importExportData")
    fun importExportData(): Array<Array<Any>> = arrayOf(
            // Empty lists
            arrayOf<Any>(listOf<Category>(), listOf<Account>(), listOf<Contact>(), listOf<Currency>(), listOf<TransactionGroup>(), listOf<Transaction>()),
            arrayOf<Any>(
                    listOf(newCategory(), newCategory()),
                    listOf(newAccount(), newAccount(), newAccount()),
                    listOf(newContact(), newContact()),
                    listOf(newCurrency(), newCurrency(), newCurrency()),
                    listOf(newTransactionGroup(), newTransactionGroup(), newTransactionGroup(), newTransactionGroup()),
                    listOf(newTransaction(), newTransaction(), newTransaction(), newTransaction())
            )
    )

    @Test(dataProvider = "importExportData")
    fun textExportAndImport(categories: List<Category>,
                            accounts: List<Account>,
                            contacts: List<Contact>,
                            currencies: List<Currency>,
                            transactionGroups: List<TransactionGroup>,
                            transactions: List<Transaction>
    ) {
        val outStream = ByteArrayOutputStream()

        Export().withCategories(categories)
                .withAccounts(accounts)
                .withContacts(contacts)
                .withCurrencies(currencies)
                .withTransactionGroups(transactionGroups)
                .withTransactions(transactions)
                .export(outStream)

        Import.import(ByteArrayInputStream(outStream.toByteArray())).apply {
            Assert.assertEquals(categories, categories)
            Assert.assertEquals(accounts, accounts)
            Assert.assertEquals(contacts, contacts)
            Assert.assertEquals(currencies, currencies)
            Assert.assertEquals(transactionGroups, transactionGroups)
            Assert.assertEquals(transactions, transactions)
        }
    }

    private fun checkResult(records: Collection<Record>, vararg contains: Record) {
        Assert.assertEquals(records.size, contains.size)
        Assert.assertTrue(records.containsAll(contains.toList()))
    }

    @Test
    fun testAccountsWithDependencies() {
        val outStream = ByteArrayOutputStream()

        Export(mock).withAccounts(listOf(), true).export(outStream)
        val bytes1 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes1))

        Import.import(ByteArrayInputStream(bytes1)).apply {
            transactions.assertEmpty()
            transactionGroups.assertEmpty()
            contacts.assertEmpty()

            categories.assertEmpty()
            currencies.assertEmpty()
            accounts.assertEmpty()
        }

        outStream.reset()
        Export(mock).withAccounts(listOf(acc1, acc2, acc3), true).export(outStream)
        val bytes2 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes2))

        Import.import(ByteArrayInputStream(bytes2)).apply {
            transactions.assertEmpty()
            transactionGroups.assertEmpty()
            contacts.assertEmpty()

            categories.assertRecords(cat1, cat2, cat3)
            currencies.assertRecords(curr1, curr3)
            accounts.assertRecords(acc1, acc2, acc3)
        }

        outStream.reset()
        Export(mock).withAccounts(listOf(acc3), true).export(outStream)
        val bytes3 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes3))

        Import.import(ByteArrayInputStream(bytes3)).apply {
            transactions.assertEmpty()
            transactionGroups.assertEmpty()
            contacts.assertEmpty()

            categories.assertRecords(cat3)
            currencies.assertRecords(curr3)
            accounts.assertRecords(acc3)
        }

        outStream.reset()
        Export(mock).withAccounts(listOf(acc1, acc2), true).export(outStream)
        val bytes4 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes4))

        Import.import(ByteArrayInputStream(bytes4)).apply {
            transactions.assertEmpty()
            transactionGroups.assertEmpty()
            contacts.assertEmpty()

            categories.assertRecords(cat1, cat2)
            currencies.assertRecords(curr1)
            accounts.assertRecords(acc1, acc2)
        }
    }

    @Test
    fun testTransactionsWithDependencies() {
        val outStream = ByteArrayOutputStream()

        Export(mock).withTransactions(listOf(tr1, tr2, tr3), true).export(outStream)
        val bytes1 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes1))

        Import.import(ByteArrayInputStream(bytes1)).apply {
            categories.assertRecords(cat1, cat2, cat3)
            accounts.assertRecords(acc1, acc2, acc3)
            currencies.assertRecords(curr1, curr3)
            contacts.assertRecords(con1)
            transactionGroups.assertRecords(tg)
            transactions.assertRecords(tr1, tr2, tr3)
        }

        outStream.reset()
        Export(mock).withTransactions(listOf(tr1), true).export(outStream)
        val bytes2 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes2))

        Import.import(ByteArrayInputStream(bytes2)).apply {
            categories.assertRecords(cat1, cat2)
            accounts.assertRecords(acc1, acc2)
            currencies.assertRecords(curr1)
            contacts.assertRecords(con1)
            transactionGroups.assertEmpty()
            transactions.assertRecords(tr1)
        }

        outStream.reset()
        Export(mock).withTransactions(listOf(tr4), true).export(outStream)
        val bytes3 = outStream.toByteArray()
        validateXML(ByteArrayInputStream(bytes3))

        Import.import(ByteArrayInputStream(bytes3)).apply {
            categories.assertRecords(cat1, cat3)
            accounts.assertRecords(acc1, acc3)
            currencies.assertRecords(curr1, curr3)
            contacts.assertRecords(con2)
            transactionGroups.assertEmpty()
            transactions.assertRecords(tr4)
        }
    }
}