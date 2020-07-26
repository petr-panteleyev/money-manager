/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import org.panteleyev.mysqlapi.annotations.Column;
import org.panteleyev.mysqlapi.annotations.ForeignKey;
import org.panteleyev.mysqlapi.annotations.PrimaryKey;
import org.panteleyev.mysqlapi.annotations.ReferenceOption;
import org.panteleyev.mysqlapi.annotations.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

@Table("account")
public record Account(
    @PrimaryKey
    @Column("uuid")
    UUID uuid,
    @Column("name")
    String name,
    @Column("comment")
    String comment,
    @Column("number")
    String accountNumber,
    @Column("opening")
    BigDecimal openingBalance,
    @Column("account_limit")
    BigDecimal accountLimit,
    @Column("rate")
    BigDecimal currencyRate,
    @Column("type")
    CategoryType type,
    @Column(value = "category_uuid")
    @ForeignKey(table = Category.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID categoryUuid,
    @Column("currency_uuid")
    @ForeignKey(table = Currency.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID currencyUuid,
    @Column("enabled")
    boolean enabled,
    @Column("interest")
    BigDecimal interest,
    @Column("closing_date")
    LocalDate closingDate,
    @Column("icon_uuid")
    @ForeignKey(table = Icon.class, column = "uuid", onDelete = ReferenceOption.SET_NULL)
    UUID iconUuid,
    @Column("card_type")
    CardType cardType,
    @Column("card_number")
    String cardNumber,
    @Column("total")
    BigDecimal total,
    @Column("total_waiting")
    BigDecimal totalWaiting,
    @Column("created")
    long created,
    @Column("modified")
    long modified

) implements MoneyRecord, Named, Comparable<Account> {
    public final static Predicate<Account> FILTER_ENABLED = Account::enabled;

    public Account {
        this.openingBalance = MoneyRecord.normalize(openingBalance);
        this.accountLimit = MoneyRecord.normalize(accountLimit);
        this.currencyRate = MoneyRecord.normalize(currencyRate);
        this.interest = MoneyRecord.normalize(interest);
        this.total = MoneyRecord.normalize(total);
        this.totalWaiting = MoneyRecord.normalize(totalWaiting);
    }

    @Override
    public int compareTo(Account other) {
        return name.compareToIgnoreCase(other.name);
    }

    public Account enable(boolean e) {
        return new Account.Builder(this)
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

    public String getAccountNumberNoSpaces() {
        return accountNumber().replaceAll(" ", "");
    }

    public String getCardNumberNoSpaces() {
        return cardNumber().replaceAll(" ", "");
    }

    public BigDecimal getBalance() {
        return openingBalance.add(accountLimit).add(total);
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
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }

            long now = System.currentTimeMillis();
            if (created == 0) {
                created = now;
            }
            if (modified == 0) {
                modified = now;
            }

            return new Account(uuid, name, comment, accountNumber, openingBalance,
                accountLimit, currencyRate, type, categoryUuid,
                currencyUuid, enabled, interest, closingDate, iconUuid, cardType, cardNumber,
                total, totalWaiting, created, modified);
        }

        public Builder name(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Account name must not be empty");
            }
            this.name = name;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment == null ? "" : comment;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber == null ? "" : accountNumber;
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
            this.cardNumber = cardNumber == null ? "" : cardNumber;
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

        public Builder guid(UUID guid) {
            Objects.requireNonNull(guid);
            this.uuid = guid;
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
