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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

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
    private final int contactId;
    private final BigDecimal rate;
    private final int rateDirection;
    private final String invoiceNumber;
    private final String guid;
    private final long modified;
    private final int parentId;
    private final boolean detailed;

    private final TransactionType transactionType;
    private final CategoryType accountDebitedType;
    private final CategoryType accountCreditedType;

    protected Transaction(int id, BigDecimal amount, int day, int month, int year, int transactionTypeId,
                          String comment, boolean checked,
                          int accountDebitedId, int accountCreditedId,
                          int accountDebitedTypeId, int accountCreditedTypeId,
                          int accountDebitedCategoryId, int accountCreditedCategoryId,
                          int contactId, BigDecimal rate, int rateDirection,
                          String invoiceNumber, String guid, long modified, int parentId, boolean detailed)
    {
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
        this.contactId = contactId;
        this.rate = rate;
        this.rateDirection = rateDirection;
        this.invoiceNumber = invoiceNumber;
        this.guid = guid;
        this.modified = modified;
        this.parentId = parentId;
        this.detailed = detailed;

        this.transactionType = TransactionType.get(this.transactionTypeId);
        this.accountDebitedType = CategoryType.get(this.accountDebitedTypeId);
        this.accountCreditedType = CategoryType.get(this.accountCreditedTypeId);
    }

    public Transaction copy(int newId, int newAccountDebitedId, int newAccountCreditedId,
                            int newAccountDebitedCategoryId, int newAccountCreditedCategoryId,
                            int newContactId, int newParentId)
    {
        return new Builder(this)
            .id(newId)
            .accountDebitedId(newAccountDebitedId)
            .accountCreditedId(newAccountCreditedId)
            .accountDebitedCategoryId(newAccountDebitedCategoryId)
            .accountCreditedCategoryId(newAccountCreditedCategoryId)
            .contactId(newContactId)
            .parentId(newParentId)
            .build();
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

    public Transaction setParentId(int newParentId) {
        return new Builder(this)
            .parentId(newParentId)
            .modified(System.currentTimeMillis())
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount.stripTrailingZeros(), day, month, year, transactionTypeId, comment,
            checked, accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
            accountDebitedCategoryId, accountCreditedCategoryId, contactId, rate.stripTrailingZeros(),
            rateDirection, invoiceNumber, guid, modified, parentId, detailed);
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
            && contactId == that.contactId
            && rateDirection == that.rateDirection
            && amount.compareTo(that.amount) == 0
            && Objects.equals(comment, that.comment)
            && rate.compareTo(that.rate) == 0
            && Objects.equals(invoiceNumber, that.invoiceNumber)
            && Objects.equals(guid, that.guid)
            && modified == that.modified
            && parentId == that.parentId
            && detailed == that.detailed;
    }

    public String toString() {
        return "[Transaction id=" + id
            + " guid=" + guid
            + " amount=" + amount
            + " accountDebitedId=" + accountDebitedId
            + " accountCreditedId=" + accountCreditedId
            + " comment=" + comment
            + " parentId=" + parentId
            + "]";
    }

    public int getId() {
        return id;
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

    public final int getAccountDebitedId() {
        return accountDebitedId;
    }

    public final int getAccountCreditedId() {
        return accountCreditedId;
    }

    public final int getAccountDebitedTypeId() {
        return accountDebitedTypeId;
    }

    public final int getAccountCreditedTypeId() {
        return accountCreditedTypeId;
    }

    public final int getAccountDebitedCategoryId() {
        return accountDebitedCategoryId;
    }

    public final int getAccountCreditedCategoryId() {
        return accountCreditedCategoryId;
    }

    public final int getContactId() {
        return contactId;
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

    public String getGuid() {
        return guid;
    }

    public long getModified() {
        return modified;
    }

    public int getParentId() {
        return parentId;
    }

    public boolean isDetailed() {
        return detailed;
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
        private long modified = 0;
        private String guid;
        private int parentId = 0;
        private boolean detailed = false;

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
                this.contactId = t.getContactId();
                this.rate = t.getRate();
                this.rateDirection = t.getRateDirection();
                this.invoiceNumber = t.getInvoiceNumber();
                this.modified = t.getModified();
                this.parentId = t.getParentId();
                this.detailed = t.isDetailed();
                this.guid = t.getGuid();
            }
        }

        public int getId() {
            return this.id;
        }

        public int getAccountDebitedId() {
            return this.accountDebitedId;
        }

        public int getAccountCreditedId() {
            return this.accountCreditedId;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
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

        public Builder accountDebitedId(int id) {
            this.accountDebitedId = id;
            return this;
        }

        public Builder accountCreditedId(int id) {
            this.accountCreditedId = id;
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

        public Builder accountDebitedCategoryId(int id) {
            this.accountDebitedCategoryId = id;
            return this;
        }

        public Builder accountCreditedCategoryId(int id) {
            this.accountCreditedCategoryId = id;
            return this;
        }

        public Builder contactId(int id) {
            this.contactId = id;
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

        public Builder guid(String guid) {
            Objects.requireNonNull(guid);
            this.guid = guid;
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

        public Builder parentId(int parentId) {
            this.parentId = parentId;
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

            if (guid == null || guid.isEmpty()) {
                guid = UUID.randomUUID().toString();
            }

            if (modified == 0) {
                modified = System.currentTimeMillis();
            }

            if (this.id != 0 && this.accountDebitedId != 0 && this.accountCreditedId != 0
                && this.accountDebitedTypeId != 0 && this.accountCreditedTypeId != 0
                && this.accountDebitedCategoryId != 0
                && this.accountCreditedCategoryId != 0)
            {
                return new Transaction(id, amount, day, month, year, transactionTypeId, comment,
                    checked, accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                    accountDebitedCategoryId, accountCreditedCategoryId, contactId,
                    rate, rateDirection, invoiceNumber, guid, modified, parentId, detailed);
            } else {
                throw new IllegalStateException();
            }
        }
    }
}
