package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public enum CardType {
    NONE("-"),
    VISA("VISA"),
    MASTERCARD("MasterCard"),
    MIR("Мир"),
    AMEX("American Express");

    private String typeName;

    CardType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
