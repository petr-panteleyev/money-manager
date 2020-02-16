package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public class AccountStatement {
    private final String currency;
    private final AccountInfo bankAccountFrom;
    private final BankTransactionList bankTransactionList;

    AccountStatement(String currency, AccountInfo bankAccountFrom, BankTransactionList bankTransactionList) {
        this.currency = currency;
        this.bankAccountFrom = bankAccountFrom;
        this.bankTransactionList = bankTransactionList;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountInfo getBankAccountFrom() {
        return bankAccountFrom;
    }

    public BankTransactionList getBankTransactionList() {
        return bankTransactionList;
    }
}
