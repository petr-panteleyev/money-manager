package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.time.LocalDateTime;
import java.util.List;

public class BankTransactionList {
    private final LocalDateTime dateStart;
    private final LocalDateTime dateEnd;
    private final List<StatementTransaction> transactionList;

    public BankTransactionList() {
        this(LocalDateTime.now(), LocalDateTime.now(), List.of());
    }

    BankTransactionList(LocalDateTime dateStart, LocalDateTime dateEnd, List<StatementTransaction> transactionList) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.transactionList = transactionList;
    }

    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public List<StatementTransaction> getTransactions() {
        return transactionList;
    }

    public boolean isEmpty() {
        return transactionList.isEmpty();
    }
}
