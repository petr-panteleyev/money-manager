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
import java.io.InputStream
import java.math.BigDecimal
import java.util.Random
import java.util.UUID
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

open class BaseTest {
    fun randomId(): Int = RANDOM.nextInt(Integer.MAX_VALUE) + 1
    fun randomDay(): Int = 1 + RANDOM.nextInt(31)
    fun randomMonth(): Int = 1 + RANDOM.nextInt(12)
    fun randomYear(): Int = 1 + RANDOM.nextInt(3000)

    fun randomCategoryType(): CategoryType {
        val id = 1 + RANDOM.nextInt(CategoryType.values().size - 1)
        return CategoryType.get(id)
    }

    fun randomContactType(): ContactType {
        val id = 1 + RANDOM.nextInt(ContactType.values().size - 1)
        return ContactType.get(id)
    }

    fun randomTransactionType(): TransactionType {
        while (true) {
            val id = 1 + RANDOM.nextInt(TransactionType.values().size - 1)
            val type = TransactionType.get(id)
            if (!type.separator) {
                return type
            }
        }
    }

    internal fun newAccount(id: Int = randomId(), type: CategoryType = randomCategoryType(),
                            categoryId: Int = randomId(), currencyId: Int = randomId()): Account {
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
                RANDOM.nextBoolean(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    internal fun newAccount(id: Int = randomId(), category: Category, currency: Currency): Account {
        return Account(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                category.type.id,
                category.id,
                currency.id,
                RANDOM.nextBoolean(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    internal fun newCategory(id: Int = randomId(), type: CategoryType = randomCategoryType()): Category {
        return Category(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                type.id,
                RANDOM.nextBoolean(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    internal fun newCurrency(id: Int = randomId()): Currency {
        return Currency(
                id,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                RANDOM.nextInt(2),
                RANDOM.nextBoolean(),
                RANDOM.nextBoolean(),
                BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                RANDOM.nextInt(2),
                RANDOM.nextBoolean(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    internal fun newContact(id: Int = randomId()): Contact {
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
                UUID.randomUUID().toString(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    internal fun newTransactionGroup(id: Int = randomId()): TransactionGroup {
        return TransactionGroup(id = id,
                day = randomDay(),
                month = randomMonth(),
                year = randomYear(),
                expanded = RANDOM.nextBoolean(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    fun newTransaction(
            id: Int = randomId(),
            type: TransactionType = randomTransactionType(),
            accountDebitedId: Int = randomId(),
            accountCreditedId: Int = randomId(),
            accountDebitedType: CategoryType = randomCategoryType(),
            accountCreditedType: CategoryType = randomCategoryType(),
            accountDebitedCategoryId: Int = randomId(),
            accountCreditedCategoryId: Int = randomId(),
            groupId: Int = randomId(),
            contactId: Int = randomId()
    ): Transaction {
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
                contactId
        )
    }

    fun newTransaction(
            id: Int = randomId(),
            type: TransactionType = randomTransactionType(),
            accountDebited: Account,
            accountCredited: Account,
            group: TransactionGroup? = null,
            contact: Contact? = null
    ): Transaction {
        return Transaction(
                id = id,
                amount = BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                day = randomDay(),
                month = randomMonth(),
                year = randomYear(),
                transactionTypeId = type.id,
                comment = UUID.randomUUID().toString(),
                checked = RANDOM.nextBoolean(),
                accountDebitedId = accountDebited.id,
                accountCreditedId = accountCredited.id,
                accountDebitedTypeId = accountDebited.typeId,
                accountCreditedTypeId = accountCredited.typeId,
                accountDebitedCategoryId = accountDebited.categoryId,
                accountCreditedCategoryId = accountCredited.categoryId,
                groupId = group?.id ?: 0,
                contactId = contact?.id ?: 0,
                rate = BigDecimal(RANDOM.nextDouble()).setScale(6, BigDecimal.ROUND_HALF_UP),
                rateDirection = 0,
                invoiceNumber = UUID.randomUUID().toString(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
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
        return Transaction(id = id,
                amount = amount,
                day = randomDay(),
                month = randomMonth(),
                year = randomYear(),
                transactionTypeId = type.id,
                comment = UUID.randomUUID().toString(),
                checked = RANDOM.nextBoolean(),
                accountDebitedId = accountDebitedId,
                accountCreditedId = accountCreditedId,
                accountDebitedTypeId = accountDebitedType.id,
                accountCreditedTypeId = accountCreditedType.id,
                accountDebitedCategoryId = accountDebitedCategoryId,
                accountCreditedCategoryId = accountCreditedCategoryId,
                groupId = groupId,
                contactId = contactId,
                rate = rate,
                rateDirection = RANDOM.nextInt(2),
                invoiceNumber = UUID.randomUUID().toString(),
                guid = UUID.randomUUID().toString(),
                modified = System.currentTimeMillis()
        )
    }

    protected fun validateXML(input: InputStream) {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val schema = schemaFactory.newSchema(this::class.java.getResource("/org/panteleyev/money/xml/money.xsd"))
        val validator = schema.newValidator()
        validator.validate(StreamSource(input))
    }

    companion object {
        internal val RANDOM = Random(System.currentTimeMillis())
    }
}
