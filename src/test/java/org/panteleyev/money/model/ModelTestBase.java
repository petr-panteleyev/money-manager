/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public abstract class ModelTestBase extends BaseTest {

    public abstract Object[][] testBuildDataProvider();

    @Test(dataProvider = "testBuildDataProvider")
    public void testBuild(MoneyRecord actual, MoneyRecord expected) {
        assertEquals(actual, expected);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }
}
