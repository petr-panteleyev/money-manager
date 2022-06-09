/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class TransactionPredicateBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"ALL", "All transactions"},
                {"CURRENT_MONTH", "Current month"},
                {"CURRENT_WEEK", "Current week"},
                {"CURRENT_YEAR", "Current year"},
                {"LAST_MONTH", "Last month"},
                {"LAST_QUARTER", "Last quarter"},
                {"LAST_YEAR", "Last year"},
        };
    }
}
