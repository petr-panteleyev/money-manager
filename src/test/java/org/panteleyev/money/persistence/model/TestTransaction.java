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
import static org.panteleyev.money.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.BaseTestUtils.randomDay;
import static org.panteleyev.money.BaseTestUtils.randomId;
import static org.panteleyev.money.BaseTestUtils.randomMonth;
import static org.panteleyev.money.BaseTestUtils.randomTransactionType;
import static org.panteleyev.money.BaseTestUtils.randomYear;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestTransaction extends BaseTest {
    @Test
    public void testEquals() {
        var id = randomId();
        var amount = randomBigDecimal();
        var day = randomDay();
        var month = randomMonth();
        var year = randomYear();
        var transactionTypeId = randomTransactionType().getId();
        var comment = UUID.randomUUID().toString();
        var checked = RANDOM.nextBoolean();
        var accountDebitedId = randomId();
        var accountCreditedId = randomId();
        var accountDebitedTypeId = randomCategoryType().getId();
        var accountCreditedTypeId = randomCategoryType().getId();
        var accountDebitedCategoryId = randomId();
        var accountCreditedCategoryId = randomId();
        var contactId = randomId();
        var rate = randomBigDecimal();
        var rateDirection = RANDOM.nextInt();
        var invoiceNumber = UUID.randomUUID().toString();
        var guid = UUID.randomUUID().toString();
        var modified = System.currentTimeMillis();
        var parentId = randomId();
        var detailed = RANDOM.nextBoolean();

        var t1 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                contactId, rate, rateDirection, invoiceNumber,
                guid, modified, parentId, detailed);

        var t2 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                contactId, rate, rateDirection, invoiceNumber,
                guid, modified, parentId, detailed);

        assertEquals(t2, t1);
        assertEquals(t2.hashCode(), t1.hashCode());
    }

    @Test
    public void testCheck() {
        var id = randomId();
        var amount = randomBigDecimal();
        var day = randomDay();
        var month = randomMonth();
        var year = randomYear();
        var transactionTypeId = randomTransactionType().getId();
        var comment = UUID.randomUUID().toString();
        var checked = RANDOM.nextBoolean();
        var accountDebitedId = randomId();
        var accountCreditedId = randomId();
        var accountDebitedTypeId = randomCategoryType().getId();
        var accountCreditedTypeId = randomCategoryType().getId();
        var accountDebitedCategoryId = randomId();
        var accountCreditedCategoryId = randomId();
        var contactId = randomId();
        var rate = randomBigDecimal();
        var rateDirection = RANDOM.nextInt();
        var invoiceNumber = UUID.randomUUID().toString();
        var guid = UUID.randomUUID().toString();
        var modified = System.currentTimeMillis();
        int parentId = randomId();
        boolean detailed = RANDOM.nextBoolean();

        Transaction t1 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                contactId, rate, rateDirection, invoiceNumber,
                guid, modified, parentId, detailed);

        var t2 = t1.check(!t1.getChecked());

        assertEquals(t2.getId(), t1.getId());
        assertEquals(t2.getAmount(), t1.getAmount());
        assertEquals(t2.getDay(), t1.getDay());
        assertEquals(t2.getMonth(), t1.getMonth());
        assertEquals(t2.getYear(), t1.getYear());
        assertEquals(t2.getTransactionTypeId(), t1.getTransactionTypeId());
        assertEquals(t2.getComment(), t1.getComment());
        assertEquals(t2.getChecked(), !t1.getChecked());
        assertEquals(t2.getAccountDebitedId(), t1.getAccountDebitedId());
        assertEquals(t2.getAccountCreditedId(), t1.getAccountCreditedId());
        assertEquals(t2.getAccountDebitedType(), t1.getAccountDebitedType());
        assertEquals(t2.getAccountCreditedType(), t1.getAccountCreditedType());
        assertEquals(t2.getAccountDebitedCategoryId(), t1.getAccountDebitedCategoryId());
        assertEquals(t2.getAccountCreditedCategoryId(), t1.getAccountCreditedCategoryId());
        assertEquals(t2.getContactId(), t1.getContactId());
        assertEquals(t2.getRate(), t1.getRate());
        assertEquals(t2.getRateDirection(), t1.getRateDirection());
        assertEquals(t2.getInvoiceNumber(), t1.getInvoiceNumber());
        assertEquals(t2.getGuid(), t1.getGuid());
        assertTrue(t2.getModified() >= t1.getModified());
    }

    @Test
    public void testBuilder() {
        var original = newTransaction(randomId());

        // Builder copy
        var builderCopy = new Transaction.Builder(original).build();
        assertEquals(builderCopy, original);
    }
}
