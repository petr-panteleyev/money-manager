/*
 Copyright © 2020-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
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
                new AccountInfo() : creditCardStatementList.getFirst().creditCardAccountFrom();
    }

    public BankTransactionList getBankTransactionList() {
        return creditCardStatementList.isEmpty() ?
                new BankTransactionList() : creditCardStatementList.getFirst().bankTransactionList();
    }

    public PendingTransactionList getPendingTransactionList() {
        return creditCardStatementList.isEmpty() ?
                new PendingTransactionList() : creditCardStatementList.getFirst().pendingTransactionList();
    }
}
