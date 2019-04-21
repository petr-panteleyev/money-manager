/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence.model;

import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.ForeignKey;
import org.panteleyev.persistence.annotations.PrimaryKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.ReferenceOption;
import org.panteleyev.persistence.annotations.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/*
Migration:

update transaction
set contact_uuid = (select uuid from contact where contact.id = transaction.contact_id),
acc_debited_uuid = (select uuid from account where account.id = transaction.acc_debited_id),
acc_credited_uuid = (select uuid from account where account.id = transaction.acc_credited_id),
acc_debited_category_uuid = (select uuid from category where category.id = transaction.acc_debited_category_id),
acc_credited_category_uuid = (select uuid from category where category.id = transaction.acc_credited_category_id)
;

update transaction as t1 join (select id, uuid from transaction) as t2
set t1.parent_uuid = t2.uuid
where t1.parent_id = t2.id;
 */


@Table("transaction")
public class Transaction implements MoneyRecord {
    public static final Comparator<Transaction> BY_DATE =
        Comparator.comparing(Transaction::getDate).thenComparingLong(Transaction::getCreated);

    public static final Comparator<Transaction> BY_DAY =
        Comparator.comparingInt(Transaction::getDay).thenComparingLong(Transaction::getCreated);

    @PrimaryKey
    @Column("uuid")
    private final UUID guid;

    @Column("amount")
    private final BigDecimal amount;

    @Column("day")
    private final int day;

    @Column("month")
    private final int month;

    @Column("year")
    private final int year;

    @Column("type_id")
    private final int transactionTypeId;

    @Column("comment")
    private final String comment;

    @Column("checked")
    private final boolean checked;

    @Column(value = "acc_debited_uuid", nullable = true)
    @ForeignKey(table = Account.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    private final UUID accountDebitedUuid;

    @Column(value = "acc_credited_uuid", nullable = true)
    @ForeignKey(table = Account.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    private final UUID accountCreditedUuid;

    @Column("acc_debited_type_id")
    private final int accountDebitedTypeId;

    @Column("acc_credited_type_id")
    private final int accountCreditedTypeId;

    @Column(value = "acc_debited_category_uuid", nullable = true)
    @ForeignKey(table = Category.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    private final UUID accountDebitedCategoryUuid;

    @Column(value = "acc_credited_category_uuid", nullable = true)
    @ForeignKey(table = Category.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    private final UUID accountCreditedCategoryUuid;

    @Column("contact_uuid")
    @ForeignKey(table = Contact.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    private final UUID contactUuid;

    @Column("rate")
    private final BigDecimal rate;

    @Column("rate_direction")
    private final int rateDirection;

    @Column("invoice_number")
    private final String invoiceNumber;

    @Column("created")
    private final long created;

    @Column("modified")
    private final long modified;

    @Column("parent_uuid")
    @ForeignKey(table = Transaction.class, column = "uuid", onDelete = ReferenceOption.RESTRICT)
    private final UUID parentUuid;

    @Column("detailed")
    private final boolean detailed;

    private final TransactionType transactionType;
    private final CategoryType accountDebitedType;
    private final CategoryType accountCreditedType;
    private final LocalDate date;

    @RecordBuilder
    public Transaction(@Column("amount") BigDecimal amount,
                       @Column("day") int day,
                       @Column("month") int month,
                       @Column("year") int year,
                       @Column("type_id") int transactionTypeId,
                       @Column("comment") String comment,
                       @Column("checked") boolean checked,
                       @Column("acc_debited_uuid") UUID accountDebitedUuid,
                       @Column("acc_credited_uuid") UUID accountCreditedUuid,
                       @Column("acc_debited_type_id") int accountDebitedTypeId,
                       @Column("acc_credited_type_id") int accountCreditedTypeId,
                       @Column("acc_debited_category_uuid") UUID accountDebitedCategoryUuid,
                       @Column("acc_credited_category_uuid") UUID accountCreditedCategoryUuid,
                       @Column("contact_uuid") UUID contactUuid,
                       @Column("rate") BigDecimal rate,
                       @Column("rate_direction") int rateDirection,
                       @Column("invoice_number") String invoiceNumber,
                       @Column("uuid") UUID guid,
                       @Column("created") long created,
                       @Column("modified") long modified,
                       @Column("parent_uuid") UUID parentUuid,
                       @Column("detailed") boolean detailed) {
        this.amount = amount;
        this.day = day;
        this.month = month;
        this.year = year;
        this.transactionTypeId = transactionTypeId;
        this.comment = comment;
        this.checked = checked;
        this.accountDebitedUuid = accountDebitedUuid;
        this.accountCreditedUuid = accountCreditedUuid;
        this.accountDebitedTypeId = accountDebitedTypeId;
        this.accountCreditedTypeId = accountCreditedTypeId;
        this.accountDebitedCategoryUuid = accountDebitedCategoryUuid;
        this.accountCreditedCategoryUuid = accountCreditedCategoryUuid;
        this.contactUuid = contactUuid;
        this.rate = rate;
        this.rateDirection = rateDirection;
        this.invoiceNumber = invoiceNumber;
        this.guid = guid;
        this.created = created;
        this.modified = modified;
        this.parentUuid = parentUuid;
        this.detailed = detailed;

        this.transactionType = TransactionType.get(this.transactionTypeId);
        this.accountDebitedType = CategoryType.get(this.accountDebitedTypeId);
        this.accountCreditedType = CategoryType.get(this.accountCreditedTypeId);

        this.date = LocalDate.of(year, month, day);
    }

    public BigDecimal getSignedAmount() {
        return accountCreditedType != accountDebitedType && accountDebitedType != CategoryType.INCOMES ?
            amount.negate() : amount;
    }

    public final TransactionType getTransactionType() {
        return this.transactionType;
    }

    public final CategoryType getAccountDebitedType() {
        return this.accountDebitedType;
    }

    public final CategoryType getAccountCreditedType() {
        return this.accountCreditedType;
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

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), day, month, year, transactionTypeId, comment,
            checked, accountDebitedUuid, accountCreditedUuid,
            accountDebitedTypeId, accountCreditedTypeId, accountDebitedCategoryUuid,
            accountCreditedCategoryUuid, contactUuid, rate.stripTrailingZeros(),
            rateDirection, invoiceNumber, guid, created, modified, parentUuid, detailed);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Transaction)) {
            return false;
        }

        Transaction that = (Transaction) other;
        return day == that.day
            && month == that.month
            && year == that.year
            && transactionTypeId == that.transactionTypeId
            && checked == that.checked
            && Objects.equals(accountDebitedUuid, that.accountDebitedUuid)
            && Objects.equals(accountCreditedUuid, that.accountCreditedUuid)
            && accountDebitedTypeId == that.accountDebitedTypeId
            && accountCreditedTypeId == that.accountCreditedTypeId
            && Objects.equals(accountDebitedCategoryUuid, that.accountDebitedCategoryUuid)
            && Objects.equals(accountCreditedCategoryUuid, that.accountCreditedCategoryUuid)
            && Objects.equals(contactUuid, that.contactUuid)
            && rateDirection == that.rateDirection
            && amount.compareTo(that.amount) == 0
            && Objects.equals(comment, that.comment)
            && rate.compareTo(that.rate) == 0
            && Objects.equals(invoiceNumber, that.invoiceNumber)
            && Objects.equals(guid, that.guid)
            && created == that.created
            && modified == that.modified
            && Objects.equals(parentUuid, that.parentUuid)
            && detailed == that.detailed;
    }

    public String toString() {
        return "[Transaction "
            + " guid=" + guid
            + " amount=" + amount
            + " accountDebitedUuid=" + accountDebitedUuid
            + " accountCreditedUuid=" + accountCreditedUuid
            + " comment=" + comment
            + " parentUuid=" + parentUuid
            + "]";
    }

    public final BigDecimal getAmount() {
        return amount;
    }

    public final int getDay() {
        return day;
    }

    public final int getMonth() {
        return month;
    }

    public final int getYear() {
        return year;
    }

    public final int getTransactionTypeId() {
        return transactionTypeId;
    }

    public final String getComment() {
        return comment;
    }

    public final boolean getChecked() {
        return checked;
    }

    public UUID getAccountDebitedUuid() {
        return accountDebitedUuid;
    }

    public UUID getAccountCreditedUuid() {
        return accountCreditedUuid;
    }

    public UUID getAccountDebitedCategoryUuid() {
        return accountDebitedCategoryUuid;
    }

    public UUID getAccountCreditedCategoryUuid() {
        return accountCreditedCategoryUuid;
    }

    public Optional<UUID> getContactUuid() {
        return Optional.ofNullable(contactUuid);
    }

    public Optional<UUID> getParentUuid() {
        return Optional.ofNullable(parentUuid);
    }

    public final int getAccountDebitedTypeId() {
        return accountDebitedTypeId;
    }

    public final int getAccountCreditedTypeId() {
        return accountCreditedTypeId;
    }

    public final BigDecimal getRate() {
        return rate;
    }

    public final int getRateDirection() {
        return rateDirection;
    }

    public final String getInvoiceNumber() {
        return invoiceNumber;
    }

    public UUID getGuid() {
        return guid;
    }

    @Override
    public long getCreated() {
        return created;
    }

    public long getModified() {
        return modified;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public LocalDate getDate() {
        return date;
    }

    public static final class Builder {
        private BigDecimal amount = BigDecimal.ZERO;
        private int day;
        private int month;
        private int year;
        private int transactionTypeId;
        private String comment = "";
        private boolean checked;
        private UUID accountDebitedUuid;
        private UUID accountCreditedUuid;
        private int accountDebitedTypeId;
        private int accountCreditedTypeId;
        private UUID accountDebitedCategoryUuid;
        private UUID accountCreditedCategoryUuid;
        private UUID contactUuid;
        private BigDecimal rate = BigDecimal.ONE;
        private int rateDirection;
        private String invoiceNumber = "";
        private long created = 0;
        private long modified = 0;
        private UUID guid;
        private UUID parentUuid;
        private boolean detailed = false;

        public Builder() {
        }

        public Builder(Transaction t) {
            if (t != null) {
                this.amount = t.getAmount();
                this.day = t.getDay();
                this.month = t.getMonth();
                this.year = t.getYear();
                this.transactionTypeId = t.getTransactionTypeId();
                this.comment = t.getComment();
                this.checked = t.getChecked();
                this.accountDebitedUuid = t.getAccountDebitedUuid();
                this.accountCreditedUuid = t.getAccountCreditedUuid();
                this.accountDebitedTypeId = t.getAccountDebitedTypeId();
                this.accountCreditedTypeId = t.getAccountCreditedTypeId();
                this.accountDebitedCategoryUuid = t.getAccountDebitedCategoryUuid();
                this.accountCreditedCategoryUuid = t.getAccountCreditedCategoryUuid();
                this.contactUuid = t.getContactUuid().orElse(null);
                this.rate = t.getRate();
                this.rateDirection = t.getRateDirection();
                this.invoiceNumber = t.getInvoiceNumber();
                this.created = t.getCreated();
                this.modified = t.getModified();
                this.parentUuid = t.getParentUuid().orElse(null);
                this.detailed = t.isDetailed();
                this.guid = t.getGuid();
            }
        }

        public UUID getUuid() {
            return this.guid;
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

        public Builder transactionTypeId(int id) {
            this.transactionTypeId = id;
            return this;
        }

        public Builder transactionType(TransactionType type) {
            Objects.requireNonNull(type);
            this.transactionTypeId = type.getId();
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

        public Builder accountDebitedTypeId(int id) {
            this.accountDebitedTypeId = id;
            return this;
        }

        public Builder accountDebitedType(CategoryType type) {
            this.accountDebitedTypeId = type.getId();
            return this;
        }

        public Builder accountCreditedTypeId(int id) {
            this.accountCreditedTypeId = id;
            return this;
        }

        public Builder accountCreditedType(CategoryType type) {
            this.accountCreditedTypeId = type.getId();
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
            this.guid = guid;
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

        public Transaction build() {
            if (this.transactionTypeId == 0) {
                this.transactionTypeId = TransactionType.UNDEFINED.getId();
            }

            if (guid == null) {
                guid = UUID.randomUUID();
            }

            long now = System.currentTimeMillis();
            if (created == 0) {
                created = now;
            }
            if (modified == 0) {
                modified = now;
            }

            if (this.guid != null && this.accountDebitedUuid != null && this.accountCreditedUuid != null
                && this.accountDebitedTypeId != 0 && this.accountCreditedTypeId != 0
                && this.accountDebitedCategoryUuid != null && this.accountCreditedCategoryUuid != null) {
                return new Transaction(amount, day, month, year, transactionTypeId, comment,
                    checked, accountDebitedUuid, accountCreditedUuid,
                    accountDebitedTypeId, accountCreditedTypeId,
                    accountDebitedCategoryUuid, accountCreditedCategoryUuid, contactUuid,
                    rate, rateDirection, invoiceNumber, guid, created, modified, parentUuid, detailed);
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
