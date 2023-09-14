/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

final class BaseTestUtils {
    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";

    public static final Random RANDOM = new Random(System.currentTimeMillis());

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

    static Account newAccount(
            UUID uuid,
            UUID categoryUuid,
            UUID currencyUuid,
            UUID iconUuid,
            long created,
            long modifed
    ) {
        return new Account.Builder()
                .uuid(uuid)
                .name(UUID.randomUUID().toString())
                .comment(UUID.randomUUID().toString())
                .accountNumber(UUID.randomUUID().toString())
                .openingBalance(randomBigDecimal())
                .accountLimit(randomBigDecimal())
                .currencyRate(randomBigDecimal())
                .type(randomCategoryType())
                .categoryUuid(categoryUuid)
                .currencyUuid(currencyUuid)
                .iconUuid(iconUuid)
                .enabled(RANDOM.nextBoolean())
                .interest(randomBigDecimal())
                .closingDate(LocalDate.now())
                .cardType(randomCardType())
                .cardNumber(randomString())
                .created(created)
                .modified(modifed)
                .build();
    }

    static Category newCategory(UUID uuid, UUID iconUuid, long created, long modified) {
        return new Category.Builder()
                .name(UUID.randomUUID().toString())
                .comment(UUID.randomUUID().toString())
                .type(randomCategoryType())
                .iconUuid(iconUuid)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();
    }

    static Currency newCurrency(UUID uuid, long created, long modified) {
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
                .created(created)
                .modified(modified)
                .build();
    }

    static Contact newContact(UUID uuid, UUID iconUuid, long created, long modified) {
        return new Contact.Builder()
                .name(UUID.randomUUID().toString())
                .type(randomContactType())
                .phone(UUID.randomUUID().toString())
                .mobile(UUID.randomUUID().toString())
                .email(UUID.randomUUID().toString())
                .web(UUID.randomUUID().toString())
                .comment(UUID.randomUUID().toString())
                .street(UUID.randomUUID().toString())
                .city(UUID.randomUUID().toString())
                .country(UUID.randomUUID().toString())
                .zip(UUID.randomUUID().toString())
                .iconUuid(iconUuid)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();
    }

    static Transaction newTransaction(
            UUID uuid,
            UUID accountUuid,
            UUID categoryUuid,
            UUID contactUuid,
            long created,
            long modified
    ) {
        return new Transaction.Builder()
                .uuid(uuid)
                .amount(randomBigDecimal())
                .creditAmount(randomBigDecimal())
                .day(randomDay())
                .month(randomMonth())
                .year(randomYear())
                .type(randomTransactionType())
                .comment(UUID.randomUUID().toString())
                .checked(RANDOM.nextBoolean())
                .accountDebitedUuid(accountUuid)
                .accountCreditedUuid(accountUuid)
                .accountDebitedType(randomCategoryType())
                .accountCreditedType(randomCategoryType())
                .accountDebitedCategoryUuid(categoryUuid)
                .accountCreditedCategoryUuid(categoryUuid)
                .contactUuid(contactUuid)
                .invoiceNumber(UUID.randomUUID().toString())
                .created(created)
                .modified(modified)
                .build();
    }

    static MoneyDocument newDocument(
            UUID uuid,
            UUID ownerUuid,
            UUID contactUuid,
            long created,
            long modified
    ) {
        return new MoneyDocument.Builder()
                .uuid(uuid)
                .ownerUuid(ownerUuid)
                .contactUuid(contactUuid)
                .documentType(randomDocumentType())
                .fileName(randomString())
                .date(LocalDate.now())
                .size(RANDOM.nextInt())
                .mimeType(randomString())
                .description(randomString())
                .created(created)
                .modified(modified)
                .build();
    }

    static Icon newIcon(UUID uuid, String name, long created, long updated) {
        try (var inputStream = BaseTestUtils.class.getResourceAsStream("/images/" + name)) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot retrieve test resource");
            }
            var bytes = inputStream.readAllBytes();
            return new Icon(uuid, name, bytes, created, updated);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
