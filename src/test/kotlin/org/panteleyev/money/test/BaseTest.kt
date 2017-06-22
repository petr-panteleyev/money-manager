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

package org.panteleyev.money.test

import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.ContactType
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup
import org.panteleyev.money.persistence.TransactionType
import java.math.BigDecimal
import java.util.Random
import java.util.UUID

open class BaseTest {

    fun randomCategoryType(): CategoryType {
        val id = 1 + RANDOM.nextInt(CategoryType.values().size - 1)
        return CategoryType.get(id)
    }

    fun randomContactType(): ContactType {
        val id = 1 + RANDOM.nextInt(ContactType.values().size - 1)
        return ContactType.get(id)
    }

    fun randomTransactionType(): TransactionType {
        val id = 1 + RANDOM.nextInt(TransactionType.values().size - 1)
        return TransactionType.get(id)
    }

    internal fun newAccount(id: Int = RANDOM.nextInt(), type: CategoryType = randomCategoryType(),
                            categoryId: Int = RANDOM.nextInt(), currencyId: Int = RANDOM.nextInt()): Account {
        return Account(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                type.id,
                categoryId,
                currencyId,
                RANDOM.nextBoolean()
        )
    }

    internal fun newCategory(id: Int = RANDOM.nextInt(), type: CategoryType = randomCategoryType()): Category {
        return Category(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                type.id,
                RANDOM.nextBoolean()
        )
    }

    internal fun newCurrency(id: Int = RANDOM.nextInt()): Currency {
        return Currency(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                RANDOM.nextInt(),
                RANDOM.nextBoolean(),
                RANDOM.nextBoolean(),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                RANDOM.nextInt(),
                RANDOM.nextBoolean()
        )
    }

    internal fun newContact(id: Int = RANDOM.nextInt()): Contact {
        return Contact(
                id,
                UUID.randomUUID().toString(),
                randomContactType().id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        )
    }

    internal fun newTransactionGroup(id: Int = RANDOM.nextInt()): TransactionGroup {
        return TransactionGroup(id,
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                RANDOM.nextBoolean())
    }

    fun newTransaction(
            id: Int,
            type: TransactionType,
            accountDebitedId: Int,
            accountCreditedId: Int,
            accountDebitedType: CategoryType,
            accountCreditedType: CategoryType,
            accountDebitedCategoryId: Int,
            accountCreditedCategoryId: Int,
            groupId: Int,
            contactId: Int): Transaction {
        return newTransaction(id,
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                type,
                accountDebitedId,
                accountCreditedId,
                accountDebitedType,
                accountCreditedType,
                accountDebitedCategoryId,
                accountCreditedCategoryId,
                groupId,
                contactId)
    }

    fun newTransaction(
            id: Int,
            amount: BigDecimal,
            rate: BigDecimal,
            type: TransactionType,
            accountDebitedId: Int,
            accountCreditedId: Int,
            accountDebitedType: CategoryType,
            accountCreditedType: CategoryType,
            accountDebitedCategoryId: Int,
            accountCreditedCategoryId: Int,
            groupId: Int,
            contactId: Int): Transaction {
        return Transaction(id,
                amount,
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                RANDOM.nextInt(),
                type.id,
                UUID.randomUUID().toString(),
                RANDOM.nextBoolean(),
                accountDebitedId,
                accountCreditedId,
                accountDebitedType.id,
                accountCreditedType.id,
                accountDebitedCategoryId,
                accountCreditedCategoryId,
                groupId,
                contactId,
                rate,
                RANDOM.nextInt(),
                UUID.randomUUID().toString()
        )
    }

    companion object {
        internal val RANDOM = Random(System.currentTimeMillis())
    }
}
