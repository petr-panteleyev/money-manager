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

import org.panteleyev.money.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.BaseTestUtils.newTransaction;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.BaseTestUtils.randomDay;
import static org.panteleyev.money.BaseTestUtils.randomMonth;
import static org.panteleyev.money.BaseTestUtils.randomString;
import static org.panteleyev.money.BaseTestUtils.randomTransactionType;
import static org.panteleyev.money.BaseTestUtils.randomYear;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestTransaction extends BaseTest {
    @Test
    public void testEquals() {
        var amount = randomBigDecimal();
        var day = randomDay();
        var month = randomMonth();
        var year = randomYear();
        var transactionTypeId = randomTransactionType().getId();
        var comment = UUID.randomUUID().toString();
        var checked = RANDOM.nextBoolean();
        var accountDebitedUuid = UUID.randomUUID();
        var accountCreditedUuid = UUID.randomUUID();
        var accountDebitedTypeId = randomCategoryType().getId();
        var accountCreditedTypeId = randomCategoryType().getId();
        var accountDebitedCategoryUuid = UUID.randomUUID();
        var accountCreditedCategoryUuid = UUID.randomUUID();
        var contactUuid = UUID.randomUUID();
        var rate = randomBigDecimal();
        var rateDirection = RANDOM.nextInt();
        var invoiceNumber = UUID.randomUUID().toString();
        var guid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();
        var parentUuid = UUID.randomUUID();
        var detailed = RANDOM.nextBoolean();

        var t1 = new Transaction.Builder()
            .amount(amount)
            .day(day)
            .month(month)
            .year(year)
            .transactionTypeId(transactionTypeId)
            .comment(comment)
            .checked(checked)
            .accountDebitedUuid(accountDebitedUuid)
            .accountCreditedUuid(accountCreditedUuid)
            .accountDebitedTypeId(accountDebitedTypeId)
            .accountCreditedTypeId(accountCreditedTypeId)
            .accountDebitedCategoryUuid(accountDebitedCategoryUuid)
            .accountCreditedCategoryUuid(accountCreditedCategoryUuid)
            .contactUuid(contactUuid)
            .rate(rate)
            .rateDirection(rateDirection)
            .invoiceNumber(invoiceNumber)
            .guid(guid)
            .created(created)
            .modified(modified)
            .parentUuid(parentUuid)
            .detailed(detailed)
            .build();

        var t2 = new Transaction.Builder()
            .amount(amount)
            .day(day)
            .month(month)
            .year(year)
            .transactionTypeId(transactionTypeId)
            .comment(comment)
            .checked(checked)
            .accountDebitedUuid(accountDebitedUuid)
            .accountCreditedUuid(accountCreditedUuid)
            .accountDebitedTypeId(accountDebitedTypeId)
            .accountCreditedTypeId(accountCreditedTypeId)
            .accountDebitedCategoryUuid(accountDebitedCategoryUuid)
            .accountCreditedCategoryUuid(accountCreditedCategoryUuid)
            .contactUuid(contactUuid)
            .rate(rate)
            .rateDirection(rateDirection)
            .invoiceNumber(invoiceNumber)
            .guid(guid)
            .created(created)
            .modified(modified)
            .parentUuid(parentUuid)
            .detailed(detailed)
            .build();

        assertEquals(t2, t1);
        assertEquals(t2.hashCode(), t1.hashCode());
    }

    @Test
    public void testCheck() {
        var t1 = new Transaction.Builder()
            .amount(randomBigDecimal())
            .day(randomDay())
            .month(randomMonth())
            .year(randomYear())
            .transactionTypeId(randomTransactionType().getId())
            .comment(randomString())
            .checked(randomBoolean())
            .accountDebitedUuid(UUID.randomUUID())
            .accountCreditedUuid(UUID.randomUUID())
            .accountDebitedTypeId(randomCategoryType().getId())
            .accountCreditedTypeId(randomCategoryType().getId())
            .accountDebitedCategoryUuid(UUID.randomUUID())
            .accountCreditedCategoryUuid(UUID.randomUUID())
            .contactUuid(UUID.randomUUID())
            .rate(randomBigDecimal())
            .rateDirection(RANDOM.nextInt())
            .invoiceNumber(randomString())
            .guid(UUID.randomUUID())
            .created(System.currentTimeMillis())
            .modified(System.currentTimeMillis())
            .parentUuid(UUID.randomUUID())
            .detailed(randomBoolean())
            .build();

        var t2 = t1.check(!t1.getChecked());

        assertEquals(t2.getAmount(), t1.getAmount());
        assertEquals(t2.getDay(), t1.getDay());
        assertEquals(t2.getMonth(), t1.getMonth());
        assertEquals(t2.getYear(), t1.getYear());
        assertEquals(t2.getTransactionTypeId(), t1.getTransactionTypeId());
        assertEquals(t2.getComment(), t1.getComment());
        assertEquals(t2.getChecked(), !t1.getChecked());
        assertEquals(t2.getAccountDebitedUuid(), t1.getAccountDebitedUuid());
        assertEquals(t2.getAccountCreditedUuid(), t1.getAccountCreditedUuid());
        assertEquals(t2.getAccountDebitedType(), t1.getAccountDebitedType());
        assertEquals(t2.getAccountCreditedType(), t1.getAccountCreditedType());
        assertEquals(t2.getContactUuid(), t1.getContactUuid());
        assertEquals(t2.getRate(), t1.getRate());
        assertEquals(t2.getRateDirection(), t1.getRateDirection());
        assertEquals(t2.getInvoiceNumber(), t1.getInvoiceNumber());
        assertEquals(t2.getParentUuid(), t1.getParentUuid());
        assertEquals(t2.getUuid(), t1.getUuid());
        assertEquals(t1.getCreated(), t2.getCreated());
        assertTrue(t2.getModified() >= t1.getModified());
    }

    @Test
    public void testBuilder() {
        var original = newTransaction();

        // Builder copy
        var builderCopy = new Transaction.Builder(original).build();
        assertEquals(builderCopy, original);
    }
}
