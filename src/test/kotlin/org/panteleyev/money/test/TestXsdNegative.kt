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

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.xml.sax.SAXParseException
import java.io.File
import java.io.FileInputStream

/**
 * This test covers various validation violations.
 */
class TestXsdNegative : BaseTest() {
    companion object {
         const val RESOURCES = "src/test/resources/org/panteleyev/money/test/TestXsdNegative/"
    }

    @DataProvider(name = "dataProvider")
    fun dataProvider() : Array<Array<String>> {
        return arrayOf(
                arrayOf("noAccounts.xml"),
                arrayOf("noCategories.xml"),
                arrayOf("noContacts.xml"),
                arrayOf("noCurrencies.xml"),
                arrayOf("noTransactionGroups.xml"),
                arrayOf("noTransactions.xml"),

                // account
                arrayOf("account/invalidId.xml"),
                arrayOf("account/notUniqueId.xml"),
                arrayOf("account/notUniqueUUID.xml"),

                // category
                arrayOf("category/notUniqueId.xml"),
                arrayOf("category/notUniqueUUID.xml"),

                // contact
                arrayOf("contact/notUniqueId.xml"),
                arrayOf("contact/notUniqueUUID.xml"),

                // currency
                arrayOf("currency/notUniqueId.xml"),
                arrayOf("currency/notUniqueUUID.xml"),

                // transaction group
                arrayOf("transactionGroup/notUniqueId.xml"),
                arrayOf("transactionGroup/notUniqueUUID.xml"),

                // transaction
                arrayOf("transaction/notUniqueId.xml"),
                arrayOf("transaction/notUniqueUUID.xml")
        )
    }

    @Test(dataProvider = "dataProvider")
    fun negativeXsdTest(fileName: String) {
        FileInputStream(File(RESOURCES + fileName)).use { input ->
            var caught = false
            try {
                validateXML(input)
            } catch (ex: SAXParseException) {
                caught = true
            }
            Assert.assertTrue(caught)
        }
    }
}