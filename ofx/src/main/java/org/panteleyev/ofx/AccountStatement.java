package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public record AccountStatement(String currency, AccountInfo bankAccountFrom, BankTransactionList bankTransactionList) {
}
