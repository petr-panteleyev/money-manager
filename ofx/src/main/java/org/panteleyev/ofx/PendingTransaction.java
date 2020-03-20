package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public record PendingTransaction(TransactionEnum type,
                                 BigDecimal amount,
                                 String name,
                                 String memo,
                                 LocalDateTime dateTransaction,
                                 LocalDateTime dateExpire)
{

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
