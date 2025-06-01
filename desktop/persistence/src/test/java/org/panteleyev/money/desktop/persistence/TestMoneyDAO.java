/*
 Copyright © 2018-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

        dao.insertIcon(icon);
        Assertions.assertEquals(icon, cache.getIcon(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(icon, retrieved.orElseThrow());

        var update = BaseTestUtils.newIcon(uuid, ICON_EURO);
        dao.updateIcon(update);
        Assertions.assertEquals(update, cache.getIcon(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testCategory() {
        var repo = new CategoryRepository();

        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        dao.insertIcon(icon);

        var uuid = UUID.randomUUID();
        var category = BaseTestUtils.newCategory(uuid, iconUuid);

        dao.insertCategory(category);
        Assertions.assertEquals(category, cache.getCategory(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(category, retrieved.orElseThrow());

        var update = BaseTestUtils.newCategory(uuid);
        dao.updateCategory(update);
        Assertions.assertEquals(update, cache.getCategory(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testCurrency() {
        var repo = new CurrencyRepository();

        var uuid = UUID.randomUUID();
        var currency = BaseTestUtils.newCurrency(uuid);

        dao.insertCurrency(currency);
        Assertions.assertEquals(currency, cache.getCurrency(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(currency, retrieved.orElseThrow());

        var update = BaseTestUtils.newCurrency(uuid);
        dao.updateCurrency(update);
        Assertions.assertEquals(update, cache.getCurrency(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testContact() {
        var repo = new ContactRepository();

        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        dao.insertIcon(icon);

        var uuid = UUID.randomUUID();
        var contact = BaseTestUtils.newContact(uuid, iconUuid);

        dao.insertContact(contact);
        Assertions.assertEquals(contact, cache.getContact(uuid).orElseThrow());
        var retrieved = get(repo, uuid);
        assertEquals(contact, retrieved.orElseThrow());

        var update = BaseTestUtils.newContact(uuid);
        dao.updateContact(update);
        Assertions.assertEquals(update, cache.getContact(uuid).orElseThrow());
        retrieved = get(repo, uuid);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testAccount() {
        var repo = new AccountRepository();

        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        dao.insertIcon(icon);

        var category = new Category.Builder()
                .name(BaseTestUtils.randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao.insertCategory(category);

        var accountId = UUID.randomUUID();

        var account = new Account.Builder()
                .name(BaseTestUtils.randomString())
                .uuid(accountId)
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .iconUuid(iconUuid)
                .build();
        dao.insertAccount(account);

        Assertions.assertEquals(account, cache.getAccount(accountId).orElseThrow());
        var retrieved = get(repo, accountId);
        assertEquals(account, retrieved.orElseThrow());

        var update = new Account.Builder(account)
                .accountNumber(BaseTestUtils.randomString())
                .build();

        dao.updateAccount(update);
        Assertions.assertEquals(update, cache.getAccount(accountId).orElseThrow());
        retrieved = get(repo, accountId);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testTransaction() {
        var repo = new TransactionRepository();

        var category = new Category.Builder()
                .name(BaseTestUtils.randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao.insertCategory(category);

        var account = new Account.Builder()
                .uuid(UUID.randomUUID())
                .name(BaseTestUtils.randomString())
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .build();
        dao.insertAccount(account);

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


        dao.insertTransaction(transaction);
        Assertions.assertEquals(transaction, cache.getTransaction(id).orElseThrow());
        var retrieved = get(repo, id);
        assertEquals(transaction, retrieved.orElseThrow());

        var update = new Transaction.Builder(transaction)
                .comment(BaseTestUtils.randomString())
                .build();

        dao.updateTransaction(update);
        Assertions.assertEquals(update, cache.getTransaction(id).orElseThrow());
        retrieved = get(repo, id);
        assertEquals(update, retrieved.orElseThrow());
    }

    @Test
    public void testCard() {
        var repo = new CardRepository();

        var category = new Category.Builder()
                .name(BaseTestUtils.randomString())
                .type(CategoryType.BANKS_AND_CASH)
                .uuid(UUID.randomUUID())
                .build();
        dao.insertCategory(category);

        var account = new Account.Builder()
                .uuid(UUID.randomUUID())
                .name(BaseTestUtils.randomString())
                .type(category.type())
                .categoryUuid(category.uuid())
                .accountNumber("123456")
                .build();
        dao.insertAccount(account);

        var card = new Card.Builder()
                .uuid(UUID.randomUUID())
                .accountUuid(account.uuid())
                .type(BaseTestUtils.randomCardType())
                .number(BaseTestUtils.randomString())
                .expiration(LocalDate.now())
                .comment(BaseTestUtils.randomString())
                .enabled(BaseTestUtils.randomBoolean())
                .build();

        dao.insertCard(card);
        Assertions.assertEquals(card, cache.getCard(card.uuid()).orElseThrow());
        var retrieved = get(repo, card.uuid());
        assertEquals(card, retrieved.orElseThrow());

        var update = new Card.Builder(card)
                .comment(BaseTestUtils.randomString())
                .build();

        dao.updateCard(update);
        Assertions.assertEquals(update, cache.getCard(card.uuid()).orElseThrow());
        retrieved = get(repo, card.uuid());
        assertEquals(update, retrieved.orElseThrow());
    }
}
