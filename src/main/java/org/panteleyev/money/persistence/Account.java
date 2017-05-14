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
import org.panteleyev.persistence.annotations.ReferenceType;
import org.panteleyev.persistence.annotations.Table;

@Table("account")
public class Account implements Record, Named, Comparable<Account> {
    public static final class AccountCategoryNameComparator implements Comparator<Account> {
        @Override
        public int compare(Account o1, Account o2) {
            String name1 = MoneyDAO.getInstance().getCategory(o1.getCategoryId())
                    .map(Category::getName).orElse("");
            String name2 = MoneyDAO.getInstance().getCategory(o2.getCategoryId())
                    .map(Category::getName).orElse("");

            return name1.compareToIgnoreCase(name2);
        }
    }

    public static final class Builder {
        private int          id;
        private String       name;
        private String       comment;
        private BigDecimal   openingBalance;
        private BigDecimal   accountLimit;
        private BigDecimal   currencyRate;
        private CategoryType type;
        private int          categoryId;
        private int          currencyId;
        private boolean      enabled;

        public Builder() {
            openingBalance = BigDecimal.ZERO;
            accountLimit = BigDecimal.ZERO;
            currencyRate = BigDecimal.ONE;
            enabled = false;
        }

        public Builder(Account a) {
            this();

            if (a != null) {
                this.id = a.getId();
                this.name = a.getName();
                this.comment = a.getComment();
                this.openingBalance = a.getOpeningBalance();
                this.accountLimit = a.getAccountLimit();
                this.currencyRate = a.getCurrencyRate();
                this.type = a.getType();
                this.categoryId = a.getCategoryId();
                this.currencyId = a.getCurrencyId();
                this.enabled = a.isEnabled();
            }
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public int id() {
            return id;
        }

        public Builder name(String x) {
            this.name = x;
            return this;
        }

        public Builder comment(String x) {
            this.comment = x;
            return this;
        }

        public Builder openingBalance(BigDecimal x) {
            this.openingBalance = x;
            return this;
        }

        public Builder accountLimit(BigDecimal x) {
            this.accountLimit = x;
            return this;
        }

        public Builder currencyRate(BigDecimal x) {
            this.currencyRate = x;
            return this;
        }

        public Builder type(CategoryType type) {
            this.type = type;
            return this;
        }

        public Builder categoryId(int x) {
            this.categoryId = x;
            return this;
        }

        public Builder currencyId(int x) {
            this.currencyId = x;
            return this;
        }

        public Builder enabled(boolean x) {
            this.enabled = x;
            return this;
        }

        public Account build() {
            Objects.requireNonNull(name);
            Objects.requireNonNull(type);

            if (id == 0 || categoryId == 0) {
                throw new IllegalStateException();
            }
            // TODO: temporarily allow null currency but must be forbidden in the future
            //Objects.requireNonNull(currencyId);

            return new Account(
                    id,
                    name,
                    comment,
                    openingBalance,
                    accountLimit,
                    currencyRate,
                    type,
                    categoryId,
                    currencyId,
                    enabled
                );
        }
    }

    private final int          id;
    private final String       name;
    private final String       comment;
    private final BigDecimal   openingBalance;
    private final BigDecimal   accountLimit;
    private final BigDecimal   currencyRate;
    private final CategoryType type;
    private final int          categoryId;
    private final int          currencyId;
    private final boolean      enabled;

    @RecordBuilder
    public Account(
            @Field(Field.ID) int id,
            @Field("name") String name,
            @Field("comment") String comment,
            @Field("opening") BigDecimal openingBalance,
            @Field("acc_limit") BigDecimal accountLimit,
            @Field("currency_rate") BigDecimal currencyRate,
            @Field("type") CategoryType type,
            @Field("category_id") int categoryId,
            @Field("currency_id") int currencyId,
            @Field("enabled") boolean enabled
    ) {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.openingBalance = openingBalance;
        this.accountLimit = accountLimit;
        this.currencyRate = currencyRate;
        this.type = type;
        this.categoryId = categoryId;
        this.currencyId = currencyId;
        this.enabled = enabled;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public int getId() {
        return id;
    }

    @Field("name")
    @Override
    public String getName() {
        return name;
    }

    @Field("comment")
    public String getComment() {
        return comment;
    }

    @Field("opening")
    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    @Field(value = "type", nullable=false)
    public CategoryType getType() {
        return type;
    }

    @Field(value = "category_id", nullable=false)
    @ForeignKey(table=Category.class, onDelete=ReferenceType.CASCADE)
    public int getCategoryId() {
        return categoryId;
    }

    @Field("currency_id")
    @ForeignKey(table=Currency.class, onDelete=ReferenceType.CASCADE)
    public int getCurrencyId() {
        return currencyId;
    }

    @Field("acc_limit")
    public BigDecimal getAccountLimit() {
        return accountLimit;
    }

    @Field("currency_rate")
    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    @Field("enabled")
    public boolean isEnabled() {
        return enabled;
    }

    public Account enable(boolean enable) {
        return new Account(id, name, comment, openingBalance, accountLimit, currencyRate,
                type, categoryId, currencyId, enable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Account) {
            Account that = (Account)obj;
            return this.id == that.id
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.comment, that.comment)
                    && Objects.equals(this.openingBalance, that.openingBalance)
                    && Objects.equals(this.accountLimit, that.accountLimit)
                    && Objects.equals(this.currencyRate, that.currencyRate)
                    && Objects.equals(this.type, that.type)
                    && this.categoryId == that.categoryId
                    && this.currencyId == that.currencyId
                    && this.enabled == that.enabled;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, comment, openingBalance, accountLimit, currencyRate, type, categoryId,
                currencyId, enabled);
    }

    @Override
    public int compareTo(Account o) {
        return Objects.compare(this.getName(), o.getName(), String::compareToIgnoreCase);
    }
}
