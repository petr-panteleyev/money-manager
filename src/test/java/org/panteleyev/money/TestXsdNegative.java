/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * This test covers various validation violations.
 */
public class TestXsdNegative extends BaseTest {
    private final static String RESOURCES = "src/test/resources/org/panteleyev/money/test/TestXsdNegative/";

    @DataProvider(name = "dataProvider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"noAccounts.xml"},
                {"noCategories.xml"},
                {"noContacts.xml"},
                {"noCurrencies.xml"},
                {"noTransactionGroups.xml"},
                {"noTransactions.xml"},

                // account
                {"account/invalidId.xml"},
                {"account/notUniqueId.xml"},
                {"account/notUniqueUUID.xml"},

                // category
                {"category/notUniqueId.xml"},
                {"category/notUniqueUUID.xml"},

                // contact
                {"contact/notUniqueId.xml"},
                {"contact/notUniqueUUID.xml"},

                // currency
                {"currency/notUniqueId.xml"},
                {"currency/notUniqueUUID.xml"},

                // transaction group
                {"transactionGroup/notUniqueId.xml"},
                {"transactionGroup/notUniqueUUID.xml"},

                // transaction
                {"transaction/notUniqueId.xml"},
                {"transaction/notUniqueUUID.xml"}
        };
    }

    @Test(dataProvider = "dataProvider")
    public void negativeXsdTest(String fileName) throws Exception {
        try (InputStream input = new FileInputStream(new File(RESOURCES + fileName))) {
            boolean caught = false;

            try {
                validateXML(input);
            } catch (SAXParseException ex) {
                caught = true;
            }

            Assert.assertTrue(caught);
        }
    }
}
