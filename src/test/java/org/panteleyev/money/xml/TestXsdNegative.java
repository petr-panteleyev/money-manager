/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.xml;

import org.panteleyev.money.test.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.FileInputStream;

/**
 * This test covers various validation violations.
 */
public class TestXsdNegative extends BaseTest implements XsdUtil {
    private final static String RESOURCES = "src/test/resources/org/panteleyev/money/xml/TestXsdNegative/";

    @DataProvider(name = "dataProvider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"noAccounts.xml"},
                {"noCategories.xml"},
                {"noContacts.xml"},
                {"noCurrencies.xml"},
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

                // transaction
                {"transaction/notUniqueId.xml"},
                {"transaction/notUniqueUUID.xml"}
        };
    }

    @Test(dataProvider = "dataProvider")
    public void negativeXsdTest(String fileName) throws Exception {
        try (var input = new FileInputStream(new File(RESOURCES + fileName))) {
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
