/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public abstract class ModelTestBase {

    public abstract Object[][] testBuildDataProvider();

    @Test(dataProvider = "testBuildDataProvider")
    public void testBuild(MoneyRecord actual, MoneyRecord expected) {
        assertEquals(actual, expected);
        Assert.assertTrue(actual.created() > 0);
        Assert.assertTrue(actual.modified() >= actual.created());
    }
}
