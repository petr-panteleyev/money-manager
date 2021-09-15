/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class TransactionPredicateBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
            {"ALL", "Все проводки"},
            {"CURRENT_MONTH", "Текущий месяц"},
            {"CURRENT_WEEK", "Текущая неделя"},
            {"CURRENT_YEAR", "Текущий год"},
            {"LAST_MONTH", "Последний месяц"},
            {"LAST_QUARTER", "Последний квартал"},
            {"LAST_YEAR", "Последний год"},
        };
    }
}
