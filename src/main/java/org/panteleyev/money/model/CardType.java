/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
