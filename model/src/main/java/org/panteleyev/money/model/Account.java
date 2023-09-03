/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Account(
        UUID uuid,
        String name,
        String comment,
        String accountNumber,
        BigDecimal openingBalance,
        BigDecimal accountLimit,
        BigDecimal currencyRate,
        CategoryType type,
        UUID categoryUuid,
        UUID currencyUuid,
        UUID securityUuid,
        boolean enabled,
        BigDecimal interest,
        LocalDate closingDate,
        UUID iconUuid,
        CardType cardType,
        String cardNumber,
        BigDecimal total,
        BigDecimal totalWaiting,
        long created,
        long modified
) implements MoneyRecord, Named {
    public Account {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Account name cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalStateException("Account type cannot be null");
        }
        if (categoryUuid == null) {
            throw new IllegalStateException("Account category cannot be null");
        }

        long now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }

        comment = MoneyRecord.normalize(comment);
        accountNumber = MoneyRecord.normalize(accountNumber);
        cardNumber = MoneyRecord.normalize(cardNumber);

        openingBalance = MoneyRecord.normalize(openingBalance, BigDecimal.ZERO);
        accountLimit = MoneyRecord.normalize(accountLimit, BigDecimal.ZERO);
        currencyRate = MoneyRecord.normalize(currencyRate, BigDecimal.ONE);
        interest = MoneyRecord.normalize(interest, BigDecimal.ZERO);
        total = MoneyRecord.normalize(total, BigDecimal.ZERO);
        totalWaiting = MoneyRecord.normalize(totalWaiting, BigDecimal.ZERO);
    }

    public Account enable(boolean e) {
        return new Builder(this)
                .enabled(e)
                .modified(System.currentTimeMillis())
                .build();
    }

    public Account updateBalance(BigDecimal total, BigDecimal totalWaiting) {
        return new Builder(this)
                .total(total)
                .totalWaiting(totalWaiting)
                .modified(System.currentTimeMillis())
                .build();
    }

    public static String getAccountNumberNoSpaces(Account account) {
        return account.accountNumber().replaceAll(" ", "");
    }

    public static String getCardNumberNoSpaces(Account account) {
        return account.cardNumber().replaceAll(" ", "");
    }

    public static BigDecimal getBalance(Account account) {
        return account.openingBalance().add(account.accountLimit()).add(account.total());
    }

    public static final class Builder {
        private String name = "";
        private String comment = "";
        private String accountNumber = "";
        private BigDecimal openingBalance = BigDecimal.ZERO;
        private BigDecimal accountLimit = BigDecimal.ZERO;
        private BigDecimal currencyRate = BigDecimal.ONE;
        private CategoryType type;
        private UUID categoryUuid;
        private UUID currencyUuid;
        private UUID securityUuid;
        private boolean enabled = true;
        private BigDecimal interest = BigDecimal.ZERO;
        private LocalDate closingDate = null;
        private UUID iconUuid = null;
        private CardType cardType = CardType.NONE;
        private String cardNumber = "";
        private BigDecimal total = BigDecimal.ZERO;
        private BigDecimal totalWaiting = BigDecimal.ZERO;
        private UUID uuid = null;
        private long created = 0;
        private long modified = 0;

        public Builder() {
        }

        public Builder(Account account) {
            if (account == null) {
                return;
            }

            name = account.name();
            comment = account.comment();
            accountNumber = account.accountNumber();
            openingBalance = account.openingBalance();
            accountLimit = account.accountLimit();
            currencyRate = account.currencyRate();
            type = account.type();
            categoryUuid = account.categoryUuid();
            currencyUuid = account.currencyUuid();
            securityUuid = account.securityUuid();
            enabled = account.enabled();
            interest = account.interest();
            closingDate = account.closingDate();
            iconUuid = account.iconUuid();
            cardType = account.cardType();
            cardNumber = account.cardNumber();
            uuid = account.uuid();
            created = account.created();
            modified = account.modified();
            total = account.total();
            totalWaiting = account.totalWaiting();
        }

        public UUID getUuid() {
            return uuid;
        }

        public Account build() {
            return new Account(uuid, name, comment, accountNumber, openingBalance,
                    accountLimit, currencyRate, type, categoryUuid,
                    currencyUuid, securityUuid, enabled, interest, closingDate, iconUuid, cardType, cardNumber,
                    total, totalWaiting, created, modified);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder openingBalance(BigDecimal openingBalance) {
            this.openingBalance = openingBalance;
            return this;
        }

        public Builder accountLimit(BigDecimal accountLimit) {
            this.accountLimit = accountLimit;
            return this;
        }

        public Builder currencyRate(BigDecimal currencyRate) {
            this.currencyRate = currencyRate;
            return this;
        }

        public Builder type(CategoryType type) {
            this.type = type;
            return this;
        }

        public Builder categoryUuid(UUID categoryUuid) {
            this.categoryUuid = categoryUuid;
            return this;
        }

        public Builder currencyUuid(UUID currencyUuid) {
            this.currencyUuid = currencyUuid;
            return this;
        }

        public Builder securityUuid(UUID securityUuid) {
            this.securityUuid = securityUuid;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder interest(BigDecimal interest) {
            this.interest = interest;
            return this;
        }

        public Builder closingDate(LocalDate closingDate) {
            this.closingDate = closingDate;
            return this;
        }

        public Builder iconUuid(UUID iconUuid) {
            this.iconUuid = iconUuid;
            return this;
        }

        public Builder cardType(CardType cardType) {
            this.cardType = cardType;
            return this;
        }

        public Builder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder total(BigDecimal total) {
            this.total = total;
            return this;
        }

        public Builder totalWaiting(BigDecimal totalWaiting) {
            this.totalWaiting = totalWaiting;
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
