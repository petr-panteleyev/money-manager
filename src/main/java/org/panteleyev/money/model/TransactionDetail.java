/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDetail(UUID uuid, BigDecimal amount, UUID accountCreditedUuid, String comment, long modified) {
    public TransactionDetail {
        amount = MoneyRecord.normalize(amount);
    }

    public TransactionDetail(Transaction transaction) {
        this(transaction.uuid(), transaction.amount(),
            transaction.accountCreditedUuid(), transaction.comment(), transaction.modified());
    }

    public static final class Builder {
        private UUID uuid;
        private BigDecimal amount;
        private UUID accountCreditedUuid;
        private String comment;

        public TransactionDetail build() {
            return new TransactionDetail(uuid, amount, accountCreditedUuid, comment, System.currentTimeMillis());
        }

        public Builder(UUID uuid) {
            this.uuid = uuid;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder amount(BigDecimal sum) {
            this.amount = sum;
            return this;
        }

        public Builder accountCreditedUuid(UUID accountCreditedUuid) {
            this.accountCreditedUuid = accountCreditedUuid;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }
    }
}
