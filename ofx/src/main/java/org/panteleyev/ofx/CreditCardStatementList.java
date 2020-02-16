package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.List;

public class CreditCardStatementList {
    private final List<CreditCardStatement> creditCardStatementList;

    CreditCardStatementList(List<CreditCardStatement> creditCardStatementList) {
        this.creditCardStatementList = creditCardStatementList;
    }

    public List<CreditCardStatement> getCreditCardStatementList() {
        return creditCardStatementList;
    }

    public AccountInfo getAccountInfo() {
        return creditCardStatementList.isEmpty() ?
            new AccountInfo() : creditCardStatementList.get(0).getCreditCardAccountFrom();
    }

    public BankTransactionList getBankTransactionList() {
        return creditCardStatementList.isEmpty() ?
            new BankTransactionList() : creditCardStatementList.get(0).getBankTransactionList();
    }

    public PendingTransactionList getPendingTransactionList() {
        return creditCardStatementList.isEmpty() ?
            new PendingTransactionList() : creditCardStatementList.get(0).getPendingTransactionList();
    }
}
