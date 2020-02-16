package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.Arrays;
import java.util.List;
import static org.panteleyev.money.model.Bundles.TRANSACTION_TYPE_BUNDLE;

public enum TransactionType {
    CARD_PAYMENT(1),
    CASH_PURCHASE(2),
    CHEQUE(3),
    S1(4, true),
    WITHDRAWAL(5),
    CACHIER(6),
    DEPOSIT(7),
    TRANSFER(8),
    S2(9, true),
    INTEREST(10),
    DIVIDEND(11),
    S3(12, true),
    DIRECT_BILLING(13),
    CHARGE(14),
    FEE(15),
    S4(16, true),
    INCOME(17),
    SALE(18),
    S5(19, true),
    REFUND(20),
    UNDEFINED(21);

    private final int id;
    private final boolean separator;
    private final String typeName;

    TransactionType(int id) {
        this(id, false);
    }

    TransactionType(int id, boolean separator) {
        this.id = id;
        this.separator = separator;

        if (separator) {
            typeName = "";
        } else {
            typeName = TRANSACTION_TYPE_BUNDLE.getString("name" + id);
        }
    }

    public int getId() {
        return id;
    }

    public boolean isSeparator() {
        return separator;
    }

    public String getTypeName() {
        return typeName;
    }

    public static TransactionType get(int id) {
        return Arrays.stream(TransactionType.values())
            .filter(t -> t.id == id)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    public static List<TransactionType> valuesAsList() {
        return List.of(TransactionType.values());
    }
}
