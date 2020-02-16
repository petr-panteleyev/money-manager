package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class PendingTransaction {
    private final TransactionEnum type;
    private final BigDecimal amount;
    private final String name;
    private final String memo;
    private final LocalDateTime dateTransaction;
    private final LocalDateTime dateExpire;

    PendingTransaction(TransactionEnum type, BigDecimal amount, String name, String memo,
                       LocalDateTime dateTransaction, LocalDateTime dateExpire)
    {
        this.type = type;
        this.amount = amount;
        this.name = name;
        this.memo = memo;
        this.dateTransaction = dateTransaction;
        this.dateExpire = dateExpire;
    }

    public TransactionEnum getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public Optional<LocalDateTime> getDateExpire() {
        return Optional.ofNullable(dateExpire);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PendingTransaction)) {
            return false;
        }

        PendingTransaction that = (PendingTransaction) obj;
        return Objects.equals(this.type, that.type)
            && Objects.equals(this.amount, that.amount)
            && Objects.equals(this.name, that.name)
            && Objects.equals(this.memo, that.memo)
            && Objects.equals(this.dateTransaction, that.dateTransaction)
            && Objects.equals(this.dateExpire, that.dateExpire);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, name, memo, dateTransaction, dateExpire);
    }

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
