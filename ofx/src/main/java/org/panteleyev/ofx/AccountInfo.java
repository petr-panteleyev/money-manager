package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public record AccountInfo(Type type, String bankId, String accountNumber) {
    public enum Type {
        NONE,
        CHECKING,
        SAVINGS,
        MONEY_MARKET,
        CREDIT_LINE
    }

    AccountInfo() {
        this(Type.NONE, "", "");
    }
}
