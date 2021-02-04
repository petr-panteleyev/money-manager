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

@Table("transaction")
public record Transaction(
    @PrimaryKey
    @Column("uuid")
    UUID uuid,
    @Column("amount")
    BigDecimal amount,
    @Column("day")
    int day,
    @Column("month")
    int month,
    @Column("year")
    int year,
    @Column("type")
    TransactionType type,
    @Column("comment")
    String comment,
    @Column("checked")
    boolean checked,
    @Column(value = "acc_debited_uuid")
    @ForeignKey(table = Account.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID accountDebitedUuid,
    @Column(value = "acc_credited_uuid")
    @ForeignKey(table = Account.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID accountCreditedUuid,
    @Column("acc_debited_type")
    CategoryType accountDebitedType,
    @Column("acc_credited_type")
    CategoryType accountCreditedType,
    @Column(value = "acc_debited_category_uuid")
    @ForeignKey(table = Category.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID accountDebitedCategoryUuid,
    @Column(value = "acc_credited_category_uuid")
    @ForeignKey(table = Category.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID accountCreditedCategoryUuid,
    @Column("contact_uuid")
    @ForeignKey(table = Contact.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID contactUuid,
    @Column("rate")
    BigDecimal rate,
    @Column("rate_direction")
    int rateDirection,
    @Column("invoice_number")
    String invoiceNumber,
    @Column("parent_uuid")
    @ForeignKey(table = Transaction.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    UUID parentUuid,
    @Column("detailed")
    boolean detailed,
    @Column("statement_date")
    LocalDate statementDate,
    @Column("created")
    long created,
    @Column("modified")
    long modified

) implements MoneyRecord
{

    public Transaction {
        amount = MoneyRecord.normalize(amount);
        rate = MoneyRecord.normalize(rate);

        if (statementDate == null) {
            statementDate = LocalDate.of(year, month, day);
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

    public static final class Builder {
        private BigDecimal amount = BigDecimal.ZERO;
        private int day;
        private int month;
        private int year;
        private TransactionType type;
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
            Objects.requireNonNull(amount);
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
            Objects.requireNonNull(type);
            this.type = type;
            return this;
        }

        public Builder comment(String comment) {
            Objects.requireNonNull(comment);
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
            Objects.requireNonNull(rate);
            this.rate = rate;
            return this;
        }

        public Builder rateDirection(int rateDirection) {
            this.rateDirection = rateDirection;
            return this;
        }

        public Builder invoiceNumber(String invoiceNumber) {
            Objects.requireNonNull(invoiceNumber);
            this.invoiceNumber = invoiceNumber;
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
            if (this.type == null) {
                this.type = TransactionType.UNDEFINED;
            }

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

            if (this.uuid != null && this.accountDebitedUuid != null && this.accountCreditedUuid != null
                && this.accountDebitedType != null && this.accountCreditedType != null
                && this.accountDebitedCategoryUuid != null && this.accountCreditedCategoryUuid != null) {
                return new Transaction(uuid, amount, day, month, year, type, comment,
                    checked, accountDebitedUuid, accountCreditedUuid,
                    accountDebitedType, accountCreditedType,
                    accountDebitedCategoryUuid, accountCreditedCategoryUuid, contactUuid,
                    rate, rateDirection, invoiceNumber, parentUuid, detailed, statementDate, created, modified);
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
