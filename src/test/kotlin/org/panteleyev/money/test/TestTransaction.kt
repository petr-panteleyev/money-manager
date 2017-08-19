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

import org.panteleyev.money.persistence.Transaction
import org.testng.Assert
import org.testng.annotations.Test
import java.math.BigDecimal
import java.util.UUID

class TestTransaction : BaseTest() {
    @Test
    fun testEquals() {
        val id = BaseTest.RANDOM.nextInt()
        val amount = BigDecimal(BaseTest.RANDOM.nextDouble())
        val day = BaseTest.RANDOM.nextInt()
        val month = BaseTest.RANDOM.nextInt()
        val year = BaseTest.RANDOM.nextInt()
        val transactionTypeId = randomTransactionType().id
        val comment = UUID.randomUUID().toString()
        val checked = BaseTest.RANDOM.nextBoolean()
        val accountDebitedId = BaseTest.RANDOM.nextInt()
        val accountCreditedId = BaseTest.RANDOM.nextInt()
        val accountDebitedTypeId = randomCategoryType().id
        val accountCreditedTypeId = randomCategoryType().id
        val accountDebitedCategoryId = BaseTest.RANDOM.nextInt()
        val accountCreditedCategoryId = BaseTest.RANDOM.nextInt()
        val groupId = BaseTest.RANDOM.nextInt()
        val contactId = BaseTest.RANDOM.nextInt()
        val rate = BigDecimal(BaseTest.RANDOM.nextDouble())
        val rateDirection = BaseTest.RANDOM.nextInt()
        val invoiceNumber = UUID.randomUUID().toString()
        val guid = UUID.randomUUID().toString()
        val modified = System.currentTimeMillis()

        val t1 = Transaction(id = id,
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
                guid = guid,
                modified = modified
        )

        val t2 = Transaction(id = id,
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
                guid = guid,
                modified = modified
        )

        Assert.assertEquals(t1, t2)
        Assert.assertEquals(t1.hashCode(), t2.hashCode())
    }
}
