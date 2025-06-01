/*
 Copyright © 2020-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.util.List;

public record AccountStatementList(List<AccountStatement> accountStatementList) {
    public AccountInfo getAccountInfo() {
        return accountStatementList.isEmpty() ?
                new AccountInfo() : accountStatementList.getFirst().bankAccountFrom();
    }

    public BankTransactionList getBankTransactionList() {
        return accountStatementList.isEmpty() ?
                new BankTransactionList() : accountStatementList.getFirst().bankTransactionList();
    }
}
