/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CategoryRepository;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_EURO;
import static org.panteleyev.money.backend.BaseTestUtils.insertAndUpdate;
import static org.panteleyev.money.backend.BaseTestUtils.newIcon;
import static org.panteleyev.money.backend.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.backend.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.backend.BaseTestUtils.randomCardType;
import static org.panteleyev.money.backend.BaseTestUtils.randomCategoryType;
import static org.panteleyev.money.backend.BaseTestUtils.randomContactType;
import static org.panteleyev.money.backend.BaseTestUtils.randomDay;
import static org.panteleyev.money.backend.BaseTestUtils.randomInt;
import static org.panteleyev.money.backend.BaseTestUtils.randomMonth;
import static org.panteleyev.money.backend.BaseTestUtils.randomString;
import static org.panteleyev.money.backend.BaseTestUtils.randomTransactionType;
import static org.panteleyev.money.backend.BaseTestUtils.randomYear;
import static org.panteleyev.money.backend.Profiles.TEST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RepositoriesTest {
    private static final UUID ICON_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID TRANSACTION_UUID = UUID.randomUUID();
    @Autowired
    private IconRepository iconRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @Order(1)
    public void testIcon() {
        var created = System.currentTimeMillis();
        var insert = newIcon(ICON_UUID, ICON_DOLLAR, created, created);
        var update = newIcon(ICON_UUID, ICON_EURO, created, System.currentTimeMillis());
        insertAndUpdate(iconRepository, insert, update);
    }

    @Test
    @Order(2)
    public void testCategory() {
        var created = System.currentTimeMillis();

        var insert = new Category(
                CATEGORY_UUID,
                randomString(),
                randomString(),
                randomCategoryType(),
                ICON_UUID,
                created,
                created
        );

        var update = new Category(
                CATEGORY_UUID,
                randomString(),
                randomString(),
                randomCategoryType(),
                null,
                created,
                System.currentTimeMillis()
        );

        insertAndUpdate(categoryRepository, insert, update);
    }

    @Test
    @Order(3)
    public void testCurrency() {
        var created = System.currentTimeMillis();
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
                created,
                created
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
                created,
                System.currentTimeMillis()
        );

        insertAndUpdate(currencyRepository, insert, update);
    }

    @Test
    @Order(4)
    public void testContact() {
        var created = System.currentTimeMillis();

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
                created,
                created
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
                created,
                System.currentTimeMillis()
        );

        insertAndUpdate(contactRepository, insert, update);
    }

    @Test
    @Order(5)
    public void testAccount() {
        var created = System.currentTimeMillis();

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
                randomBoolean(),
                randomBigDecimal(),
                LocalDate.now(),
                ICON_UUID,
                randomCardType(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                created,
                created
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
                randomBoolean(),
                randomBigDecimal(),
                LocalDate.now(),
                null,
                randomCardType(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                created,
                System.currentTimeMillis()
        );

        insertAndUpdate(accountRepository, insert, update);
    }

    @Test
    @Order(6)
    public void testTransaction() {
        var created = System.currentTimeMillis();

        var insert = new Transaction(
                TRANSACTION_UUID,
                randomBigDecimal(),
                randomDay(),
                randomMonth(),
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
                created,
                created
        );

        var update = new Transaction(
                TRANSACTION_UUID,
                randomBigDecimal(),
                randomDay(),
                randomMonth(),
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
                created,
                System.currentTimeMillis()
        );

        insertAndUpdate(transactionRepository, insert, update);
    }
}
