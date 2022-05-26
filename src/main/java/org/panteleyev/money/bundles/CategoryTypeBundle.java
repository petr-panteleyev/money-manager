/*
 Copyright (C) 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class CategoryTypeBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                // Names
                {"BANKS_AND_CASH_name", "Banks"},
                {"INCOMES_name", "Incomes"},
                {"EXPENSES_name", "Expenses"},
                {"DEBTS_name", "Debts"},
                {"PORTFOLIO_name", "Portfolio"},
                {"ASSETS_name", "Assets"},
                {"STARTUP_name", "Startup"},
                // Comments
                {"BANKS_AND_CASH_comment", "Current and savings accounts, cash"},
                {"INCOMES_comment", "Incomes"},
                {"EXPENSES_comment", "Expenses"},
                {"DEBTS_comment", "Credits, credit cards, etc."},
                {"PORTFOLIO_comment", "Portfolio"},
                {"ASSETS_comment", "Assets"},
                {"STARTUP_comment", ""},
        };
    }
}
