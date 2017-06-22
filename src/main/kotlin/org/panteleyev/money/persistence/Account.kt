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

package org.panteleyev.money.persistence

import org.panteleyev.persistence.Record
import org.panteleyev.persistence.annotations.Field
import org.panteleyev.persistence.annotations.ForeignKey
import org.panteleyev.persistence.annotations.RecordBuilder
import org.panteleyev.persistence.annotations.ReferenceOption
import org.panteleyev.persistence.annotations.Table
import java.math.BigDecimal
import java.util.Comparator
import java.util.Objects

@Table("account")
data class Account @RecordBuilder constructor (
        @param:Field(Field.ID)
        val _id : Int,

        @param:Field("name")
        @get:Field("name")
        override val name : String,

        @param:Field("comment")
        @get:Field("comment")
        val comment : String,

        @param:Field("opening")
        @get:Field("opening")
        val openingBalance : BigDecimal,

        @param:Field("acc_limit")
        @get:Field("acc_limit")
        val accountLimit : BigDecimal,

        @param:Field("currency_rate")
        @get:Field("currency_rate")
        val currencyRate : BigDecimal,

        @param:Field("type_id")
        @get:Field("type_id", nullable = false)
        val typeId : Int,

        @param:Field("category_id")
        @get:Field("category_id")
        @get:ForeignKey(table = Category::class, onDelete = ReferenceOption.CASCADE)
        val categoryId : Int,

        @param:Field("currency_id")
        @get:Field("currency_id")
        val currencyId : Int,

        @param:Field("enabled")
        @get:Field("enabled")
        val enabled : Boolean
) : Record, Named, Comparable<Account> {
    val type : CategoryType = CategoryType.get(typeId)

    @Field(value = Field.ID, primaryKey = true)
    override fun getId(): Int = _id

    override fun compareTo(other: Account) : Int = this.name.compareTo(other.name, ignoreCase = true)

    fun enable(e: Boolean): Account = copy(enabled = e)

    override fun equals(other: Any?): Boolean {
        return if (other is Account) {
            this._id == other._id
                && this.name == other.name
                && this.comment == other.comment
                && this.openingBalance.compareTo(other.openingBalance) == 0
                && this.accountLimit.compareTo(other.accountLimit) == 0
                && this.currencyRate.compareTo(other.currencyRate) == 0
                && this.typeId == other.typeId
                && this.categoryId == other.categoryId
                && this.currencyId == other.currencyId
                && this.enabled == other.enabled
        } else false
    }

    override fun hashCode(): Int {
        return Objects.hash(_id, name, comment,
                openingBalance.stripTrailingZeros(),
                accountLimit.stripTrailingZeros(),
                currencyRate.stripTrailingZeros(),
                typeId, categoryId, currencyId, enabled)
    }

    class AccountCategoryNameComparator : Comparator<Account> {
        override fun compare(o1: Account, o2: Account): Int {
            val name1 = MoneyDAO.getCategory(o1.categoryId)?.name?:""
            val name2 = MoneyDAO.getCategory(o2.categoryId)?.name?:""
            return name1.compareTo(name2, ignoreCase = true)
        }
    }
}