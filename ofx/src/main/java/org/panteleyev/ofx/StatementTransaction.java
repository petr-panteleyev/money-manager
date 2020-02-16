package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class StatementTransaction {
    private final TransactionEnum type;
    private final LocalDateTime datePosted;
    private final LocalDateTime dateUser;
    private final LocalDateTime dateAvailable;
    private final BigDecimal amount;
    private final String name;
    private final String memo;
    private final String checkNum;

    StatementTransaction(TransactionEnum type, LocalDateTime datePosted, LocalDateTime dateUser,
                                LocalDateTime dateAvailable, BigDecimal amount, String name, String memo,
                                String checkNum) {
        this.type = type;
        this.datePosted = datePosted;
        this.dateUser = dateUser;
        this.dateAvailable = dateAvailable;
        this.amount = amount;
        this.name = name;
        this.memo = memo;
        this.checkNum = checkNum;
    }

    public LocalDateTime getDatePosted() {
        return datePosted;
    }

    public LocalDateTime getDateUser() {
        return dateUser;
    }

    public LocalDateTime getDateAvailable() {
        return dateAvailable;
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

    public String getCheckNum() {
        return checkNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof StatementTransaction)) {
            return false;
        }

        StatementTransaction that = (StatementTransaction)obj;

        return Objects.equals(this.type, that.type)
            && Objects.equals(this.amount, that.amount)
            && Objects.equals(this.checkNum, that.checkNum)
            && Objects.equals(this.dateAvailable, that.dateAvailable)
            && Objects.equals(this.datePosted, that.datePosted)
            && Objects.equals(this.dateUser, that.dateUser)
            && Objects.equals(this.name, that.name)
            && Objects.equals(this.memo, that.memo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, checkNum, dateAvailable, datePosted, dateUser, name, memo);
    }

    public static class Builder {
        private TransactionEnum type;
        private LocalDateTime datePosted;
        private LocalDateTime dateUser;
        private LocalDateTime dateAvailable;
        private BigDecimal amount;
        private String name;
        private String memo;
        private String checkNum;

        public StatementTransaction build() {
            return new StatementTransaction(
                    type,
                    datePosted,
                    dateUser,
                    dateAvailable,
                    amount,
                    name,
                    memo,
                    checkNum
            );
        }

        public Builder datePosted(LocalDateTime datePosted) {
            this.datePosted = datePosted;
            return this;
        }

        public Builder dateUser(LocalDateTime dateUser) {
            this.dateUser = dateUser;
            return this;
        }

        public Builder dateAvailable(LocalDateTime dateAvailable) {
            this.dateAvailable = dateAvailable;
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

        Builder type(String type) {
            this.type = TransactionEnum.valueOf(type);
            return this;
        }

        Builder checkNum(String checkNum) {
            this.checkNum = checkNum;
            return this;
        }
    }
}
