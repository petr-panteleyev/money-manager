/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.util.List;

public enum TransactionType {
    CARD_PAYMENT,
    SBP_PAYMENT,
    CASH_PURCHASE,
    CHEQUE,
    S1(true),
    WITHDRAWAL,
    CACHIER,
    DEPOSIT,
    TRANSFER,
    SBP_TRANSFER,
    S2(true),
    INTEREST,
    DIVIDEND,
    S3(true),
    DIRECT_BILLING,
    CHARGE,
    FEE,
    S4(true),
    INCOME,
    PURCHASE,
    SALE,
    S5(true),
    REFUND,
    UNDEFINED;

    private final boolean separator;

    TransactionType() {
        this(false);
    }

    TransactionType(boolean separator) {
        this.separator = separator;
    }

    public boolean isSeparator() {
        return separator;
    }

    public static List<TransactionType> valuesAsList() {
        return List.of(TransactionType.values());
    }
}
