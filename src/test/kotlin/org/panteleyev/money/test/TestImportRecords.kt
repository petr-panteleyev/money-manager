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

import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.xml.Import
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.io.FileInputStream


class TestImportRecords : BaseDaoTest() {
    companion object {
        private const val RESOURCE_DIR = "src/test/resources/org/panteleyev/money/test/TestImportRecords"

        private const val INITIAL = RESOURCE_DIR + "/initial.xml"
        private const val UPDATE = RESOURCE_DIR + "/update.xml"
        private const val EXPECTED = RESOURCE_DIR + "/expected.xml"
    }

    @BeforeClass
    override fun setupAndSkip() {
        try {
            super.setupAndSkip()
            with(MoneyDAO) {
                createTables()
                preload()
            }
        } catch (ex: Exception) {
            throw SkipException("Database not configured")
        }

    }

    @AfterClass
    override fun cleanup() {
        MoneyDAO.dropTables()
        super.cleanup()
    }

    @Test
    fun testImportRecords() {
        val initial: File = File(INITIAL)
        Assert.assertTrue(initial.exists())

        FileInputStream(initial).use {
            MoneyDAO.importFullDump(Import.import(it), {})
        }

        MoneyDAO.preload()
        compareDatabase(INITIAL)

        val update: File = File(UPDATE)
        Assert.assertTrue(update.exists())

        FileInputStream(update).use {
            MoneyDAO.importRecords(Import.import(it), {})
        }

        MoneyDAO.preload()
        compareDatabase(EXPECTED)
    }

    private fun compareDatabase(expected: String) {
        val file = File(expected)
        Assert.assertTrue(file.exists())

        FileInputStream(file).use {
            val imp = Import.import(it)

            val categories = MoneyDAO.getCategories()
            val accounts = MoneyDAO.getAccounts()
            val currencies = MoneyDAO.getCurrencies()
            val contacts = MoneyDAO.getContacts()
            val transactionGroups = MoneyDAO.getTransactionGroups()
            val transactions = MoneyDAO.getTransactions()

            Assert.assertEquals(categories.sortedBy { it.id }, imp.categories.sortedBy { it.id })
            Assert.assertEquals(accounts.sortedBy { it.id }, imp.accounts.sortedBy { it.id })
            Assert.assertEquals(currencies.sortedBy { it.id }, imp.currencies.sortedBy { it.id })
            Assert.assertEquals(contacts.sortedBy { it.id }, imp.contacts.sortedBy { it.id })
            Assert.assertEquals(transactionGroups.sortedBy { it.id }, imp.transactionGroups.sortedBy { it.id })
            Assert.assertEquals(transactions.sortedBy { it.id }, imp.transactions.sortedBy { it.id })
        }
    }
}