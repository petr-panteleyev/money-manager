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
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public final class Account implements MoneyRecord, Named, Comparable<Account> {
    private final int id;
    private final String name;
    private final String comment;
    private final String accountNumber;
    private final BigDecimal openingBalance;
    private final BigDecimal accountLimit;
    private final BigDecimal currencyRate;
    private final int typeId;
    private final int categoryId;
    private final int currencyId;
    private final boolean enabled;
    private final String guid;
    private final long modified;

    private final CategoryType type;

    private Account(int id, String name, String comment, String accountNumber, BigDecimal openingBalance,
                    BigDecimal accountLimit, BigDecimal currencyRate, int typeId, int categoryId,
                    int currencyId, boolean enabled, String guid, long modified)
    {
        this.id = id;
        this.name = name;
        this.comment = comment;
        this.accountNumber = accountNumber;
        this.openingBalance = openingBalance;
        this.accountLimit = accountLimit;
        this.currencyRate = currencyRate;
        this.typeId = typeId;
        this.categoryId = categoryId;
        this.currencyId = currencyId;
        this.enabled = enabled;
        this.guid = guid;
        this.modified = modified;

        this.type = CategoryType.get(this.typeId);
    }

    public Account copy(int newId) {
        return new Builder(this)
            .id(newId)
            .build();
    }

    public Account copy(int newId, int newCategoryId) {
        return new Builder(this)
            .id(newId)
            .categoryId(newCategoryId)
            .build();
    }

    public CategoryType getType() {
        return type;
    }

    @Override
    public int compareTo(Account other) {
        return name.compareToIgnoreCase(other.name);
    }

    public Account enable(boolean e) {
        return new Account(id, name, comment, accountNumber, openingBalance, accountLimit, currencyRate, typeId,
            categoryId, currencyId, e, guid, modified);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getAccountLimit() {
        return accountLimit;
    }

    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    public int getTypeId() {
        return typeId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public long getModified() {
        return modified;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Account)) {
            return false;
        }

        Account that = (Account) other;

        return id == that.id
            && Objects.equals(name, that.name)
            && Objects.equals(comment, that.comment)
            && Objects.equals(accountNumber, that.accountNumber)
            && openingBalance.compareTo(that.openingBalance) == 0
            && accountLimit.compareTo(that.accountLimit) == 0
            && currencyRate.compareTo(that.currencyRate) == 0
            && typeId == that.typeId
            && categoryId == that.categoryId
            && currencyId == that.currencyId
            && enabled == that.enabled
            && Objects.equals(guid, that.guid)
            && modified == that.modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, comment, accountNumber, openingBalance.stripTrailingZeros(),
            accountLimit.stripTrailingZeros(),
            currencyRate.stripTrailingZeros(), typeId, categoryId, currencyId, enabled,
            guid, modified);
    }

    @Override
    public String toString() {
        return "Account ["
            + " id:" + id
            + " name:" + name
            + " comment:" + comment
            + " accountNumber:" + accountNumber
            + " categoryId:" + categoryId
            + "]";

    }

    public static final class AccountCategoryNameComparator implements Comparator<Account> {
        @Override
        public int compare(Account o1, Account o2) {
            String name1 = getDao().getCategory(o1.getCategoryId()).map(Category::getName).orElse("");
            String name2 = getDao().getCategory(o2.getCategoryId()).map(Category::getName).orElse("");
            return name1.compareToIgnoreCase(name2);
        }
    }

    /**
     * Calculates balance of all transactions related to this account.
     *
     * @param total whether initial balance should be added to the result
     * @return account balance
     */
    public BigDecimal calculateBalance(boolean total, Predicate<Transaction> filter) {
        return getDao().getTransactions(this).stream()
            .filter(filter)
            .map(t -> {
                BigDecimal amount = t.getAmount();
                if (this.getId() == t.getAccountCreditedId()) {
                    // handle conversion rate
                    var rate = t.getRate();
                    if (rate.compareTo(BigDecimal.ZERO) != 0) {
                        amount = t.getRateDirection() == 0 ?
                            amount.divide(rate, RoundingMode.HALF_UP) : amount.multiply(rate);
                    }
                } else {
                    amount = amount.negate();
                }
                return amount;
            })
            .reduce(total ? this.getOpeningBalance() : BigDecimal.ZERO, BigDecimal::add);
    }

    public String getAccountNumberNoSpaces() {
        return getAccountNumber().replaceAll(" ", "");
    }

    public static final class Builder {
        private int id = 0;
        private String name = "";
        private String comment = "";
        private String accountNumber = "";
        private BigDecimal openingBalance = BigDecimal.ZERO;
        private BigDecimal accountLimit = BigDecimal.ZERO;
        private BigDecimal currencyRate = BigDecimal.ONE;
        private int typeId;
        private int categoryId;
        private int currencyId;
        private boolean enabled = true;
        private String guid = null;
        private long modified = 0;

        public Builder() {
        }

        public Builder(int id) {
            this.id = id;
        }

        public Builder(Account account) {
            if (account == null) {
                return;
            }

            id = account.getId();
            name = account.getName();
            comment = account.getComment();
            accountNumber = account.getAccountNumber();
            openingBalance = account.getOpeningBalance();
            accountLimit = account.getAccountLimit();
            currencyRate = account.getCurrencyRate();
            typeId = account.getTypeId();
            categoryId = account.getCategoryId();
            currencyId = account.getCurrencyId();
            enabled = account.getEnabled();
            guid = account.getGuid();
            modified = account.getModified();
        }

        public Account build() {
            if (guid == null) {
                guid = UUID.randomUUID().toString();
            }

            if (modified == 0) {
                modified = System.currentTimeMillis();
            }

            return new Account(id, name, comment, accountNumber, openingBalance,
                accountLimit, currencyRate, typeId, categoryId,
                currencyId, enabled, guid, modified);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
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

        public Builder typeId(int typeId) {
            this.typeId = typeId;
            return this;
        }

        public Builder categoryId(int categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder currencyId(int currencyId) {
            this.currencyId = currencyId;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
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
    }
}
