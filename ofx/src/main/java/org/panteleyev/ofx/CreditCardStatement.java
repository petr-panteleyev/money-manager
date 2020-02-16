package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public class CreditCardStatement {
    private final String currency;
    private final AccountInfo creditCardAccountFrom;
    private final BankTransactionList bankTransactionList;
    private final PendingTransactionList pendingTransactionList;

    CreditCardStatement(String currency,
                        AccountInfo creditCardAccountFrom,
                        BankTransactionList bankTransactionList,
                        PendingTransactionList pendingTransactionList) {
        this.currency = currency;
        this.creditCardAccountFrom = creditCardAccountFrom;
        this.bankTransactionList = bankTransactionList;
        this.pendingTransactionList = pendingTransactionList;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountInfo getCreditCardAccountFrom() {
        return creditCardAccountFrom;
    }

    public BankTransactionList getBankTransactionList() {
        return bankTransactionList;
    }

    public PendingTransactionList getPendingTransactionList() {
        return pendingTransactionList;
    }
}
