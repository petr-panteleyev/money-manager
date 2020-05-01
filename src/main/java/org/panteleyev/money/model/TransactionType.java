package org.panteleyev.money.model;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.List;
import static org.panteleyev.money.model.Bundles.TRANSACTION_TYPE_BUNDLE;

public enum TransactionType {
    CARD_PAYMENT,
    CASH_PURCHASE,
    CHEQUE,
    S1(true),
    WITHDRAWAL,
    CACHIER,
    DEPOSIT,
    TRANSFER,
    S2(true),
    INTEREST,
    DIVIDEND,
    S3(true),
    DIRECT_BILLING,
    CHARGE,
    FEE,
    S4(true),
    INCOME,
    SALE,
    S5(true),
    REFUND,
    UNDEFINED;

    private final boolean separator;
    private final String typeName;

    TransactionType() {
        this(false);
    }

    TransactionType(boolean separator) {
        this.separator = separator;

        if (separator) {
            typeName = "";
        } else {
            typeName = TRANSACTION_TYPE_BUNDLE.getString(name());
        }
    }

    public boolean isSeparator() {
        return separator;
    }

    public String getTypeName() {
        return typeName;
    }

    public static List<TransactionType> valuesAsList() {
        return List.of(TransactionType.values());
    }
}
