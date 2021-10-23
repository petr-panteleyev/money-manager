/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model.bundles;

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
