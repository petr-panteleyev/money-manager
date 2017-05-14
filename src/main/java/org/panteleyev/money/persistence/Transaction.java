/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.ForeignKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;

@Table("transact")
public class Transaction implements Record {
    public static final Comparator<Transaction> BY_DATE = (x, y) -> {
        int res = x.year - y.year;
        if (res != 0) {
            return res;
        }

        res = x.month - y.month;
        if (res != 0) {
            return res;
        }

        res = x.day - y.day;
        if (res != 0) {
            return res;
        }

        return x.getId() - y.getId();
    };

    public static final class Builder {
        private int             id;
        private BigDecimal      amount;
        private int             day;
        private int             month;
        private int             year;
        private TransactionType transactionType;
        private String          comment;
        private boolean         checked;
        private int             accountDebitedId;
        private int             accountCreditedId;
        private CategoryType    accountDebitedType;
        private CategoryType    accountCreditedType;
        private int             accountDebitedCategoryId;
        private int             accountCreditedCategoryId;
        private int             groupId;
        private int             contactId;
        private BigDecimal      rate;
        private int             rateDirection;
        private String          invoiceNumber;

        public Builder() {
            this.groupId = 0;
            this.rate = BigDecimal.ONE;
            this.rateDirection = 0;
            this.checked = false;
            this.contactId = 0;
        }

        public Builder(Transaction t) {
            this();

            if (t != null) {
                this.id = t.getId();
                this.amount = t.getAmount();
                this.day = t.getDay();
                this.month = t.getMonth();
                this.year = t.getYear();
                this.transactionType = t.getTransactionType();
                this.comment = t.getComment();
                this.checked = t.isChecked();
                this.accountDebitedId = t.getAccountDebitedId();
                this.accountCreditedId = t.getAccountCreditedId();
                this.accountDebitedType = t.getAccountDebitedType();
                this.accountCreditedType = t.getAccountCreditedType();
                this.accountDebitedCategoryId = t.getAccountDebitedCategoryId();
                this.accountCreditedCategoryId = t.getAccountCreditedCategoryId();
                this.groupId = t.getGroupId();
                this.contactId = t.getContactId();
                this.rate = t.getRate();
                this.rateDirection = t.getRateDirection();
                this.invoiceNumber = t.getInvoiceNumber();
            }
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public int id() {
            return id;
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

        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder checked(Boolean checked) {
            this.checked = checked;
            return this;
        }

        public Builder accountDebitedId(int accountDebitedId) {
            this.accountDebitedId = accountDebitedId;
            return this;
        }

        public int getAccountDebitedId() {
            return accountDebitedId;
        }

        public Builder accountCreditedId(int accountCreditedId) {
            this.accountCreditedId = accountCreditedId;
            return this;
        }

        public int getAccountCreditedId() {
            return accountCreditedId;
        }

        public Builder accountDebitedType(CategoryType accountDebitedType) {
            this.accountDebitedType = accountDebitedType;
            return this;
        }

        public Builder accountCreditedType(CategoryType accountCreditedType) {
            this.accountCreditedType = accountCreditedType;
            return this;
        }

        public Builder accountDebitedCategoryId(int accountDebitedCategoryId) {
            this.accountDebitedCategoryId = accountDebitedCategoryId;
            return this;
        }

        public Builder accountCreditedCategoryId(int accountCreditedCategoryId) {
            this.accountCreditedCategoryId = accountCreditedCategoryId;
            return this;
        }

        public Builder groupId(int groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder contactId(int contactId) {
            this.contactId = contactId;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
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

        public Transaction build() {
            Objects.requireNonNull(transactionType);

            if (id == 0
                    || accountDebitedId == 0
                    || accountCreditedId == 0
                    || accountDebitedCategoryId == 0
                    || accountCreditedCategoryId == 0) {
                throw new IllegalStateException();
            }

            Objects.requireNonNull(accountDebitedType);
            Objects.requireNonNull(accountCreditedType);

            return new Transaction(
                    id,
                    amount,
                    day,
                    month,
                    year,
                    transactionType,
                    comment,
                    checked,
                    accountDebitedId,
                    accountCreditedId,
                    accountDebitedType,
                    accountCreditedType,
                    accountDebitedCategoryId,
                    accountCreditedCategoryId,
                    groupId,
                    contactId,
                    rate,
                    rateDirection,
                    invoiceNumber
            );
        }
    }

    private final int             id;
    private final BigDecimal      amount;
    private final int             day;
    private final int             month;
    private final int             year;
    private final TransactionType transactionType;
    private final String          comment;
    private final boolean         checked;
    private final int             accountDebitedId;
    private final int             accountCreditedId;
    private final CategoryType    accountDebitedType;
    private final CategoryType    accountCreditedType;
    private final int             accountDebitedCategoryId;
    private final int             accountCreditedCategoryId;
    private final int             groupId;
    private final int             contactId;
    private final BigDecimal      rate;
    private final int             rateDirection;
    private final String          invoiceNumber;

    @RecordBuilder
    public Transaction(
            @Field(Field.ID) int id,
            @Field("amount") BigDecimal  amount,
            @Field("date_day") int day,
            @Field("date_month") int month,
            @Field("date_year") int year,
            @Field("transaction_type") TransactionType transactionType,
            @Field("comment") String comment,
            @Field("checked") boolean     checked,
            @Field("account_debited_id") int accountDebitedId,
            @Field("account_credited_id") int accountCreditedId,
            @Field("account_debited_type") CategoryType     accountDebitedType,
            @Field("account_credited_type") CategoryType     accountCreditedType,
            @Field("account_debited_category_id") int accountDebitedCategoryId,
            @Field("account_credited_category_id") int accountCreditedCategoryId,
            @Field("group_id") int groupId,
            @Field("contact_id") int contactId,
            @Field("currency_rate") BigDecimal  rate,
            @Field("rate_direction") int         rateDirection,
            @Field("invoice_number") String      invoiceNumber
    ) {
        this.id = id;
        this.amount = amount;
        this.day = day;
        this.month = month;
        this.year = year;
        this.transactionType = transactionType;
        this.comment = comment;
        this.checked = checked;
        this.accountDebitedId = accountDebitedId;
        this.accountCreditedId = accountCreditedId;
        this.accountDebitedType = accountDebitedType;
        this.accountCreditedType = accountCreditedType;
        this.accountDebitedCategoryId = accountDebitedCategoryId;
        this.accountCreditedCategoryId = accountCreditedCategoryId;
        this.groupId = groupId;
        this.contactId = contactId;
        this.rate = rate;
        this.rateDirection = rateDirection;
        this.invoiceNumber = invoiceNumber;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public int getId() {
        return id;
    }

    @Field("amount")
    public BigDecimal getAmount() {
        return amount;
    }

    @Field("date_day")
    public int getDay() {
        return day;
    }

    @Field("date_month")
    public int getMonth() {
        return month;
    }

    @Field("date_year")
    public int getYear() {
        return year;
    }

    @Field("transaction_type")
    public TransactionType getTransactionType() {
        return transactionType;
    }

    @Field("comment")
    public String getComment() {
        return comment;
    }

    @Field("checked")
    public boolean isChecked() {
        return checked;
    }

    @Field(value = "account_debited_id", nullable=false)
    @ForeignKey(table=Account.class)
    public int getAccountDebitedId() {
        return accountDebitedId;
    }

    @Field(value = "account_credited_id", nullable=false)
    @ForeignKey(table=Account.class)
    public int getAccountCreditedId() {
        return accountCreditedId;
    }

    @Field(value = "group_id", nullable = false)
    @ForeignKey(table=TransactionGroup.class)
    public int getGroupId() {
        return groupId;
    }

    @Field("contact_id")
    @ForeignKey(table=Contact.class)
    public int getContactId() {
        return contactId;
    }

    @Field(value = "account_debited_type", nullable=false)
    public CategoryType getAccountDebitedType() {
        return accountDebitedType;
    }

    @Field(value = "account_credited_type", nullable=false)
    public CategoryType getAccountCreditedType() {
        return accountCreditedType;
    }

    @Field(value = "account_debited_category_id", nullable=false)
    @ForeignKey(table=Category.class)
    public int getAccountDebitedCategoryId() {
        return accountDebitedCategoryId;
    }

    @Field(value = "account_credited_category_id", nullable=false)
    @ForeignKey(table=Category.class)
    public int getAccountCreditedCategoryId() {
        return accountCreditedCategoryId;
    }

   @Field("currency_rate")
    public BigDecimal getRate() {
        return rate;
    }

    @Field("rate_direction")
    public int getRateDirection() {
        return rateDirection;
    }

    @Field("invoice_number")
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Transaction) {
            Transaction that = (Transaction)obj;

            return this.id == that.id
                    && Objects.equals(this.amount, that.amount)
                    && this.day == that.day
                    && this.month == that.month
                    && this.year == that.year
                    && Objects.equals(this.transactionType, that.transactionType)
                    && Objects.equals(this.comment, that.comment)
                    && this.checked == that.checked
                    && this.accountDebitedId == that.accountDebitedId
                    && this.accountCreditedId == that.accountCreditedId
                    && Objects.equals(this.accountDebitedType, that.accountDebitedType)
                    && Objects.equals(this.accountCreditedType, that.accountCreditedType)
                    && this.accountDebitedCategoryId == that.accountDebitedCategoryId
                    && this.accountCreditedCategoryId == that.accountCreditedCategoryId
                    && this.groupId == that.groupId
                    && this.contactId == that.contactId
                    && Objects.equals(this.rate, that.rate)
                    && this.rateDirection == that.rateDirection
                    && Objects.equals(this.invoiceNumber, that.invoiceNumber);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, day, month, year, transactionType, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedType, accountCreditedType,
                accountDebitedCategoryId, accountCreditedCategoryId, groupId, contactId, rate, rateDirection,
                invoiceNumber);
    }
}
