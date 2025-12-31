// Copyright © 2019-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.model;

import java.util.Arrays;
import java.util.List;

public enum CardType {
    NONE("-"),
    VISA("VISA"),
    MASTERCARD("MasterCard"),
    MIR("Мир"),
    AMEX("American Express");

    private final String typeName;

    CardType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public static List<CardType> asList() {
        return Arrays.asList(values());
    }
}
