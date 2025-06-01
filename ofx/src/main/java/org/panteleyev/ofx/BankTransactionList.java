/*
 Copyright © 2020 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.time.LocalDateTime;
import java.util.List;

public record BankTransactionList(LocalDateTime dateStart,
                                  LocalDateTime dateEnd,
                                  List<StatementTransaction> transactions) {

    public BankTransactionList() {
        this(LocalDateTime.now(), LocalDateTime.now(), List.of());
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }
}
