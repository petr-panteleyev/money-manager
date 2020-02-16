package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.Arrays;
import static org.panteleyev.money.model.Bundles.CATEGORY_TYPE_BUNDLE;

public enum CategoryType {
    BANKS_AND_CASH(1),
    INCOMES(2),
    EXPENSES(3),
    DEBTS(4),
    PORTFOLIO(5),
    ASSETS(6),
    STARTUP(7);

    private final int id;
    private final String typeName;
    private final String comment;

    CategoryType(int id) {
        this.id = id;

        typeName = CATEGORY_TYPE_BUNDLE.getString("name" + id);
        comment = CATEGORY_TYPE_BUNDLE.getString("comment" + id);
    }

    public int getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getComment() {
        return comment;
    }

    public static CategoryType get(int id) {
        return Arrays.stream(CategoryType.values())
            .filter(t -> t.id == id)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
