/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.model;

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
