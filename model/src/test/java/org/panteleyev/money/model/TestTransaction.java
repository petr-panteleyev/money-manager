/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.model.BaseTestUtils.RANDOM;
import static org.panteleyev.money.model.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.model.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.model.BaseTestUtils.randomTransactionType;

public class TestTransaction {

    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var amount = randomBigDecimal();
        var creditAmount = randomBigDecimal();
        var transactionDate = LocalDate.now();
        var type = randomTransactionType();
        var comment = BaseTestUtils.randomString();
        var checked = BaseTestUtils.randomBoolean();
        var accountDebitedUuid = UUID.randomUUID();
        var accountCreditedUuid = UUID.randomUUID();
        var accountDebitedType = randomCategoryType();
        var accountCreditedType = randomCategoryType();
        var accountDebitedCategoryUuid = UUID.randomUUID();
        var accountCreditedCategoryUuid = UUID.randomUUID();
        var contactUuid = UUID.randomUUID();
        var invoiceNumber = BaseTestUtils.randomString();
        var parentUuid = UUID.randomUUID();
        var detailed = BaseTestUtils.randomBoolean();
        var statementDate = LocalDate.now();
        var created = System.currentTimeMillis();
        var modified = created + 1000;

        return List.of(
                Arguments.of(
                        new Transaction.Builder()
                                .uuid(uuid)
                                .amount(amount)
                                .creditAmount(creditAmount)
                                .transactionDate(transactionDate)
                                .type(type)
                                .comment(comment)
                                .checked(checked)
                                .accountDebitedUuid(accountDebitedUuid)
                                .accountCreditedUuid(accountCreditedUuid)
                                .accountDebitedType(accountDebitedType)
                                .accountCreditedType(accountCreditedType)
                                .accountDebitedCategoryUuid(accountDebitedCategoryUuid)
                                .accountCreditedCategoryUuid(accountCreditedCategoryUuid)
                                .contactUuid(contactUuid)
                                .invoiceNumber(invoiceNumber)
                                .parentUuid(parentUuid)
                                .detailed(detailed)
                                .statementDate(statementDate)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new Transaction(
                                uuid, amount, creditAmount, transactionDate,
                                type, comment, checked, accountDebitedUuid, accountCreditedUuid,
                                accountDebitedType, accountCreditedType,
                                accountDebitedCategoryUuid, accountCreditedCategoryUuid,
                                contactUuid, invoiceNumber, parentUuid,
                                detailed, statementDate, created, modified
                        )
                ),
                Arguments.of(
                        new Transaction(
                                uuid, amount, creditAmount, transactionDate,
                                type, null, checked, accountDebitedUuid, accountCreditedUuid,
                                accountDebitedType, accountCreditedType,
                                accountDebitedCategoryUuid, accountCreditedCategoryUuid,
                                contactUuid, null, null,
                                detailed, null, created, modified
                        ),
                        new Transaction(
                                uuid, amount, creditAmount, transactionDate,
                                type, "", checked, accountDebitedUuid, accountCreditedUuid,
                                accountDebitedType, accountCreditedType,
                                accountDebitedCategoryUuid, accountCreditedCategoryUuid,
                                contactUuid, "", null,
                                detailed, null, created, modified
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(Transaction actual, Transaction expected) {
        assertEquals(actual, expected);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }

    @Test
    public void testEquals() {
        var amount = randomBigDecimal();
        var creditAmount = randomBigDecimal();
        var transactionDate = LocalDate.now();
        var type = randomTransactionType();
        var comment = UUID.randomUUID().toString();
        var checked = RANDOM.nextBoolean();
        var accountDebitedUuid = UUID.randomUUID();
        var accountCreditedUuid = UUID.randomUUID();
        var accountDebitedType = randomCategoryType();
        var accountCreditedType = randomCategoryType();
        var accountDebitedCategoryUuid = UUID.randomUUID();
        var accountCreditedCategoryUuid = UUID.randomUUID();
        var contactUuid = UUID.randomUUID();
        var invoiceNumber = UUID.randomUUID().toString();
        var guid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();
        var parentUuid = UUID.randomUUID();
        var detailed = RANDOM.nextBoolean();
        var statementDate = LocalDate.now();

        var t1 = new Transaction.Builder()
                .amount(amount)
                .creditAmount(creditAmount)
                .transactionDate(transactionDate)
                .type(type)
                .comment(comment)
                .checked(checked)
                .accountDebitedUuid(accountDebitedUuid)
                .accountCreditedUuid(accountCreditedUuid)
                .accountDebitedType(accountDebitedType)
                .accountCreditedType(accountCreditedType)
                .accountDebitedCategoryUuid(accountDebitedCategoryUuid)
                .accountCreditedCategoryUuid(accountCreditedCategoryUuid)
                .contactUuid(contactUuid)
                .invoiceNumber(invoiceNumber)
                .uuid(guid)
                .created(created)
                .modified(modified)
                .parentUuid(parentUuid)
                .detailed(detailed)
                .statementDate(statementDate)
                .build();

        var t2 = new Transaction.Builder()
                .amount(amount)
                .creditAmount(creditAmount)
                .transactionDate(transactionDate)
                .type(type)
                .comment(comment)
                .checked(checked)
                .accountDebitedUuid(accountDebitedUuid)
                .accountCreditedUuid(accountCreditedUuid)
                .accountDebitedType(accountDebitedType)
                .accountCreditedType(accountCreditedType)
                .accountDebitedCategoryUuid(accountDebitedCategoryUuid)
                .accountCreditedCategoryUuid(accountCreditedCategoryUuid)
                .contactUuid(contactUuid)
                .invoiceNumber(invoiceNumber)
                .uuid(guid)
                .created(created)
                .modified(modified)
                .parentUuid(parentUuid)
                .detailed(detailed)
                .statementDate(statementDate)
                .build();

        assertEquals(t2, t1);
        assertEquals(t2.hashCode(), t1.hashCode());
    }

    @Test
    public void testCheck() {
        var t1 = new Transaction.Builder()
                .amount(randomBigDecimal())
                .creditAmount(randomBigDecimal())
                .transactionDate(LocalDate.now())
                .type(randomTransactionType())
                .comment(BaseTestUtils.randomString())
                .checked(BaseTestUtils.randomBoolean())
                .accountDebitedUuid(UUID.randomUUID())
                .accountCreditedUuid(UUID.randomUUID())
                .accountDebitedType(randomCategoryType())
                .accountCreditedType(randomCategoryType())
                .accountDebitedCategoryUuid(UUID.randomUUID())
                .accountCreditedCategoryUuid(UUID.randomUUID())
                .contactUuid(UUID.randomUUID())
                .invoiceNumber(BaseTestUtils.randomString())
                .uuid(UUID.randomUUID())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .parentUuid(UUID.randomUUID())
                .detailed(BaseTestUtils.randomBoolean())
                .build();

        var t2 = t1.check(!t1.checked());

        assertEquals(t2.amount(), t1.amount());
        assertEquals(t2.creditAmount(), t1.creditAmount());
        assertEquals(t2.transactionDate(), t1.transactionDate());
        assertEquals(t2.type(), t1.type());
        assertEquals(t2.comment(), t1.comment());
        assertEquals(t2.checked(), !t1.checked());
        assertEquals(t2.accountDebitedUuid(), t1.accountDebitedUuid());
        assertEquals(t2.accountCreditedUuid(), t1.accountCreditedUuid());
        assertEquals(t2.accountDebitedType(), t1.accountDebitedType());
        assertEquals(t2.accountCreditedType(), t1.accountCreditedType());
        assertEquals(t2.contactUuid(), t1.contactUuid());
        assertEquals(t2.invoiceNumber(), t1.invoiceNumber());
        assertEquals(t2.parentUuid(), t1.parentUuid());
        assertEquals(t2.uuid(), t1.uuid());
        assertEquals(t1.created(), t2.created());
        assertTrue(t2.modified() >= t1.modified());
    }

    @Test
    public void testCopy() {
        var original = BaseTestUtils.newTransaction();

        // Builder copy
        var builderCopy = new Transaction.Builder(original).build();
        assertEquals(original, builderCopy);
    }
}
