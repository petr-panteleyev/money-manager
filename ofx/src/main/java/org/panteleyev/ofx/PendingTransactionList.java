/*
 Copyright © 2020 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.time.LocalDateTime;
import java.util.List;

public record PendingTransactionList(LocalDateTime dateAsOf, List<PendingTransaction> transactions) {

    PendingTransactionList() {
        this(LocalDateTime.now(), List.of());
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }
}
