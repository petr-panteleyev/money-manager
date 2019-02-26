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

package org.panteleyev.money.details;

import org.panteleyev.money.persistence.model.Transaction;
import java.math.BigDecimal;

public class TransactionDetail {
    private final int id;
    private final BigDecimal amount;
    private final int accountCreditedId;
    private final String comment;
    private final long modified;

    TransactionDetail(Transaction transaction) {
        id = transaction.getId();
        amount = transaction.getAmount();
        accountCreditedId = transaction.getAccountCreditedId();
        comment = transaction.getComment();
        modified = transaction.getModified();
    }

    private TransactionDetail(int id, BigDecimal amount, int accountCreditedId, String comment, long modified) {
        this.id = id;
        this.amount = amount;
        this.accountCreditedId = accountCreditedId;
        this.comment = comment;
        this.modified = modified;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getAccountCreditedId() {
        return accountCreditedId;
    }

    public String getComment() {
        return comment;
    }

    public long getModified() {
        return modified;
    }

    public static final class Builder {
        private int id;
        private BigDecimal amount;
        private int accountCreditedId;
        private String comment;

        public TransactionDetail build() {
            return new TransactionDetail(id, amount, accountCreditedId, comment, System.currentTimeMillis());
        }

        public Builder(int id) {
            this.id = id;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder amount(BigDecimal sum) {
            this.amount = sum;
            return this;
        }

        public Builder accountCreditedId(int accountCreditedId) {
            this.accountCreditedId = accountCreditedId;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }
    }
}
