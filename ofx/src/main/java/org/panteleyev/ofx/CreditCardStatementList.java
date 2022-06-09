/*
 Copyright © 2020 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

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
                new AccountInfo() : creditCardStatementList.get(0).creditCardAccountFrom();
    }

    public BankTransactionList getBankTransactionList() {
        return creditCardStatementList.isEmpty() ?
                new BankTransactionList() : creditCardStatementList.get(0).bankTransactionList();
    }

    public PendingTransactionList getPendingTransactionList() {
        return creditCardStatementList.isEmpty() ?
                new PendingTransactionList() : creditCardStatementList.get(0).pendingTransactionList();
    }
}
