/*
 Copyright © 2021-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.RecurrenceType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.test.BaseTestUtils.newIcon;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.test.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.test.BaseTestUtils.randomCardType;
import static org.panteleyev.money.test.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.test.BaseTestUtils.randomContactType;
import static org.panteleyev.money.test.BaseTestUtils.randomDay;
import static org.panteleyev.money.test.BaseTestUtils.randomInt;
import static org.panteleyev.money.test.BaseTestUtils.randomMonth;
import static org.panteleyev.money.test.BaseTestUtils.randomMonthNumber;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.panteleyev.money.test.BaseTestUtils.randomTransactionType;
import static org.panteleyev.money.test.BaseTestUtils.randomYear;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRepositories extends BaseDaoTest {
    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";

    private static final UUID ICON_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID EXCHANGE_SECURITY_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID TRANSACTION_UUID = UUID.randomUUID();
    private static final UUID DOCUMENT_UUID = UUID.randomUUID();
    private static final UUID PERIODIC_PAYMENT_UUID = UUID.randomUUID();

    @BeforeAll
    public static void init() {
        var initialized = BaseDaoTest.setupAndSkip();
        assumeTrue(initialized);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        BaseDaoTest.tearDown();
    }

    @Test
    @Order(1)
    public void testIcon() {
        var repository = new IconRepository();
        var insert = newIcon(ICON_UUID, ICON_DOLLAR);
        var update = newIcon(ICON_UUID, ICON_EURO);
        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(2)
    public void testCategory() {
        var repository = new CategoryRepository();

        var insert = new Category(
                CATEGORY_UUID,
                randomString(),
                randomString(),
                randomCategoryType(),
                ICON_UUID,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Category(
                CATEGORY_UUID,
                randomString(),
                randomString(),
                randomCategoryType(),
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(3)
    public void testCurrency() {
        var repository = new CurrencyRepository();

        var insert = new Currency(
                CURRENCY_UUID,
                randomString(),
                randomString(),
                randomString(),
                randomInt(),
                randomBoolean(),
                randomBoolean(),
                randomBigDecimal(),
                randomInt(),
                randomBoolean(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Currency(
                CURRENCY_UUID,
                randomString(),
                randomString(),
                randomString(),
                randomInt(),
                randomBoolean(),
                randomBoolean(),
                randomBigDecimal(),
                randomInt(),
                randomBoolean(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(3)
    public void testExchangeSecurity() {
        var repository = new ExchangeSecurityRepository();

        var insert = new ExchangeSecurity(
                EXCHANGE_SECURITY_UUID,
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomBigDecimal(),
                LocalDate.now(),
                LocalDate.now(),
                randomInt(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomBigDecimal(),
                LocalDate.now(),
                randomInt(),
                randomBigDecimal(),
                randomInt(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new ExchangeSecurity(
                EXCHANGE_SECURITY_UUID,
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomBigDecimal(),
                LocalDate.now(),
                null,
                null,
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomBigDecimal(),
                null,
                null,
                null,
                null,
                null,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(5)
    public void testContact() {
        var repository = new ContactRepository();

        var insert = new Contact(
                CONTACT_UUID,
                randomString(),
                randomContactType(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                ICON_UUID,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Contact(
                CONTACT_UUID,
                randomString(),
                randomContactType(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(6)
    public void testAccount() {
        var repository = new AccountRepository();

        var insert = new Account(
                ACCOUNT_UUID,
                randomString(),
                randomString(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomCategoryType(),
                CATEGORY_UUID,
                CURRENCY_UUID,
                EXCHANGE_SECURITY_UUID,
                randomBoolean(),
                randomBigDecimal(),
                LocalDate.now(),
                ICON_UUID,
                randomCardType(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Account(
                ACCOUNT_UUID,
                randomString(),
                randomString(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomBigDecimal(),
                randomCategoryType(),
                CATEGORY_UUID,
                null,
                null,
                randomBoolean(),
                randomBigDecimal(),
                LocalDate.now(),
                null,
                randomCardType(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(7)
    public void testTransaction() {
        var repository = new TransactionRepository();

        var insert = new Transaction(
                TRANSACTION_UUID,
                randomBigDecimal(),
                randomDay(),
                randomMonthNumber(),
                randomYear(),
                randomTransactionType(),
                randomString(),
                randomBoolean(),
                ACCOUNT_UUID,
                ACCOUNT_UUID,
                randomCategoryType(),
                randomCategoryType(),
                CATEGORY_UUID,
                CATEGORY_UUID,
                CONTACT_UUID,
                randomBigDecimal(),
                randomInt(),
                randomString(),
                null,
                randomBoolean(),
                LocalDate.now(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Transaction(
                TRANSACTION_UUID,
                randomBigDecimal(),
                randomDay(),
                randomMonthNumber(),
                randomYear(),
                randomTransactionType(),
                randomString(),
                randomBoolean(),
                ACCOUNT_UUID,
                ACCOUNT_UUID,
                randomCategoryType(),
                randomCategoryType(),
                CATEGORY_UUID,
                CATEGORY_UUID,
                null,
                randomBigDecimal(),
                randomInt(),
                randomString(),
                TRANSACTION_UUID,
                randomBoolean(),
                LocalDate.now(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(8)
    public void testDocument() {
        var repository = new DocumentRepository();

        var insert = new MoneyDocument(
                DOCUMENT_UUID,
                ACCOUNT_UUID,
                CONTACT_UUID,
                DocumentType.CONTRACT,
                randomString(),
                LocalDate.now(),
                randomInt(),
                randomString(),
                randomString(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new MoneyDocument(
                DOCUMENT_UUID,
                ACCOUNT_UUID,
                CONTACT_UUID,
                DocumentType.CONTRACT,
                randomString(),
                LocalDate.now(),
                randomInt(),
                randomString(),
                randomString(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(9)
    public void testDocumentContent() {
        var repository = new DocumentRepository();

        try (var inputStream = getClass().getResourceAsStream("/org/panteleyev/money/icons/" + ICON_DOLLAR)) {
            var bytes = inputStream.readAllBytes();

            dao().withNewConnection(conn -> {
                repository.insertBytes(conn, DOCUMENT_UUID, bytes);

                var actual = repository.getBytes(conn, DOCUMENT_UUID)
                        .orElseThrow();
                assertArrayEquals(bytes, actual);
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Test
    @Order(9)
    public void testPeriodicPayment() {
        var repository = new PeriodicPaymentRepository();

        var insert = new PeriodicPayment(
                PERIODIC_PAYMENT_UUID,
                randomString(),
                PeriodicPaymentType.CARD_PAYMENT,
                RecurrenceType.MONTHLY,
                randomBigDecimal(),
                randomDay(),
                randomMonth(),
                ACCOUNT_UUID,
                ACCOUNT_UUID,
                CONTACT_UUID,
                randomString(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new PeriodicPayment(
                PERIODIC_PAYMENT_UUID,
                randomString(),
                PeriodicPaymentType.MANUAL_PAYMENT,
                RecurrenceType.MONTHLY,
                randomBigDecimal(),
                randomDay(),
                randomMonth(),
                ACCOUNT_UUID,
                ACCOUNT_UUID,
                CONTACT_UUID,
                randomString(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    private static <T extends MoneyRecord> void insertAndUpdate(Repository<T> repository, T insert, T update) {
        dao().withNewConnection(conn -> {
            var uuid = insert.uuid();

            repository.insert(conn, insert);
            assertEquals(insert, repository.get(conn, uuid).orElseThrow());

            repository.update(conn, update);
            assertEquals(update, repository.get(conn, uuid).orElseThrow());
        });
    }
}
