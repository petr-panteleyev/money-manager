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

import java.math.BigDecimal
import java.util.UUID

class SplitTransaction(id : Int, group : List<Transaction>) : Transaction (
        id = group[0].id,
        amount = calculateTotal(group),
        day = group[0].day, month = group[0].month, year = group[0].year,
        transactionTypeId = calculateTransactionType(group).id,
        comment = calculateComment(group),
        checked = false,
        accountDebitedId = group[0].accountDebitedId,
        accountCreditedId = 0,
        accountDebitedTypeId = CategoryType.BANKS_AND_CASH.id,
        accountCreditedTypeId = CategoryType.BANKS_AND_CASH.id,
        accountDebitedCategoryId = 0,
        accountCreditedCategoryId = 0,
        groupId = id,
        contactId = 0,
        rate = BigDecimal.ONE,
        rateDirection = 0,
        invoiceNumber = "",
        guid = UUID.randomUUID().toString(),
        modified = 0L
) {
    val contactString : String
    val accountCreditedString : String

    init {
        contactString = group.map { it.contactId }
                .distinct()
                .map { MoneyDAO.getContact(it) }
                .map { it?.name }
                .joinToString(separator = " + ")

        val accCredIDs : List<Int> = group
                .map { it.accountCreditedId }
                .distinct()

        accountCreditedString = if (accCredIDs.size == 1) {
            MoneyDAO.getAccount(accCredIDs[0])?.name?: ""
        } else {
            accCredIDs.size.toString() + " accounts"
        }
    }

    companion object {
        private fun calculateTotal(group: List<Transaction>): BigDecimal =
                group.map { it.signedAmount }.reduce { acc, next -> acc.add(next) }

        private fun calculateTransactionType(group: List<Transaction>): TransactionType =
            group.map{ it.transactionType }
                    .filter { it != TransactionType.UNDEFINED }
                    .firstOrNull() ?: TransactionType.UNDEFINED

        private fun calculateComment(group: List<Transaction>): String =
            group.map { it.comment }
                    .filter { c -> !c.isEmpty() }
                    .firstOrNull() ?: ""
    }
}