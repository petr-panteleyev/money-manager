/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence;

import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.ForeignKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Table("transact")
public class Transaction implements MoneyRecord {
    public static final Comparator<Transaction> BY_DATE = (x, y) -> {
        int res = x.year - y.year;
        if (res == 0) {
            res = x.month - y.month;
            if (res == 0) {
                res = x.day - y.day;
                if (res == 0) {
                    res = x.id - y.id;
                }
            }
        }

        return res;
    };

    private final int id;
    private final BigDecimal amount;
    private final int day;
    private final int month;
    private final int year;
    private final int transactionTypeId;
    private final String comment;
    private final boolean checked;
    private final int accountDebitedId;
    private final int accountCreditedId;
    private final int accountDebitedTypeId;
    private final int accountCreditedTypeId;
    private final int accountDebitedCategoryId;
    private final int accountCreditedCategoryId;
    private final int groupId;
    private final int contactId;
    private final BigDecimal rate;
    private final int rateDirection;
    private final String invoiceNumber;
    private final String guid;
    private final long modified;

    private final TransactionType transactionType;
    private final CategoryType accountDebitedType;
    private final CategoryType accountCreditedType;

    @RecordBuilder
    public Transaction(@Field("id") int id,
                       @Field("amount") BigDecimal amount,
                       @Field("date_day") int day,
                       @Field("date_month") int month,
                       @Field("date_year") int year,
                       @Field("transaction_type_id") int transactionTypeId,
                       @Field("comment") String comment,
                       @Field("checked") boolean checked,
                       @Field("account_debited_id") int accountDebitedId,
                       @Field("account_credited_id") int accountCreditedId,
                       @Field("account_debited_type_id") int accountDebitedTypeId,
                       @Field("account_credited_type_id") int accountCreditedTypeId,
                       @Field("account_debited_category_id") int accountDebitedCategoryId,
                       @Field("account_credited_category_id") int accountCreditedCategoryId,
                       @Field("group_id") int groupId,
                       @Field("contact_id") int contactId,
                       @Field("currency_rate") BigDecimal rate,
                       @Field("rate_direction") int rateDirection,
                       @Field("invoice_number") String invoiceNumber,
                       @Field("guid") String guid,
                       @Field("modified") long modified) {
        this.id = id;
        this.amount = amount;
        this.day = day;
        this.month = month;
        this.year = year;
        this.transactionTypeId = transactionTypeId;
        this.comment = comment;
        this.checked = checked;
        this.accountDebitedId = accountDebitedId;
        this.accountCreditedId = accountCreditedId;
        this.accountDebitedTypeId = accountDebitedTypeId;
        this.accountCreditedTypeId = accountCreditedTypeId;
        this.accountDebitedCategoryId = accountDebitedCategoryId;
        this.accountCreditedCategoryId = accountCreditedCategoryId;
        this.groupId = groupId;
        this.contactId = contactId;
        this.rate = rate;
        this.rateDirection = rateDirection;
        this.invoiceNumber = invoiceNumber;
        this.guid = guid;
        this.modified = modified;

        this.transactionType = TransactionType.get(this.transactionTypeId);
        this.accountDebitedType = CategoryType.get(this.accountDebitedTypeId);
        this.accountCreditedType = CategoryType.get(this.accountCreditedTypeId);
    }

    public Transaction copy(int newId, int newAccountDebitedId, int newAccountCreditedId,
                            int newAccountDebitedCategoryId, int newAccountCreditedCategoryId,
                            int newContactId, int newGroupId) {
        return new Transaction(newId, amount, day, month, year, transactionTypeId, comment, checked,
                newAccountDebitedId, newAccountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                newAccountDebitedCategoryId, newAccountCreditedCategoryId, newGroupId, newContactId,
                rate, rateDirection, invoiceNumber, guid, modified);
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
        return new Transaction(id, amount, day, month, year, transactionTypeId, comment, check,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId, groupId, contactId,
                rate, rateDirection, invoiceNumber, guid, System.currentTimeMillis());
    }

    public Transaction setGroupId(int newGroupId) {
        return new Transaction(id, amount, day, month, year, transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId, newGroupId, contactId,
                rate, rateDirection, invoiceNumber, guid, System.currentTimeMillis());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount.stripTrailingZeros(), day, month, year, transactionTypeId, comment,
                checked, accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId, groupId, contactId, rate.stripTrailingZeros(),
                rateDirection, invoiceNumber, guid, modified);
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
        return id == that.id
                && day == that.day
                && month == that.month
                && year == that.year
                && transactionTypeId == that.transactionTypeId
                && checked == that.checked
                && accountDebitedId == that.accountDebitedId
                && accountCreditedId == that.accountCreditedId
                && accountDebitedTypeId == that.accountDebitedTypeId
                && accountCreditedTypeId == that.accountCreditedTypeId
                && accountDebitedCategoryId == that.accountDebitedCategoryId
                && accountCreditedCategoryId == that.accountCreditedCategoryId
                && groupId == that.groupId
                && contactId == that.contactId
                && rateDirection == that.rateDirection
                && amount.compareTo(that.amount) == 0
                && Objects.equals(comment, that.comment)
                && rate.compareTo(that.rate) == 0
                && Objects.equals(invoiceNumber, that.invoiceNumber)
                && Objects.equals(guid, that.guid)
                && modified == that.modified;
    }

    public String toString() {
        return "[Transaction id=" + this.getId() + ", amount=" + this.amount + "], accountDebitedId=" + this
                .accountDebitedId + " accountCreditedId=" + this.accountCreditedId;
    }

    @Field(value = "id", primaryKey = true)
    public int getId() {
        return this.id;
    }

    @Field("amount")
    public final BigDecimal getAmount() {
        return this.amount;
    }

    @Field("date_day")
    public final int getDay() {
        return this.day;
    }

    @Field("date_month")
    public final int getMonth() {
        return this.month;
    }

    @Field("date_year")
    public final int getYear() {
        return this.year;
    }

    @Field("transaction_type_id")
    public final int getTransactionTypeId() {
        return this.transactionTypeId;
    }

    @Field("comment")
    public final String getComment() {
        return this.comment;
    }

    @Field("checked")
    public final boolean getChecked() {
        return this.checked;
    }

    @Field("account_debited_id")
    @ForeignKey(table = Account.class)
    public final int getAccountDebitedId() {
        return this.accountDebitedId;
    }

    @Field("account_credited_id")
    @ForeignKey(table = Account.class)
    public final int getAccountCreditedId() {
        return this.accountCreditedId;
    }

    @Field(value = "account_debited_type_id", nullable = false)
    public final int getAccountDebitedTypeId() {
        return this.accountDebitedTypeId;
    }

    @Field(value = "account_credited_type_id", nullable = false)
    public final int getAccountCreditedTypeId() {
        return this.accountCreditedTypeId;
    }

    @Field(value = "account_debited_category_id", nullable = false)
    public final int getAccountDebitedCategoryId() {
        return this.accountDebitedCategoryId;
    }

    @Field(value = "account_credited_category_id", nullable = false)
    public final int getAccountCreditedCategoryId() {
        return this.accountCreditedCategoryId;
    }

    @Field(value = "group_id", nullable = false)
    public final int getGroupId() {
        return this.groupId;
    }

    @Field("contact_id")
    public final int getContactId() {
        return this.contactId;
    }

    @Field("currency_rate")
    public final BigDecimal getRate() {
        return this.rate;
    }

    @Field("rate_direction")
    public final int getRateDirection() {
        return this.rateDirection;
    }

    @Field("invoice_number")
    public final String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    @Field("guid")
    public String getGuid() {
        return this.guid;
    }

    @Field("modified")
    public long getModified() {
        return this.modified;
    }

    public static final class Builder {
        private int id;
        private BigDecimal amount = BigDecimal.ZERO;
        private int day;
        private int month;
        private int year;
        private int transactionTypeId;
        private String comment = "";
        private boolean checked;
        private int accountDebitedId;
        private int accountCreditedId;
        private int accountDebitedTypeId;
        private int accountCreditedTypeId;
        private int accountDebitedCategoryId;
        private int accountCreditedCategoryId;
        private int groupId;
        private int contactId;
        private BigDecimal rate = BigDecimal.ONE;
        private int rateDirection;
        private String invoiceNumber = "";
        private long created;
        private long modified;

        public Builder() {
        }

        public Builder(Transaction t) {
            if (t != null) {
                this.id = t.getId();
                this.amount = t.getAmount();
                this.day = t.getDay();
                this.month = t.getMonth();
                this.year = t.getYear();
                this.transactionTypeId = t.getTransactionTypeId();
                this.comment = t.getComment();
                this.checked = t.getChecked();
                this.accountDebitedId = t.getAccountDebitedId();
                this.accountCreditedId = t.getAccountCreditedId();
                this.accountDebitedTypeId = t.getAccountDebitedTypeId();
                this.accountCreditedTypeId = t.getAccountCreditedTypeId();
                this.accountDebitedCategoryId = t.getAccountDebitedCategoryId();
                this.accountCreditedCategoryId = t.getAccountCreditedCategoryId();
                this.groupId = t.getGroupId();
                this.contactId = t.getContactId();
                this.rate = t.getRate();
                this.rateDirection = t.getRateDirection();
                this.invoiceNumber = t.getInvoiceNumber();
                this.modified = t.getModified();
            }
        }

        public final int getId() {
            return this.id;
        }

        public final void setId(int id) {
            this.id = id;
        }


        public final BigDecimal getAmount() {
            return this.amount;
        }

        public final void setAmount(BigDecimal var1) {
            this.amount = var1;
        }

        public final int getDay() {
            return this.day;
        }

        public final void setDay(int var1) {
            this.day = var1;
        }

        public final int getMonth() {
            return this.month;
        }

        public final void setMonth(int var1) {
            this.month = var1;
        }

        public final int getYear() {
            return this.year;
        }

        public final void setYear(int var1) {
            this.year = var1;
        }

        public final int getTransactionTypeId() {
            return this.transactionTypeId;
        }

        public final void setTransactionTypeId(int var1) {
            this.transactionTypeId = var1;
        }

        public final String getComment() {
            return this.comment;
        }

        public final void setComment(String var1) {
            this.comment = var1;
        }

        public final boolean getChecked() {
            return this.checked;
        }

        public final void setChecked(boolean var1) {
            this.checked = var1;
        }

        public final int getAccountDebitedId() {
            return this.accountDebitedId;
        }

        public final void setAccountDebitedId(int var1) {
            this.accountDebitedId = var1;
        }

        public final int getAccountCreditedId() {
            return this.accountCreditedId;
        }

        public final void setAccountCreditedId(int var1) {
            this.accountCreditedId = var1;
        }

        public final int getAccountDebitedTypeId() {
            return this.accountDebitedTypeId;
        }

        public final void setAccountDebitedTypeId(int var1) {
            this.accountDebitedTypeId = var1;
        }

        public final int getAccountCreditedTypeId() {
            return this.accountCreditedTypeId;
        }

        public final void setAccountCreditedTypeId(int var1) {
            this.accountCreditedTypeId = var1;
        }

        public final int getAccountDebitedCategoryId() {
            return this.accountDebitedCategoryId;
        }

        public final void setAccountDebitedCategoryId(int var1) {
            this.accountDebitedCategoryId = var1;
        }

        public final int getAccountCreditedCategoryId() {
            return this.accountCreditedCategoryId;
        }

        public final void setAccountCreditedCategoryId(int var1) {
            this.accountCreditedCategoryId = var1;
        }

        public final int getGroupId() {
            return this.groupId;
        }

        public final void setGroupId(int var1) {
            this.groupId = var1;
        }

        public final int getContactId() {
            return this.contactId;
        }

        public final void setContactId(int var1) {
            this.contactId = var1;
        }


        public final BigDecimal getRate() {
            return this.rate;
        }

        public final void setRate(BigDecimal var1) {
            this.rate = var1;
        }

        public final int getRateDirection() {
            return this.rateDirection;
        }

        public final void setRateDirection(int var1) {
            this.rateDirection = var1;
        }


        public final String getInvoiceNumber() {
            return this.invoiceNumber;
        }

        public final void setInvoiceNumber(String var1) {
            this.invoiceNumber = var1;
        }

        public final long getCreated() {
            return this.created;
        }

        public final void setCreated(long var1) {
            this.created = var1;
        }

        public final long getModified() {
            return this.modified;
        }

        public final void setModified(long var1) {
            this.modified = var1;
        }


        public final Builder id(int id) {
            this.id = id;
            return this;
        }


        public final Builder amount(BigDecimal amount) {
            Objects.requireNonNull(amount);
            this.amount = amount;
            return this;
        }

        public final Builder day(int day) {
            this.day = day;
            return this;
        }


        public final Builder month(int month) {
            this.month = month;
            return this;
        }


        public final Builder year(int year) {
            this.year = year;
            return this;
        }


        public final Builder transactionTypeId(int id) {
            this.transactionTypeId = id;
            return this;
        }


        public final Builder transactionType(TransactionType type) {
            Objects.requireNonNull(type);
            this.transactionTypeId = type.getId();
            return this;
        }


        public final Builder comment(String comment) {
            Objects.requireNonNull(comment);
            this.comment = comment;
            return this;
        }


        public final Builder checked(boolean checked) {
            this.checked = checked;
            return this;
        }


        public final Builder accountDebitedId(int id) {
            this.accountDebitedId = id;
            return this;
        }


        public final Builder accountCreditedId(int id) {
            this.accountCreditedId = id;
            return this;
        }


        public final Builder accountDebitedTypeId(int id) {
            this.accountCreditedTypeId = id;
            return this;
        }


        public final Builder accountDebitedType(CategoryType type) {
            this.accountDebitedTypeId = type.getId();
            return this;
        }

        public final Builder accountCreditedTypeId(int id) {
            this.accountCreditedTypeId = id;
            return this;
        }

        public final Builder accountCreditedType(CategoryType type) {
            this.accountCreditedTypeId = type.getId();
            return this;
        }

        public final Builder accountDebitedCategoryId(int id) {
            this.accountDebitedCategoryId = id;
            return this;
        }

        public final Builder accountCreditedCategoryId(int id) {
            this.accountCreditedCategoryId = id;
            return this;
        }

        public final Builder groupId(int id) {
            this.groupId = id;
            return this;
        }

        public final Builder contactId(int id) {
            this.contactId = id;
            return this;
        }

        public final Builder rate(BigDecimal rate) {
            Objects.requireNonNull(rate);
            this.rate = rate;
            return this;
        }

        public final Builder rateDirection(int rateDirection) {
            this.rateDirection = rateDirection;
            return this;
        }

        public final Builder invoiceNumber(String invoiceNumber) {
            Objects.requireNonNull(invoiceNumber);
            this.invoiceNumber = invoiceNumber;
            return this;
        }

        public final Transaction build() {
            if (this.transactionTypeId == 0) {
                this.transactionTypeId = TransactionType.UNDEFINED.getId();
            }

            if (this.id != 0 && this.accountDebitedId != 0 && this.accountCreditedId != 0
                    && this.accountDebitedTypeId != 0 && this.accountCreditedTypeId != 0
                    && this.accountDebitedCategoryId != 0
                    && this.accountCreditedCategoryId != 0) {
                return new Transaction(id, amount, day, month, year, transactionTypeId, comment,
                        checked, accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                        accountDebitedCategoryId, accountCreditedCategoryId, groupId, contactId,
                        rate, rateDirection, invoiceNumber, UUID.randomUUID().toString(), System.currentTimeMillis());
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
