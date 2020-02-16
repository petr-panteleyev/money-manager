package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;
import static org.panteleyev.money.test.BaseTestUtils.newTransaction;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.test.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.test.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.test.BaseTestUtils.randomDay;
import static org.panteleyev.money.test.BaseTestUtils.randomMonth;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.panteleyev.money.test.BaseTestUtils.randomTransactionType;
import static org.panteleyev.money.test.BaseTestUtils.randomYear;
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
