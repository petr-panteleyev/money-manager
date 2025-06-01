/*
 Copyright © 2020 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatementTransaction(
        TransactionEnum type,
        LocalDateTime datePosted,
        LocalDateTime dateUser,
        LocalDateTime dateAvailable,
        BigDecimal amount,
        String name,
        String memo,
        String checkNum) {

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
