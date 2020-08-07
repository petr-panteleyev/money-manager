/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import static org.panteleyev.money.model.Bundles.CATEGORY_TYPE_BUNDLE;

public enum CategoryType {
    BANKS_AND_CASH,
    INCOMES,
    EXPENSES,
    DEBTS,
    PORTFOLIO,
    ASSETS,
    STARTUP;

    private final String typeName;
    private final String comment;

    CategoryType() {
        typeName = CATEGORY_TYPE_BUNDLE.getString(name() + "_name");
        comment = CATEGORY_TYPE_BUNDLE.getString(name() + "_comment");
    }

    @Override
    public String toString() {
        return typeName;
    }

    public String getComment() {
        return comment;
    }
}
