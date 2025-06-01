/*
 Copyright © 2019-2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

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
}
