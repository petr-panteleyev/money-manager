/*
 Copyright © 2020 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.util.List;

public record AccountStatementList(List<AccountStatement> accountStatementList) {
    public AccountInfo getAccountInfo() {
        return accountStatementList.isEmpty() ?
                new AccountInfo() : accountStatementList.get(0).bankAccountFrom();
    }

    public BankTransactionList getBankTransactionList() {
        return accountStatementList.isEmpty() ?
                new BankTransactionList() : accountStatementList.get(0).bankTransactionList();
    }
}
