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
import java.util.Optional;
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
        private Integer     id;
        private BigDecimal  amount;
        private int         day;
        private int         month;
        private int         year;
        private Integer     transactionTypeId;
        private String      comment;
        private boolean     checked;
        private Integer     accountDebitedId;
        private Integer     accountCreditedId;
        private Integer     accountDebitedTypeId;
        private Integer     accountCreditedTypeId;
        private Integer     accountDebitedCategoryId;
        private Integer     accountCreditedCategoryId;
        private Integer     groupId;
        private Integer     contactId;
        private BigDecimal  rate;
        private int         rateDirection;
        private String      invoiceNumber;
        private String      project;
        private int         reference;

        public Builder() {
            this.groupId = 0;
            this.rate = BigDecimal.ONE;
            this.rateDirection = 0;
            this.checked = false;
        }

        public Builder(Transaction t) {
            this();

            if (t != null) {
                this.id = t.getId();
                this.amount = t.getAmount();
                this.day = t.getDay();
                this.month = t.getMonth();
                this.year = t.getYear();
                this.transactionTypeId = t.getTransactionTypeId();
                this.comment = t.getComment();
                this.checked = t.isChecked();
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
                this.project = t.getProject();
                this.reference = t.getReference();
            }
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Optional<Integer> id() {
            return Optional.ofNullable(id);
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

        public Builder transactionTypeId(Integer transactionTypeId) {
            this.transactionTypeId = transactionTypeId;
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

        public Builder accountDebitedId(Integer accountDebitedId) {
            this.accountDebitedId = accountDebitedId;
            return this;
        }

        public Optional<Integer> accountDebitedId() {
            return Optional.ofNullable(accountDebitedId);
        }

        public Builder accountCreditedId(Integer accountCreditedId) {
            this.accountCreditedId = accountCreditedId;
            return this;
        }

        public Optional<Integer> accountCreditedId() {
            return Optional.ofNullable(accountCreditedId);
        }

        public Builder accountDebitedTypeId(Integer accountDebitedTypeId) {
            this.accountDebitedTypeId = accountDebitedTypeId;
            return this;
        }

        public Builder accountCreditedTypeId(Integer accountCreditedTypeId) {
            this.accountCreditedTypeId = accountCreditedTypeId;
            return this;
        }

        public Builder accountDebitedCategoryId(Integer accountDebitedCategoryId) {
            this.accountDebitedCategoryId = accountDebitedCategoryId;
            return this;
        }

        public Builder accountCreditedCategoryId(Integer accountCreditedCategoryId) {
            this.accountCreditedCategoryId = accountCreditedCategoryId;
            return this;
        }

        public Builder groupId(int groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder contactId(Integer contactId) {
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

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder reference(Integer reference) {
            this.reference = reference;
            return this;
        }

        public Transaction build() {
            Objects.requireNonNull(id);
            Objects.requireNonNull(transactionTypeId);
            Objects.requireNonNull(accountDebitedId);
            Objects.requireNonNull(accountCreditedId);
            Objects.requireNonNull(accountDebitedTypeId);
            Objects.requireNonNull(accountCreditedTypeId);
            Objects.requireNonNull(accountDebitedCategoryId);
            Objects.requireNonNull(accountCreditedCategoryId);

            return new Transaction(
                    id,
                    amount,
                    day,
                    month,
                    year,
                    transactionTypeId,
                    comment,
                    checked,
                    accountDebitedId,
                    accountCreditedId,
                    accountDebitedTypeId,
                    accountCreditedTypeId,
                    accountDebitedCategoryId,
                    accountCreditedCategoryId,
                    groupId,
                    contactId,
                    rate,
                    rateDirection,
                    invoiceNumber,
                    project,
                    reference
            );
        }
    }

    private final Integer    id;
    private final BigDecimal amount;
    private final int        day;
    private final int        month;
    private final int        year;
    private final Integer    transactionTypeId;
    private final String     comment;
    private final boolean    checked;
    private final Integer    accountDebitedId;
    private final Integer    accountCreditedId;
    private final Integer    accountDebitedTypeId;
    private final Integer    accountCreditedTypeId;
    private final Integer    accountDebitedCategoryId;
    private final Integer    accountCreditedCategoryId;
    private final Integer    groupId;
    private final Integer    contactId;
    private final BigDecimal rate;
    private final int        rateDirection;
    private final String     invoiceNumber;
    private final String     project;
    private final int        reference;

    @RecordBuilder
    public Transaction(
            @Field(Field.ID) Integer id,
            @Field("amount") BigDecimal  amount,
            @Field("date_day") int day,
            @Field("date_month") int month,
            @Field("date_year") int year,
            @Field("transaction_type") Integer transactionTypeId,
            @Field("comment") String comment,
            @Field("checked") boolean     checked,
            @Field("account_debited_id") Integer     accountDebitedId,
            @Field("account_credited_id") Integer     accountCreditedId,
            @Field("account_debited_type_id") Integer     accountDebitedTypeId,
            @Field("account_credited_type_id") Integer     accountCreditedTypeId,
            @Field("account_debited_category_id") Integer     accountDebitedCategoryId,
            @Field("account_credited_category_id") Integer     accountCreditedCategoryId,
            @Field("group_id") Integer     groupId,
            @Field("contact_id") Integer     contactId,
            @Field("currency_rate") BigDecimal  rate,
            @Field("rate_direction") int         rateDirection,
            @Field("invoice_number") String      invoiceNumber,
            @Field("project") String      project,
            @Field("reference") int         reference
    ) {
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
        this.project = project;
        this.reference = reference;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public Integer getId() {
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
    @ForeignKey(table=TransactionType.class)
    public Integer getTransactionTypeId() {
        return transactionTypeId;
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
    public Integer getAccountDebitedId() {
        return accountDebitedId;
    }

    @Field(value = "account_credited_id", nullable=false)
    @ForeignKey(table=Account.class)
    public Integer getAccountCreditedId() {
        return accountCreditedId;
    }

    @Field(value = "group_id", nullable = false)
    @ForeignKey(table=TransactionGroup.class)
    public Integer getGroupId() {
        return groupId;
    }

    @Field("contact_id")
    @ForeignKey(table=Contact.class)
    public Integer getContactId() {
        return contactId;
    }

    @Field(value = "account_debited_type_id", nullable=false)
    @ForeignKey(table=CategoryType.class)
    public Integer getAccountDebitedTypeId() {
        return accountDebitedTypeId;
    }

    @Field(value = "account_credited_type_id", nullable=false)
    @ForeignKey(table=CategoryType.class)
    public Integer getAccountCreditedTypeId() {
        return accountCreditedTypeId;
    }

    @Field(value = "account_debited_category_id", nullable=false)
    @ForeignKey(table=Category.class)
    public Integer getAccountDebitedCategoryId() {
        return accountDebitedCategoryId;
    }

    @Field(value = "account_credited_category_id", nullable=false)
    @ForeignKey(table=Category.class)
    public Integer getAccountCreditedCategoryId() {
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

    @Field("project")
    public String getProject() {
        return project;
    }

    @Field("reference")
    public int getReference() {
        return reference;
    }

    @Override
    public String toString() {
        return "Transaction{" + "amount=" + amount + ", day=" + day + ", month=" + month + ", year=" + year + ", transactionTypeId=" + transactionTypeId + ", comment=" + comment + ", checked=" + checked + ", accountDebitedId=" + accountDebitedId + ", accountCreditedId=" + accountCreditedId + ", accountDebitedTypeId=" + accountDebitedTypeId + ", accountCreditedTypeId=" + accountCreditedTypeId + ", accountDebitedCategoryId=" + accountDebitedCategoryId + ", accountCreditedCategoryId=" + accountCreditedCategoryId + ", groupId=" + groupId + ", contactId=" + contactId + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Transaction) {
            Transaction that = (Transaction)obj;

            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.amount, that.amount)
                    && Objects.equals(this.day, that.day)
                    && Objects.equals(this.month, that.month)
                    && Objects.equals(this.year, that.year)
                    && Objects.equals(this.transactionTypeId, that.transactionTypeId)
                    && Objects.equals(this.comment, that.comment)
                    && Objects.equals(this.checked, that.checked)
                    && Objects.equals(this.accountDebitedId, that.accountDebitedId)
                    && Objects.equals(this.accountCreditedId, that.accountCreditedId)
                    && Objects.equals(this.accountDebitedTypeId, that.accountDebitedTypeId)
                    && Objects.equals(this.accountCreditedTypeId, that.accountCreditedTypeId)
                    && Objects.equals(this.accountDebitedCategoryId, that.accountDebitedCategoryId)
                    && Objects.equals(this.accountCreditedCategoryId, that.getAccountCreditedCategoryId())
                    && Objects.equals(this.groupId, that.groupId)
                    && Objects.equals(this.contactId, that.contactId)
                    && Objects.equals(this.rate, that.rate)
                    && Objects.equals(this.rateDirection, that.rateDirection)
                    && Objects.equals(this.invoiceNumber, that.invoiceNumber)
                    && Objects.equals(this.project, that.project)
                    && Objects.equals(this.reference, that.reference);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, day, month, year, transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId, groupId, contactId, rate, rateDirection,
                invoiceNumber, project, reference);
    }
}
