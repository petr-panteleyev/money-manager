/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAccount {

    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var name = BaseTestUtils.randomString();
        var comment = BaseTestUtils.randomString();
        var accountNumber = BaseTestUtils.randomString();
        var openingBalance = BaseTestUtils.randomBigDecimal();
        var accountLimit = BaseTestUtils.randomBigDecimal();
        var currencyRate = BaseTestUtils.randomBigDecimal();
        var type = BaseTestUtils.randomCategoryType();
        var categoryUuid = UUID.randomUUID();
        var currencyUuid = UUID.randomUUID();
        var enabled = BaseTestUtils.randomBoolean();
        var interest = BaseTestUtils.randomBigDecimal();
        var closingDate = LocalDate.now();
        var iconUuid = UUID.randomUUID();
        var cardType = BaseTestUtils.randomCardType();
        var cardNumber = BaseTestUtils.randomString();
        var total = BaseTestUtils.randomBigDecimal();
        var totalWaiting = BaseTestUtils.randomBigDecimal();
        var created = System.currentTimeMillis();
        var modified = created + 1000;

        return List.of(
                Arguments.of(
                        new Account.Builder()
                                .uuid(uuid)
                                .name(name)
                                .comment(comment)
                                .accountNumber(accountNumber)
                                .openingBalance(openingBalance)
                                .accountLimit(accountLimit)
                                .currencyRate(currencyRate)
                                .type(type)
                                .categoryUuid(categoryUuid)
                                .currencyUuid(currencyUuid)
                                .enabled(enabled)
                                .interest(interest)
                                .closingDate(closingDate)
                                .iconUuid(iconUuid)
                                .cardType(cardType)
                                .cardNumber(cardNumber)
                                .total(total)
                                .totalWaiting(totalWaiting)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new Account(
                                uuid, name, comment, accountNumber, openingBalance,
                                accountLimit, currencyRate, type, categoryUuid, currencyUuid,
                                enabled, interest, closingDate, iconUuid, cardType,
                                cardNumber, total, totalWaiting, created, modified
                        )
                ),
                Arguments.of(
                        new Account(
                                uuid, name, null, null, null,
                                null, null, type, categoryUuid, null,
                                enabled, null, null, null, cardType,
                                cardNumber, null, null, created, modified
                        ),
                        new Account(
                                uuid, name, "", "", BigDecimal.ZERO,
                                BigDecimal.ZERO, BigDecimal.ONE, type, categoryUuid, null,
                                enabled, BigDecimal.ZERO, null, null, cardType,
                                cardNumber, BigDecimal.ZERO, BigDecimal.ZERO, created, modified
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(Account actual, Account expected) {
        assertEquals(expected, actual);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }

    @Test
    public void testEquals() {
        var name = UUID.randomUUID().toString();
        var comment = UUID.randomUUID().toString();
        var accountNumber = UUID.randomUUID().toString();
        var opening = BaseTestUtils.randomBigDecimal();
        var limit = BaseTestUtils.randomBigDecimal();
        var rate = BaseTestUtils.randomBigDecimal();
        var type = BaseTestUtils.randomCategoryType();
        var categoryUuid = UUID.randomUUID();
        var currencyUuid = UUID.randomUUID();
        var enabled = BaseTestUtils.RANDOM.nextBoolean();
        var interest = BaseTestUtils.randomBigDecimal();
        var closingDate = LocalDate.now();
        var iconUuid = UUID.randomUUID();
        var cardType = CardType.MASTERCARD;
        var cardNumber = UUID.randomUUID().toString();
        var uuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        var a1 = new Account.Builder()
                .name(name)
                .comment(comment)
                .accountNumber(accountNumber)
                .openingBalance(opening)
                .accountLimit(limit)
                .currencyRate(rate)
                .type(type)
                .categoryUuid(categoryUuid)
                .currencyUuid(currencyUuid)
                .enabled(enabled)
                .interest(interest)
                .closingDate(closingDate)
                .iconUuid(iconUuid)
                .cardType(cardType)
                .cardNumber(cardNumber)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();

        var a2 = new Account.Builder()
                .name(name)
                .comment(comment)
                .accountNumber(accountNumber)
                .openingBalance(opening)
                .accountLimit(limit)
                .currencyRate(rate)
                .type(type)
                .categoryUuid(categoryUuid)
                .currencyUuid(currencyUuid)
                .enabled(enabled)
                .interest(interest)
                .closingDate(closingDate)
                .iconUuid(iconUuid)
                .cardType(cardType)
                .cardNumber(cardNumber)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    public static List<Arguments> testAccountNumberDataProvider() {
        return List.of(
                Arguments.of("   1234  5 6   78 ", "12345678"),
                Arguments.of("12345678", "12345678"),
                Arguments.of("123456 78    ", "12345678"),
                Arguments.of(" 12345678", "12345678")
        );
    }

    @ParameterizedTest
    @MethodSource("testAccountNumberDataProvider")
    public void testAccountNumber(String accountNumber, String accountNumberNoSpaces) {
        var a = new Account.Builder()
                .name(BaseTestUtils.randomString())
                .accountNumber(accountNumber)
                .type(CategoryType.DEBTS)
                .categoryUuid(UUID.randomUUID())
                .uuid(UUID.randomUUID())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();

        assertEquals(a.accountNumber(), accountNumber);
        assertEquals(Account.getAccountNumberNoSpaces(a), accountNumberNoSpaces);
    }

    @Test
    public void testCopy() {
        var original = new Account.Builder()
                .name(UUID.randomUUID().toString())
                .comment(UUID.randomUUID().toString())
                .accountNumber(UUID.randomUUID().toString())
                .openingBalance(BaseTestUtils.randomBigDecimal())
                .accountLimit(BaseTestUtils.randomBigDecimal())
                .currencyRate(BaseTestUtils.randomBigDecimal())
                .type(BaseTestUtils.randomCategoryType())
                .categoryUuid(UUID.randomUUID())
                .currencyUuid(UUID.randomUUID())
                .enabled(BaseTestUtils.RANDOM.nextBoolean())
                .interest(BaseTestUtils.randomBigDecimal())
                .closingDate(LocalDate.now())
                .iconUuid(UUID.randomUUID())
                .cardType(CardType.VISA)
                .cardNumber(UUID.randomUUID().toString())
                .uuid(UUID.randomUUID())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();

        var copy = new Account.Builder(original).build();
        assertEquals(copy, original);

        var manualCopy = new Account.Builder()
                .name(original.name())
                .comment(original.comment())
                .accountNumber(original.accountNumber())
                .openingBalance(original.openingBalance())
                .accountLimit(original.accountLimit())
                .currencyRate(original.currencyRate())
                .type(original.type())
                .categoryUuid(original.categoryUuid())
                .currencyUuid(original.currencyUuid())
                .enabled(original.enabled())
                .interest(original.interest())
                .closingDate(original.closingDate())
                .iconUuid(original.iconUuid())
                .cardType(original.cardType())
                .cardNumber(original.cardNumber())
                .uuid(original.uuid())
                .created(original.created())
                .modified(original.modified())
                .build();
        assertEquals(manualCopy, original);
    }
}
