/*
 Copyright © 2018-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.test.BaseTestUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;

public class TestMoneyDAO extends BaseDaoTest {
    @BeforeClass
    @Override
    public void setupAndSkip() {
        try {
            super.setupAndSkip();
        } catch (Exception ex) {
            throw new SkipException(ex.getMessage());
        }
    }

    @AfterClass
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testIcon() {
        var repo = new IconRepository();

        var uuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(uuid, ICON_DOLLAR);

        dao().insertIcon(icon);
        assertEquals(cache().getIcon(uuid).orElseThrow(), icon);
        var retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), icon);

        var update = BaseTestUtils.newIcon(uuid, ICON_EURO);
        dao().updateIcon(update);
        assertEquals(cache().getIcon(uuid).orElseThrow(), update);
        retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), update);
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
        assertEquals(cache().getCategory(uuid).orElseThrow(), category);
        var retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), category);

        var update = BaseTestUtils.newCategory(uuid);
        dao().updateCategory(update);
        assertEquals(cache().getCategory(uuid).orElseThrow(), update);
        retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testCurrency() {
        var repo = new CurrencyRepository();

        var uuid = UUID.randomUUID();
        var category = BaseTestUtils.newCurrency(uuid);

        dao().insertCurrency(category);
        assertEquals(cache().getCurrency(uuid).orElseThrow(), category);
        var retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), category);

        var update = BaseTestUtils.newCurrency(uuid);
        dao().updateCurrency(update);
        assertEquals(cache().getCurrency(uuid).orElseThrow(), update);
        retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), update);
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
        assertEquals(cache().getContact(uuid).orElseThrow(), contact);
        var retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), contact);

        var update = BaseTestUtils.newContact(uuid);
        dao().updateContact(update);
        assertEquals(cache().getContact(uuid).orElseThrow(), update);
        retrieved = get(repo, uuid);
        assertEquals(retrieved.orElseThrow(), update);
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

        assertEquals(cache().getAccount(accountId).orElseThrow(), account);
        var retrieved = get(repo, accountId);
        assertEquals(retrieved.orElseThrow(), account);

        var update = new Account.Builder(account)
                .accountNumber(UUID.randomUUID().toString())
                .build();

        dao().updateAccount(update);
        assertEquals(cache().getAccount(accountId).orElseThrow(), update);
        retrieved = get(repo, accountId);
        assertEquals(retrieved.orElseThrow(), update);
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

        var now = LocalDate.now();
        var transaction = new Transaction.Builder()
                .uuid(id)
                .day(now.getDayOfMonth())
                .month(now.getMonthValue())
                .year(now.getYear())
                .amount(BaseTestUtils.randomBigDecimal())
                .accountDebitedUuid(account.uuid())
                .accountCreditedUuid(account.uuid())
                .accountDebitedCategoryUuid(category.uuid())
                .accountCreditedCategoryUuid(category.uuid())
                .accountDebitedType(account.type())
                .accountCreditedType(account.type())
                .build();


        dao().insertTransaction(transaction);
        assertEquals(cache().getTransaction(id).orElseThrow(), transaction);
        var retrieved = get(repo, id);
        assertEquals(retrieved.orElseThrow(), transaction);

        var update = new Transaction.Builder(transaction)
                .comment(UUID.randomUUID().toString())
                .build();

        dao().updateTransaction(update);
        assertEquals(cache().getTransaction(id).orElseThrow(), update);
        retrieved = get(repo, id);
        assertEquals(retrieved.orElseThrow(), update);
    }
}
