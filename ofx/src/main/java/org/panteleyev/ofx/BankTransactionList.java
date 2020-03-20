package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.time.LocalDateTime;
import java.util.List;

public record BankTransactionList(LocalDateTime dateStart,
                                  LocalDateTime dateEnd,
                                  List<StatementTransaction>transactions)
{

    public BankTransactionList() {
        this(LocalDateTime.now(), LocalDateTime.now(), List.of());
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }
}
