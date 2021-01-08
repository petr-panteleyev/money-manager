/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.embed.swing.JFXPanel;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.time.LocalDate;
import static org.panteleyev.money.app.Styles.EXPIRED;
import static org.testng.Assert.assertEquals;

public class AccountClosingDateCellTest {
    private static final int DELTA = 10;

    @BeforeClass
    public static void initFx() {
        new JFXPanel();
    }

    @DataProvider
    public Object[][] testCellColorDataProvider() {
        return new Object[][]{
            {0, true},
            {9, true},
            {-1, true},
            {10, false},
            {20, false},
            {31, false},
            {90, false},
            {200, false},
        };
    }

    @Test(dataProvider = "testCellColorDataProvider")
    public void testCellColor(int delta, boolean hasRedColor) {
        var cell = new AccountClosingDateCell(DELTA);

        var today = LocalDate.now();
        var account = new Account.Builder()
            .type(CategoryType.BANKS_AND_CASH)
            .closingDate(today.plusDays(delta))
            .build();

        cell.updateItem(account, false);
        assertEquals(cell.getStyleClass().contains(EXPIRED), hasRedColor);
    }
}
