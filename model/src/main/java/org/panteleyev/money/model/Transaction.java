/*
 Copyright © 2017-2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Transaction(
        UUID uuid,
        BigDecimal amount,
        BigDecimal creditAmount,
        LocalDate transactionDate,
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
        String invoiceNumber,
        UUID parentUuid,
        boolean detailed,
        LocalDate statementDate,
        UUID cardUuid,
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
        if (creditAmount == null) {
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
        creditAmount = MoneyRecord.normalize(creditAmount, BigDecimal.ZERO);

        if (statementDate == null) {
            statementDate = transactionDate;
        }

        var now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

    public static BigDecimal getSignedAmount(Transaction t) {
        return t.accountCreditedType() != t.accountDebitedType() && t.accountDebitedType() != CategoryType.INCOMES ?
                t.amount().negate() : t.amount();
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

    public static BigDecimal getNegatedAmount(Transaction t) {
        return t.amount.negate();
    }

    public static final class Builder {
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal creditAmount = BigDecimal.ZERO;
        private LocalDate transactionDate;
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
        private String invoiceNumber = "";
        private long created = 0;
        private long modified = 0;
        private UUID uuid;
        private UUID parentUuid;
        private boolean detailed = false;
        private String newContactName;
        private LocalDate statementDate;
        private UUID cardUuid;

        public Builder() {
        }

        public Builder(Transaction t) {
            if (t == null) {
                return;
            }

            this.amount = t.amount();
            this.creditAmount = t.creditAmount();
            this.transactionDate = t.transactionDate();
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
            this.invoiceNumber = t.invoiceNumber();
            this.created = t.created();
            this.modified = t.modified();
            this.parentUuid = t.parentUuid();
            this.detailed = t.detailed();
            this.uuid = t.uuid();
            this.statementDate = t.statementDate();
            this.cardUuid = t.cardUuid();
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public UUID getCardUuid() {
            return this.uuid;
        }

        public String getNewContactName() {
            return newContactName;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder creditAmount(BigDecimal creditAmount) {
            this.creditAmount = creditAmount;
            return this;
        }

        public Builder transactionDate(LocalDate transactionDate) {
            this.transactionDate = transactionDate;
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

        public Builder cardUuid(UUID cardUuid) {
            this.cardUuid = cardUuid;
            return this;
        }

        public Transaction build() {
            return new Transaction(uuid, amount, creditAmount, transactionDate, type, comment,
                    checked, accountDebitedUuid, accountCreditedUuid,
                    accountDebitedType, accountCreditedType,
                    accountDebitedCategoryUuid, accountCreditedCategoryUuid, contactUuid,
                    invoiceNumber, parentUuid, detailed, statementDate, cardUuid, created, modified);
        }
    }
}
