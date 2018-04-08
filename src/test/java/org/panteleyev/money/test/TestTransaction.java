/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.test.BaseTestUtils.randomDay;
import static org.panteleyev.money.test.BaseTestUtils.randomId;
import static org.panteleyev.money.test.BaseTestUtils.randomMonth;
import static org.panteleyev.money.test.BaseTestUtils.randomYear;

public class TestTransaction extends BaseTest {
    @Test
    public void testEquals() {
        int id = randomId();
        BigDecimal amount = randomBigDecimal();
        int day = randomDay();
        int month = randomMonth();
        int year = randomYear();
        int transactionTypeId = randomTransactionType().getId();
        String comment = UUID.randomUUID().toString();
        boolean checked = RANDOM.nextBoolean();
        int accountDebitedId = randomId();
        int accountCreditedId = randomId();
        int accountDebitedTypeId = randomCategoryType().getId();
        int accountCreditedTypeId = randomCategoryType().getId();
        int accountDebitedCategoryId = randomId();
        int accountCreditedCategoryId = randomId();
        int groupId = randomId();
        int contactId = randomId();
        BigDecimal rate = randomBigDecimal();
        int rateDirection = RANDOM.nextInt();
        String invoiceNumber = UUID.randomUUID().toString();
        String guid = UUID.randomUUID().toString();
        long modified = System.currentTimeMillis();

        Transaction t1 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                groupId, contactId, rate, rateDirection, invoiceNumber,
                guid, modified);

        Transaction t2 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                groupId, contactId, rate, rateDirection, invoiceNumber,
                guid, modified);

        Assert.assertEquals(t2, t1);
        Assert.assertEquals(t2.hashCode(), t1.hashCode());
    }

    @Test
    public void testCheck() {
        int id = randomId();
        BigDecimal amount = randomBigDecimal();
        int day = randomDay();
        int month = randomMonth();
        int year = randomYear();
        int transactionTypeId = randomTransactionType().getId();
        String comment = UUID.randomUUID().toString();
        boolean checked = RANDOM.nextBoolean();
        int accountDebitedId = randomId();
        int accountCreditedId = randomId();
        int accountDebitedTypeId = randomCategoryType().getId();
        int accountCreditedTypeId = randomCategoryType().getId();
        int accountDebitedCategoryId = randomId();
        int accountCreditedCategoryId = randomId();
        int groupId = randomId();
        int contactId = randomId();
        BigDecimal rate = randomBigDecimal();
        int rateDirection = RANDOM.nextInt();
        String invoiceNumber = UUID.randomUUID().toString();
        String guid = UUID.randomUUID().toString();
        long modified = System.currentTimeMillis();

        Transaction t1 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                groupId, contactId, rate, rateDirection, invoiceNumber,
                guid, modified);

        Transaction t2 = t1.check(!t1.getChecked());

        Assert.assertEquals(t2.getId(), t1.getId());
        Assert.assertEquals(t2.getAmount(), t1.getAmount());
        Assert.assertEquals(t2.getDay(), t1.getDay());
        Assert.assertEquals(t2.getMonth(), t1.getMonth());
        Assert.assertEquals(t2.getYear(), t1.getYear());
        Assert.assertEquals(t2.getTransactionTypeId(), t1.getTransactionTypeId());
        Assert.assertEquals(t2.getComment(), t1.getComment());
        Assert.assertEquals(t2.getChecked(), !t1.getChecked());
        Assert.assertEquals(t2.getAccountDebitedId(), t1.getAccountDebitedId());
        Assert.assertEquals(t2.getAccountCreditedId(), t1.getAccountCreditedId());
        Assert.assertEquals(t2.getAccountDebitedType(), t1.getAccountDebitedType());
        Assert.assertEquals(t2.getAccountCreditedType(), t1.getAccountCreditedType());
        Assert.assertEquals(t2.getAccountDebitedCategoryId(), t1.getAccountDebitedCategoryId());
        Assert.assertEquals(t2.getAccountCreditedCategoryId(), t1.getAccountCreditedCategoryId());
        Assert.assertEquals(t2.getGroupId(), t1.getGroupId());
        Assert.assertEquals(t2.getContactId(), t1.getContactId());
        Assert.assertEquals(t2.getRate(), t1.getRate());
        Assert.assertEquals(t2.getRateDirection(), t1.getRateDirection());
        Assert.assertEquals(t2.getInvoiceNumber(), t1.getInvoiceNumber());
        Assert.assertEquals(t2.getGuid(), t1.getGuid());
        Assert.assertTrue(t2.getModified() >= t1.getModified());
    }

    @Test
    public void testSetGroupId() {
        int id = randomId();
        BigDecimal amount = randomBigDecimal();
        int day = randomDay();
        int month = randomMonth();
        int year = randomYear();
        int transactionTypeId = randomTransactionType().getId();
        String comment = UUID.randomUUID().toString();
        boolean checked = RANDOM.nextBoolean();
        int accountDebitedId = randomId();
        int accountCreditedId = randomId();
        int accountDebitedTypeId = randomCategoryType().getId();
        int accountCreditedTypeId = randomCategoryType().getId();
        int accountDebitedCategoryId = randomId();
        int accountCreditedCategoryId = randomId();
        int groupId = randomId();
        int contactId = randomId();
        BigDecimal rate = randomBigDecimal();
        int rateDirection = RANDOM.nextInt();
        String invoiceNumber = UUID.randomUUID().toString();
        String guid = UUID.randomUUID().toString();
        long modified = System.currentTimeMillis();

        Transaction t1 = new Transaction(id, amount, day, month, year,
                transactionTypeId, comment, checked,
                accountDebitedId, accountCreditedId, accountDebitedTypeId, accountCreditedTypeId,
                accountDebitedCategoryId, accountCreditedCategoryId,
                groupId, contactId, rate, rateDirection, invoiceNumber,
                guid, modified);

        int newGroupId = RANDOM.nextInt();
        Transaction t2 = t1.setGroupId(newGroupId);

        Assert.assertEquals(t2.getId(), t2.getId());
        Assert.assertEquals(t2.getAmount(), t2.getAmount());
        Assert.assertEquals(t2.getDay(), t2.getDay());
        Assert.assertEquals(t2.getMonth(), t2.getMonth());
        Assert.assertEquals(t2.getYear(), t2.getYear());
        Assert.assertEquals(t2.getTransactionTypeId(), t2.getTransactionTypeId());
        Assert.assertEquals(t2.getComment(), t2.getComment());
        Assert.assertEquals(t2.getChecked(), t2.getChecked());
        Assert.assertEquals(t2.getAccountDebitedId(), t2.getAccountDebitedId());
        Assert.assertEquals(t2.getAccountCreditedId(), t2.getAccountCreditedId());
        Assert.assertEquals(t2.getAccountDebitedType(), t2.getAccountDebitedType());
        Assert.assertEquals(t2.getAccountCreditedType(), t2.getAccountCreditedType());
        Assert.assertEquals(t2.getAccountDebitedCategoryId(), t2.getAccountDebitedCategoryId());
        Assert.assertEquals(t2.getAccountCreditedCategoryId(), t2.getAccountCreditedCategoryId());
        Assert.assertEquals(t2.getGroupId(), newGroupId);
        Assert.assertEquals(t2.getContactId(), t2.getContactId());
        Assert.assertEquals(t2.getRate(), t2.getRate());
        Assert.assertEquals(t2.getRateDirection(), t2.getRateDirection());
        Assert.assertEquals(t2.getInvoiceNumber(), t2.getInvoiceNumber());
        Assert.assertEquals(t2.getGuid(), t2.getGuid());
        Assert.assertTrue(t2.getModified() >= t2.getModified());
    }
}
