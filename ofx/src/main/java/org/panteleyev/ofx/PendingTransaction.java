/*
 Copyright © 2020 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PendingTransaction(TransactionEnum type,
                                 BigDecimal amount,
                                 String name,
                                 String memo,
                                 LocalDateTime dateTransaction,
                                 LocalDateTime dateExpire) {

    static class Builder {
        private TransactionEnum type;
        private BigDecimal amount;
        private String name;
        private String memo;
        private LocalDateTime dateTransaction;
        private LocalDateTime dateExpire;

        PendingTransaction build() {
            return new PendingTransaction(type, amount, name, memo, dateTransaction, dateExpire);
        }

        Builder type(String type) {
            this.type = TransactionEnum.valueOf(type);
            return this;
        }

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder memo(String memo) {
            this.memo = memo;
            return this;
        }

        Builder amount(String amount) {
            this.amount = new BigDecimal(amount);
            return this;
        }

        Builder dateTransaction(LocalDateTime dateTransaction) {
            this.dateTransaction = dateTransaction;
            return this;
        }

        Builder dateExpire(LocalDateTime dateExpire) {
            this.dateExpire = dateExpire;
            return this;
        }
    }
}
