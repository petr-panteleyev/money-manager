package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.time.LocalDateTime;
import java.util.List;

public class PendingTransactionList {
    private final LocalDateTime dateAsOf;
    private final List<PendingTransaction> transactions;

    PendingTransactionList() {
        this(LocalDateTime.now(), List.of());
    }

    PendingTransactionList(LocalDateTime dateAsOf, List<PendingTransaction> transactions) {
        this.dateAsOf = dateAsOf;
        this.transactions = transactions;
    }

    public LocalDateTime getDateAsOf() {
        return dateAsOf;
    }

    public List<PendingTransaction> getTransactions() {
        return transactions;
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }
}
