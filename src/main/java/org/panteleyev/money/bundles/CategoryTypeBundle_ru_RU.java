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
