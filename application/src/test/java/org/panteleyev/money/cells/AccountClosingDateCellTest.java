/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.cells;

import javafx.embed.swing.JFXPanel;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.time.LocalDate;
import static org.panteleyev.money.Styles.RED_TEXT;
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
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .closingDate(today.plusDays(delta))
            .build();

        cell.updateItem(account, false);
        assertEquals(cell.getStyleClass().contains(RED_TEXT), hasRedColor);
    }
}
