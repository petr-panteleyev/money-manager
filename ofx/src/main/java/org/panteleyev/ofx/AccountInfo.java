package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public class AccountInfo {
    public enum Type {
        NONE,
        CHECKING,
        SAVINGS,
        MONEY_MARKET,
        CREDIT_LINE
    }

    private final Type type;
    private final String bankId;
    private final String accountNumber;

    AccountInfo() {
        this(Type.NONE, "", "");
    }

    AccountInfo(Type type, String bankId, String accountNumber) {
        this.type = type;
        this.bankId = bankId;
        this.accountNumber = accountNumber;
    }

    public Type getType() {
        return type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankId() {
        return bankId;
    }
}
