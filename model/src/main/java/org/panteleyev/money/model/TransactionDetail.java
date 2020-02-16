package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionDetail {
    private final UUID uuid;
    private final BigDecimal amount;
    private final UUID accountCreditedUuid;
    private final String comment;
    private final long modified;

    public TransactionDetail(Transaction transaction) {
        uuid = transaction.getUuid();
        amount = transaction.getAmount();
        accountCreditedUuid = transaction.getAccountCreditedUuid();
        comment = transaction.getComment();
        modified = transaction.getModified();
    }

    private TransactionDetail(UUID uuid, BigDecimal amount, UUID accountCreditedUuid, String comment, long modified) {
        this.uuid = uuid;
        this.amount = amount;
        this.accountCreditedUuid = accountCreditedUuid;
        this.comment = comment;
        this.modified = modified;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getAccountCreditedUuid() {
        return accountCreditedUuid;
    }

    public String getComment() {
        return comment;
    }

    public long getModified() {
        return modified;
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
