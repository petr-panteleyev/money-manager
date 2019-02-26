/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.ofx;

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
