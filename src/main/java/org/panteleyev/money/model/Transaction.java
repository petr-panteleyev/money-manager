/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public record Transaction(
    UUID uuid,
    BigDecimal amount,
    int day,
    int month,
    int year,
    TransactionType type,
    String comment,
    boolean checked,
    UUID accountDebitedUuid,
    UUID accountCreditedUuid,
    CategoryType accountDebitedType,
    CategoryType accountCreditedType,
    UUID accountDebitedCategoryUuid,
    UUID accountCreditedCategoryUuid,
    UUID contactUuid,
    BigDecimal rate,
    int rateDirection,
    String invoiceNumber,
    UUID parentUuid,
    boolean detailed,
    LocalDate statementDate,
    long created,
    long modified
) implements MoneyRecord {

    public Transaction {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (amount == null) {
            throw new IllegalStateException("Transaction amount cannot be null");
        }
        if (accountDebitedUuid == null) {
            throw new IllegalStateException("Debited account id cannot be null");
        }
        if (accountCreditedUuid == null) {
            throw new IllegalStateException("Credited account id cannot be null");
        }
        if (accountDebitedType == null) {
            throw new IllegalStateException("Debited account type cannot be null");
        }
        if (accountCreditedType == null) {
            throw new IllegalStateException("Credited account type cannot be null");
        }
        if (accountDebitedCategoryUuid == null) {
            throw new IllegalStateException("Debited account category id cannot be null");
        }
        if (accountCreditedCategoryUuid == null) {
            throw new IllegalStateException("Credited account category id cannot be null");
        }

        if (type == null) {
            type = TransactionType.UNDEFINED;
        }

        comment = MoneyRecord.normalize(comment);
        invoiceNumber = MoneyRecord.normalize(invoiceNumber);

        amount = MoneyRecord.normalize(amount, BigDecimal.ZERO);
        rate = MoneyRecord.normalize(rate, BigDecimal.ONE);

        if (statementDate == null) {
            statementDate = LocalDate.of(year, month, day);
        }

        long now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

    public BigDecimal getSignedAmount() {
        return accountCreditedType != accountDebitedType && accountDebitedType != CategoryType.INCOMES ?
            amount.negate() : amount;
    }

    public Transaction check(boolean check) {
        return new Builder(this)
            .checked(check)
            .modified(System.currentTimeMillis())
            .build();
    }

    public Transaction setParentUuid(UUID newParentUuid) {
        return new Builder(this)
            .parentUuid(newParentUuid)
            .modified(System.currentTimeMillis())
            .build();
    }

    public LocalDate getDate() {
        return LocalDate.of(year, month, day);
    }

    /**
     * Returns amount with conversion rate applied to it.
     *
     * @return converted amount
     */
    public BigDecimal getConvertedAmount() {
        if (rate.compareTo(BigDecimal.ZERO) == 0 || rate.compareTo(BigDecimal.ONE) == 0) {
            return amount;
        }
        return rateDirection == 0 ?
            amount.divide(rate, RoundingMode.HALF_UP) :
            amount.multiply(rate);
    }

    public BigDecimal getNegatedAmount() {
        return amount.negate();
    }

    public static final class Builder {
        private BigDecimal amount = BigDecimal.ZERO;
        private int day;
        private int month;
        private int year;
        private TransactionType type = TransactionType.UNDEFINED;
        private String comment = "";
        private boolean checked;
        private UUID accountDebitedUuid;
        private UUID accountCreditedUuid;
        private CategoryType accountDebitedType;
        private CategoryType accountCreditedType;
        private UUID accountDebitedCategoryUuid;
        private UUID accountCreditedCategoryUuid;
        private UUID contactUuid;
        private BigDecimal rate = BigDecimal.ONE;
        private int rateDirection;
        private String invoiceNumber = "";
        private long created = 0;
        private long modified = 0;
        private UUID uuid;
        private UUID parentUuid;
        private boolean detailed = false;
        private String newContactName;
        private LocalDate statementDate;

        public Builder() {
        }

        public Builder(Transaction t) {
            if (t == null) {
                return;
            }

            this.amount = t.amount();
            this.day = t.day();
            this.month = t.month();
            this.year = t.year();
            this.type = t.type();
            this.comment = t.comment();
            this.checked = t.checked();
            this.accountDebitedUuid = t.accountDebitedUuid();
            this.accountCreditedUuid = t.accountCreditedUuid();
            this.accountDebitedType = t.accountDebitedType();
            this.accountCreditedType = t.accountCreditedType();
            this.accountDebitedCategoryUuid = t.accountDebitedCategoryUuid();
            this.accountCreditedCategoryUuid = t.accountCreditedCategoryUuid();
            this.contactUuid = t.contactUuid();
            this.rate = t.rate();
            this.rateDirection = t.rateDirection();
            this.invoiceNumber = t.invoiceNumber();
            this.created = t.created();
            this.modified = t.modified();
            this.parentUuid = t.parentUuid();
            this.detailed = t.detailed();
            this.uuid = t.uuid();
            this.statementDate = t.statementDate();
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public String getNewContactName() {
            return newContactName;
        }

        public UUID getAccountDebitedUuid() {
            return this.accountDebitedUuid;
        }

        public UUID getAccountCreditedUuid() {
            return this.accountCreditedUuid;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder day(int day) {
            this.day = day;
            return this;
        }

        public Builder month(int month) {
            this.month = month;
            return this;
        }

        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder accountDebitedUuid(UUID uuid) {
            this.accountDebitedUuid = uuid;
            return this;
        }

        public Builder accountCreditedUuid(UUID uuid) {
            this.accountCreditedUuid = uuid;
            return this;
        }

        public Builder accountDebitedType(CategoryType type) {
            this.accountDebitedType = type;
            return this;
        }

        public Builder accountCreditedType(CategoryType type) {
            this.accountCreditedType = type;
            return this;
        }

        public Builder accountDebitedCategoryUuid(UUID id) {
            this.accountDebitedCategoryUuid = id;
            return this;
        }

        public Builder accountCreditedCategoryUuid(UUID id) {
            this.accountCreditedCategoryUuid = id;
            return this;
        }

        public Builder contactUuid(UUID id) {
            this.contactUuid = id;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate == null ? BigDecimal.ONE : rate;
            return this;
        }

        public Builder rateDirection(int rateDirection) {
            this.rateDirection = rateDirection;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
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

        public Builder timestamp() {
            this.modified = System.currentTimeMillis();
            return this;
        }

        public Builder parentUuid(UUID parentUuid) {
            this.parentUuid = parentUuid;
            return this;
        }

        public Builder detailed(boolean detailed) {
            this.detailed = detailed;
            return this;
        }

        public Builder newContactName(String newContactName) {
            this.newContactName = newContactName;
            return this;
        }

        public Builder statementDate(LocalDate statementDate) {
            this.statementDate = statementDate;
            return this;
        }

        public Transaction build() {
            return new Transaction(uuid, amount, day, month, year, type, comment,
                checked, accountDebitedUuid, accountCreditedUuid,
                accountDebitedType, accountCreditedType,
                accountDebitedCategoryUuid, accountCreditedCategoryUuid, contactUuid,
                rate, rateDirection, invoiceNumber, parentUuid, detailed, statementDate, created, modified);
        }
    }
}
