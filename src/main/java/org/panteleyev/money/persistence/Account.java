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
import org.panteleyev.persistence.annotations.ReferenceOption;
import org.panteleyev.persistence.annotations.Table;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

@Table("account")
public final class Account implements MoneyRecord, Named, Comparable<Account> {
    private final int id;
    private final String name;
    private final String comment;
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

    @RecordBuilder
    public Account(@Field("id") int id,
                   @Field("name") String name,
                   @Field("comment") String comment,
                   @Field("opening") BigDecimal openingBalance,
                   @Field("acc_limit") BigDecimal accountLimit,
                   @Field("currency_rate") BigDecimal currencyRate,
                   @Field("type_id") int typeId,
                   @Field("category_id") int categoryId,
                   @Field("currency_id") int currencyId,
                   @Field("enabled") boolean enabled,
                   @Field("guid") String guid,
                   @Field("modified") long modified) {
        this.id = id;
        this.name = name;
        this.comment = comment;
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
        return new Account(newId, name, comment, openingBalance, accountLimit, currencyRate, typeId, categoryId,
                currencyId, enabled, guid, modified);
    }

    public Account copy(int newId, int newCategoryId) {
        return new Account(newId, name, comment, openingBalance, accountLimit, currencyRate, typeId, newCategoryId,
                currencyId, enabled, guid, modified);
    }

    public CategoryType getType() {
        return type;
    }

    @Override
    public int compareTo(Account other) {
        return name.compareToIgnoreCase(other.name);
    }

    public Account enable(boolean e) {
        return new Account(id, name, comment, openingBalance, accountLimit, currencyRate, typeId, categoryId,
                currencyId, e, guid, modified);
    }

    @Override
    @Field(value = "id", primaryKey = true)
    public int getId() {
        return id;
    }

    @Override
    @Field("name")
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

    @Field("acc_limit")
    public BigDecimal getAccountLimit() {
        return accountLimit;
    }

    @Field("currency_rate")
    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    @Field(value = "type_id", nullable = false)
    public int getTypeId() {
        return typeId;
    }

    @Field("category_id")
    @ForeignKey(table = Category.class, onDelete = ReferenceOption.CASCADE)
    public int getCategoryId() {
        return categoryId;
    }

    @Field("currency_id")
    public int getCurrencyId() {
        return currencyId;
    }

    @Field("enabled")
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    @Field("guid")
    public String getGuid() {
        return guid;
    }

    @Override
    @Field("modified")
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
        return Objects.hash(id, name, comment, openingBalance.stripTrailingZeros(), accountLimit.stripTrailingZeros(),
                currencyRate.stripTrailingZeros(), typeId, categoryId, currencyId, enabled,
                guid, modified);
    }

    public static final class AccountCategoryNameComparator implements Comparator<Account> {
        @Override
        public int compare(Account o1, Account o2) {
            String name1 = getDao().getCategory(o1.getCategoryId()).map(Category::getName).orElse("");
            String name2 = getDao().getCategory(o2.getCategoryId()).map(Category::getName).orElse("");
            return name1.compareToIgnoreCase(name2);
        }
    }
}
