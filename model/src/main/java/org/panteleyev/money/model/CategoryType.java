// Copyright © 2017-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.model;

import java.util.Arrays;
import java.util.List;

public enum CategoryType {
    BANKS_AND_CASH,
    INCOMES,
    EXPENSES,
    DEBTS,
    PORTFOLIO,
    ASSETS,
    STARTUP;

    public static List<CategoryType> asList() {
        return Arrays.asList(values());
    }
}
