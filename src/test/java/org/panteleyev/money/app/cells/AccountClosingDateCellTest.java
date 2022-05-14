/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.cells;

import javafx.embed.swing.JFXPanel;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.panteleyev.money.app.Styles.EXPIRED;
import static org.testng.Assert.assertEquals;

public class AccountClosingDateCellTest {
    private static final int DELTA = 10;

    @BeforeClass
    public void initFx() {
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
            .uuid(UUID.randomUUID())
            .name(UUID.randomUUID().toString())
            .categoryUuid(UUID.randomUUID())
            .type(CategoryType.BANKS_AND_CASH)
            .closingDate(today.plusDays(delta))
            .build();

        cell.updateItem(account, false);
        assertEquals(cell.getStyleClass().contains(EXPIRED), hasRedColor);
    }
}
