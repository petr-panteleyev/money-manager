/*
 Copyright © 2018-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.RecurrenceType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.test.BaseTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.test.BaseTestUtils.randomBoolean;
import static org.panteleyev.money.test.BaseTestUtils.randomCardType;
import static org.panteleyev.money.test.BaseTestUtils.randomDay;
import static org.panteleyev.money.test.BaseTestUtils.randomString;

public class TestMoneyDAO extends BaseDaoTest {
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
    public void testIcon() {
        var repo = new IconRepository();

        var uuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(uuid, ICON_DOLLAR);

        dao().insertIcon(icon);
        assertEquals(icon, cache().getIcon(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(icon, retrieved.orElseThrow());

        var update = BaseTestUtils.newIcon(uuid, ICON_EURO);
        dao().updateIcon(update);
        assertEquals(update, cache().getIcon(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testCategory() {
        var repo = new CategoryRepository();

        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        dao().insertIcon(icon);

        var uuid = UUID.randomUUID();
        var category = BaseTestUtils.newCategory(uuid, iconUuid);

        dao().insertCategory(category);
        assertEquals(category, cache().getCategory(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(category, retrieved.orElseThrow());

        var update = BaseTestUtils.newCategory(uuid);
        dao().updateCategory(update);
        assertEquals(update, cache().getCategory(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testCurrency() {
        var repo = new CurrencyRepository();

        var uuid = UUID.randomUUID();
        var currency = BaseTestUtils.newCurrency(uuid);

        dao().insertCurrency(currency);
        assertEquals(currency, cache().getCurrency(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(currency, retrieved.orElseThrow());

        var update = BaseTestUtils.newCurrency(uuid);
        dao().updateCurrency(update);
        assertEquals(update, cache().getCurrency(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testContact() {
        var repo = new ContactRepository();

        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        dao().insertIcon(icon);

        var uuid = UUID.randomUUID();
        var contact = BaseTestUtils.newContact(uuid, iconUuid);

        dao().insertContact(contact);
        assertEquals(contact, cache().getContact(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(contact, retrieved.orElseThrow());

        var update = BaseTestUtils.newContact(uuid);
        dao().updateContact(update);
        assertEquals(update, cache().getContact(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testAccount() {
        var repo = new AccountRepository();

        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        dao().insertIcon(icon);

        var category = new Category.Builder()
                .name(randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao().insertCategory(category);

        var accountId = UUID.randomUUID();

        var account = new Account.Builder()
                .name(randomString())
                .uuid(accountId)
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .iconUuid(iconUuid)
                .build();
        dao().insertAccount(account);

        assertEquals(account, cache().getAccount(accountId).orElseThrow());
        var retrieved = get(repo, accountId);
        assertEquals(account, retrieved.orElseThrow());

        var update = new Account.Builder(account)
                .accountNumber(randomString())
                .build();

        dao().updateAccount(update);
        assertEquals(update, cache().getAccount(accountId).orElseThrow());
        retrieved = get(repo, accountId);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testTransaction() {
        var repo = new TransactionRepository();

        var category = new Category.Builder()
                .name(randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao().insertCategory(category);

        var account = new Account.Builder()
                .uuid(UUID.randomUUID())
                .name(randomString())
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .build();
        dao().insertAccount(account);

        var id = UUID.randomUUID();

        var transaction = new Transaction.Builder()
                .uuid(id)
                .transactionDate(LocalDate.now())
                .amount(BaseTestUtils.randomBigDecimal())
                .accountDebitedUuid(account.uuid())
                .accountCreditedUuid(account.uuid())
                .accountDebitedCategoryUuid(category.uuid())
                .accountCreditedCategoryUuid(category.uuid())
                .accountDebitedType(account.type())
                .accountCreditedType(account.type())
                .build();


        dao().insertTransaction(transaction);
        assertEquals(transaction, cache().getTransaction(id).orElseThrow());
        var retrieved = get(repo, id);
        assertEquals(transaction, retrieved.orElseThrow());

        var update = new Transaction.Builder(transaction)
                .comment(randomString())
                .build();

        dao().updateTransaction(update);
        assertEquals(update, cache().getTransaction(id).orElseThrow());
        retrieved = get(repo, id);
        assertEquals(update, retrieved.orElseThrow());
    }
    
    @Test
    public void testPeriodicPayment() {
        var repo = new PeriodicPaymentRepository();

        var category = new Category.Builder()
                .name(randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao().insertCategory(category);

        var account = new Account.Builder()
                .uuid(UUID.randomUUID())
                .name(randomString())
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .build();
        dao().insertAccount(account);

        var contact = new Contact.Builder()
                .name(randomString())
                .build();
        dao().insertContact(contact);

        var periodic = new PeriodicPayment.Builder()
                .name(randomString())
                .dayOfMonth(randomDay())
                .paymentType(PeriodicPaymentType.AUTO_PAYMENT)
                .recurrenceType(RecurrenceType.MONTHLY)
                .accountDebitedUuid(account.uuid())
                .accountCreditedUuid(account.uuid())
                .contactUuid(contact.uuid())
                .comment(randomString())
                .build();
        dao().insertPeriodicPayment(periodic);
        assertEquals(periodic, cache().getPeriodicPayment(periodic.uuid()).orElseThrow());
        var retrieved = get(repo, periodic.uuid());
        assertEquals(periodic, retrieved.orElseThrow());

        var update = new PeriodicPayment.Builder(periodic)
                .comment(randomString())
                .build();

        dao().updatePeriodicPayment(update);
        assertEquals(update, cache().getPeriodicPayment(periodic.uuid()).orElseThrow());
        retrieved = get(repo, periodic.uuid());
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testCard() {
        var repo = new CardRepository();

        var category = new Category.Builder()
                .name(randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao().insertCategory(category);

        var account = new Account.Builder()
                .uuid(UUID.randomUUID())
                .name(randomString())
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .build();
        dao().insertAccount(account);

        var card = new Card.Builder()
                .uuid(UUID.randomUUID())
                .accountUuid(account.uuid())
                .type(randomCardType())
                .number(randomString())
                .expiration(LocalDate.now())
                .comment(randomString())
                .enabled(randomBoolean())
                .build();

        dao().insertCard(card);
        assertEquals(card, cache().getCard(card.uuid()).orElseThrow());
        var retrieved = get(repo, card.uuid());
        assertEquals(card, retrieved.orElseThrow());

        var update = new Card.Builder(card)
                .comment(randomString())
                .build();

        dao().updateCard(update);
        assertEquals(update, cache().getCard(card.uuid()).orElseThrow());
        retrieved = get(repo, card.uuid());
        assertEquals(update, retrieved.orElseThrow());
    }
}
