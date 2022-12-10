/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.UUID;

final class BaseTestUtils {
    static final Random RANDOM = new Random(System.currentTimeMillis());

    private BaseTestUtils() {
    }

    static int randomDay() {
        return 1 + RANDOM.nextInt(28);
    }

    static int randomMonth() {
        return 1 + RANDOM.nextInt(12);
    }

    static int randomYear() {
        return 1 + RANDOM.nextInt(3000);
    }

    static String randomString() {
        return UUID.randomUUID().toString();
    }

    static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    static int randomInt() {
        return RANDOM.nextInt();
    }

    static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(RANDOM.nextDouble()).setScale(6, RoundingMode.HALF_UP);
    }

    static CategoryType randomCategoryType() {
        int index = RANDOM.nextInt(CategoryType.values().length);
        return CategoryType.values()[index];
    }

    static CardType randomCardType() {
        int index = RANDOM.nextInt(CardType.values().length);
        return CardType.values()[index];
    }

    static ContactType randomContactType() {
        int index = RANDOM.nextInt(ContactType.values().length);
        return ContactType.values()[index];
    }

    static TransactionType randomTransactionType() {
        while (true) {
            int index = RANDOM.nextInt(TransactionType.values().length);
            var type = TransactionType.values()[index];
            if (!type.isSeparator()) {
                return type;
            }
        }
    }

    static DocumentType randomDocumentType() {
        int index = RANDOM.nextInt(DocumentType.values().length);
        return DocumentType.values()[index];
    }

    static Currency newCurrency() {
        return newCurrency(UUID.randomUUID());
    }

    static Currency newCurrency(UUID uuid) {
        return new Currency.Builder()
                .symbol(UUID.randomUUID().toString())
                .description(UUID.randomUUID().toString())
                .formatSymbol(UUID.randomUUID().toString())
                .formatSymbolPosition(RANDOM.nextInt(2))
                .showFormatSymbol(RANDOM.nextBoolean())
                .def(RANDOM.nextBoolean())
                .rate(randomBigDecimal())
                .direction(RANDOM.nextInt(2))
                .useThousandSeparator(RANDOM.nextBoolean())
                .uuid(uuid)
                .modified(System.currentTimeMillis())
                .build();
    }

    static Transaction newTransaction() {
        return newTransaction(UUID.randomUUID());
    }

    static Transaction newTransaction(UUID uuid) {
        return new Transaction.Builder()
                .uuid(uuid)
                .amount(randomBigDecimal())
                .day(randomDay())
                .month(randomMonth())
                .year(randomYear())
                .type(randomTransactionType())
                .comment(UUID.randomUUID().toString())
                .checked(RANDOM.nextBoolean())
                .accountDebitedUuid(UUID.randomUUID())
                .accountCreditedUuid(UUID.randomUUID())
                .accountDebitedType(randomCategoryType())
                .accountCreditedType(randomCategoryType())
                .accountDebitedCategoryUuid(UUID.randomUUID())
                .accountCreditedCategoryUuid(UUID.randomUUID())
                .contactUuid(UUID.randomUUID())
                .rate(randomBigDecimal())
                .rateDirection(RANDOM.nextInt(2))
                .invoiceNumber(UUID.randomUUID().toString())
                .modified(System.currentTimeMillis())
                .build();
    }
}
