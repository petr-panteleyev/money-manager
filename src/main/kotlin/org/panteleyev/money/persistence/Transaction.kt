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

import org.panteleyev.persistence.annotations.Field
import org.panteleyev.persistence.annotations.ForeignKey
import org.panteleyev.persistence.annotations.RecordBuilder
import org.panteleyev.persistence.annotations.Table
import java.math.BigDecimal
import java.util.Objects
import java.util.UUID

@Table("transact")
open class Transaction @RecordBuilder constructor (
        @param:Field("id")
        @get:Field(value = "id", primaryKey = true)
        override val id : Int,

        @param:Field("amount")
        @get:Field("amount")
        val amount : BigDecimal,

        @param:Field("date_day")
        @get:Field("date_day")
        val day : Int,

        @param:Field("date_month")
        @get:Field("date_month")
        val month : Int,

        @param:Field("date_year")
        @get:Field("date_year")
        val year : Int,

        @param:Field("transaction_type_id")
        @get:Field("transaction_type_id")
        val transactionTypeId : Int,

        @param:Field("comment")
        @get:Field("comment")
        val comment : String,

        @param:Field("checked")
        @get:Field("checked")
        val checked : Boolean,

        @param:Field("account_debited_id")
        @get:Field("account_debited_id")
        @get:ForeignKey(table = Account::class)
        val accountDebitedId : Int,

        @param:Field("account_credited_id")
        @get:Field("account_credited_id")
        @get:ForeignKey(table = Account::class)
        val accountCreditedId : Int,

        @param:Field("account_debited_type_id")
        @get:Field("account_debited_type_id", nullable = false)
        val accountDebitedTypeId : Int,

        @param:Field("account_credited_type_id")
        @get:Field("account_credited_type_id", nullable = false)
        val accountCreditedTypeId : Int,

        @param:Field("account_debited_category_id")
        @get:Field("account_debited_category_id", nullable = false)
        val accountDebitedCategoryId : Int,

        @param:Field("account_credited_category_id")
        @get:Field("account_credited_category_id", nullable = false)
        val accountCreditedCategoryId : Int,

        @param:Field("group_id")
        @get:Field("group_id", nullable = false)
        val groupId : Int,

        @param:Field("contact_id")
        @get:Field("contact_id")
        val contactId : Int,

        @param:Field("currency_rate")
        @get:Field("currency_rate")
        val rate : BigDecimal,

        @param:Field("rate_direction")
        @get:Field("rate_direction")
        val rateDirection : Int,

        @param:Field("invoice_number")
        @get:Field("invoice_number")
        val invoiceNumber : String,

        @param:Field("guid")
        @get:Field("guid")
        override val guid: String,

        @param:Field("modified")
        @get:Field("modified")
        override val modified: Long
) : MoneyRecord {
    val transactionType = TransactionType.get(transactionTypeId)
    val accountDebitedType = CategoryType.get(accountDebitedTypeId)
    val accountCreditedType = CategoryType.get(accountCreditedTypeId)

    val signedAmount : BigDecimal =
            if (accountCreditedType != accountDebitedType && accountDebitedType != CategoryType.INCOMES)
                amount.negate()
            else
                amount

    override fun equals(other: Any?): Boolean {
        return if (other is Transaction) {
            this.id == other.id
                && this.amount.compareTo(other.amount) == 0
                && this.day == other.day
                && this.month == other.month
                && this.year == other.year
                && this.transactionTypeId == other.transactionTypeId
                && this.comment == other.comment
                && this.checked == other.checked
                && this.accountDebitedId == other.accountDebitedId
                && this.accountCreditedId == other.accountCreditedId
                && this.accountDebitedTypeId == other.accountDebitedTypeId
                && this.accountCreditedTypeId == other.accountCreditedTypeId
                && this.accountDebitedCategoryId == other.accountDebitedCategoryId
                && this.accountCreditedCategoryId == other.accountCreditedCategoryId
                && this.groupId == other.groupId
                && this.contactId == other.contactId
                && this.rate.compareTo(other.rate) == 0
                && this.rateDirection == other.rateDirection
                && this.invoiceNumber == other.invoiceNumber
                && this.guid == other.guid
                && this.modified == other.modified
        } else false
    }

    override fun hashCode(): Int {
        return Objects.hash(id,
                amount.stripTrailingZeros(),
                day, month, year, transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId,
                accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                groupId, contactId,
                rate.stripTrailingZeros(),
                rateDirection, invoiceNumber, guid,
                modified
        )
    }

    fun copy(id: Int, accountDebitedId: Int, accountCreditedId: Int, accountDebitedCategoryId: Int,
             accountCreditedCategoryId: Int, groupId: Int, contactId: Int): Transaction {
        return Transaction(
                id = id,
                amount = this.amount,
                day = this.day,
                month = this.month,
                year = this.year,
                transactionTypeId = this.transactionTypeId,
                comment = this.comment,
                checked = this.checked,
                accountDebitedId = accountDebitedId,
                accountCreditedId = accountCreditedId,
                accountDebitedTypeId = this.accountDebitedTypeId,
                accountCreditedTypeId = this.accountCreditedTypeId,
                accountDebitedCategoryId = accountDebitedCategoryId,
                accountCreditedCategoryId = accountCreditedCategoryId,
                groupId = groupId,
                contactId = contactId,
                rate = this.rate,
                rateDirection = this.rateDirection,
                invoiceNumber = this.invoiceNumber,
                guid = this.guid,
                modified = this.modified
        )
    }

    override fun toString() : String {
        return "[Transaction id=$id, amount=$amount], accountDebitedId=$accountDebitedId accountCreditedId=$accountCreditedId"
    }


    companion object {
        val BY_DATE : Comparator<Transaction> = Comparator { x, y ->
            var res = x.year - y.year
            if (res != 0) {
                res
            } else {
                res = x.month - y.month
                if (res != 0) {
                    res
                } else {
                    res = x.day - y.day
                    if (res != 0) {
                        res
                    } else {
                        x.id - y.id
                    }
                }
            }
        }
    }

    class Builder() {
        var id  = 0
        var amount : BigDecimal = BigDecimal.ZERO
        var day = 0
        var month = 0
        var year = 0
        var transactionTypeId = 0
        var comment = ""
        var checked = false
        var accountDebitedId = 0
        var accountCreditedId = 0
        var accountDebitedTypeId = 0
        var accountCreditedTypeId = 0
        var accountDebitedCategoryId = 0
        var accountCreditedCategoryId = 0
        var groupId = 0
        var contactId = 0
        var rate : BigDecimal = BigDecimal.ONE
        var rateDirection = 0
        var invoiceNumber = ""
        var created = 0L
        var modified = 0L

        constructor(t : Transaction?) : this() {
            if (t != null) {
                this.id = t.id
                this.amount = t.amount
                this.day = t.day
                this.month = t.month
                this.year = t.year
                this.transactionTypeId = t.transactionTypeId
                this.comment = t.comment
                this.checked = t.checked
                this.accountDebitedId = t.accountDebitedId
                this.accountCreditedId = t.accountCreditedId
                this.accountDebitedTypeId = t.accountDebitedTypeId
                this.accountCreditedTypeId = t.accountCreditedTypeId
                this.accountDebitedCategoryId = t.accountDebitedCategoryId
                this.accountCreditedCategoryId = t.accountCreditedCategoryId
                this.groupId = t.groupId
                this.contactId = t.contactId
                this.rate = t.rate
                this.rateDirection = t.rateDirection
                this.invoiceNumber = t.invoiceNumber
                this.modified = t.modified
            }
        }

        fun id(id : Int) = apply { this.id = id }
        fun amount(amount : BigDecimal) = apply { this.amount = amount }
        fun day(day : Int) = apply { this.day = day }
        fun month(month : Int) = apply { this.month = month }
        fun year(year : Int) = apply { this.year = year }
        fun transactionTypeId(id: Int) = apply { this.transactionTypeId = id }
        fun transactionType(type: TransactionType) = apply { this.transactionTypeId = type.id }
        fun comment(comment : String) = apply { this.comment = comment }
        fun checked(checked : Boolean) = apply { this.checked = checked }
        fun accountDebitedId(id: Int) = apply { this.accountDebitedId = id }
        fun accountCreditedId(id: Int) = apply { this.accountCreditedId = id }
        fun accountDebitedTypeId(id : Int) = apply { this.accountDebitedTypeId = id }
        fun accountDebitedType(type : CategoryType) = apply { this.accountDebitedTypeId = type.id }
        fun accountCreditedTypeId(id : Int) = apply { this.accountCreditedTypeId = id }
        fun accountCreditedType(type : CategoryType) = apply { this.accountCreditedTypeId = type.id }
        fun accountDebitedCategoryId(id : Int) = apply { this.accountDebitedCategoryId = id }
        fun accountCreditedCategoryId(id : Int) = apply { this.accountCreditedCategoryId = id }
        fun groupId(id : Int) = apply { this.groupId = id }
        fun contactId(id : Int) = apply { this.contactId = id }
        fun rate(rate : BigDecimal) = apply { this.rate = rate }
        fun rateDirection(rateDirection : Int) = apply { this.rateDirection = rateDirection }
        fun invoiceNumber(invoiceNumber : String) = apply { this.invoiceNumber = invoiceNumber }

        fun build() : Transaction {
            if (transactionTypeId == 0) {
                transactionTypeId = TransactionType.UNDEFINED.id
            }

            if (id == 0
                    || accountDebitedId == 0
                    || accountCreditedId == 0
                    || accountDebitedTypeId == 0
                    || accountCreditedTypeId == 0
                    || accountDebitedCategoryId == 0
                    || accountCreditedCategoryId == 0) {
                throw IllegalStateException()
            }

            return Transaction(
                    id = id,
                    amount = amount,
                    day = day,
                    month = month,
                    year = year,
                    transactionTypeId = transactionTypeId,
                    comment = comment,
                    checked = checked,
                    accountDebitedId = accountDebitedId,
                    accountCreditedId = accountCreditedId,
                    accountDebitedTypeId = accountDebitedTypeId,
                    accountCreditedTypeId = accountCreditedTypeId,
                    accountDebitedCategoryId = accountDebitedCategoryId,
                    accountCreditedCategoryId = accountCreditedCategoryId,
                    groupId = groupId,
                    contactId = contactId,
                    rate = rate,
                    rateDirection = rateDirection,
                    invoiceNumber = invoiceNumber,
                    guid = UUID.randomUUID().toString(),
                    modified = System.currentTimeMillis()
            )
        }
    }
}