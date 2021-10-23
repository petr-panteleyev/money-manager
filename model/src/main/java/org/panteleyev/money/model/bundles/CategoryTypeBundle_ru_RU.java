/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model.bundles;

import java.util.ListResourceBundle;

public class CategoryTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
            // Names
            {"BANKS_AND_CASH_name", "Банки"},
            {"INCOMES_name", "Доходы"},
            {"EXPENSES_name", "Расходы"},
            {"DEBTS_name", "Долги"},
            {"PORTFOLIO_name", "Портфель"},
            {"ASSETS_name", "Активы"},
            {"STARTUP_name", "Стартап"},
            // Comments
            {"BANKS_AND_CASH_comment", "Текущие, сберегательные счета и наличность"},
            {"INCOMES_comment", "Статьи доходов"},
            {"EXPENSES_comment", "Статьи расходов"},
            {"DEBTS_comment", "Кредиты, рефинансирование и кредитные карты"},
            {"PORTFOLIO_comment", "Стоки, акции и прочие счета"},
            {"ASSETS_comment", "Недвижимость"},
            {"STARTUP_comment", ""},
        };
    }
}
