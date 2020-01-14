/*
 * Copyright (c) 2020, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.persistence;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.test.BaseTestUtils;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getClient;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.testng.Assert.assertEquals;

public class TestMoneyDAO extends BaseDaoTest {
    @BeforeClass
    @Override
    public void setupAndSkip() {
        try {
            super.setupAndSkip();
            getDao().createTables();
            getDao().preload();
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        getDao().dropTables();
        super.cleanup();
    }

    @Test
    public void testIcon() {
        var uuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(uuid, ICON_DOLLAR);

        getDao().insertIcon(icon);
        assertEquals(cache().getIcon(uuid).orElseThrow(), icon);
        var retrieved = getClient().get(uuid, Icon.class);
        assertEquals(retrieved.orElseThrow(), icon);

        var update = BaseTestUtils.newIcon(uuid, ICON_EURO);
        getDao().updateIcon(update);
        assertEquals(cache().getIcon(uuid).orElseThrow(), update);
        retrieved = getClient().get(uuid, Icon.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testCategory() {
        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        getDao().insertIcon(icon);

        var uuid = UUID.randomUUID();
        var category = BaseTestUtils.newCategory(uuid, iconUuid);

        getDao().insertCategory(category);
        assertEquals(cache().getCategory(uuid).orElseThrow(), category);
        var retrieved = getClient().get(uuid, Category.class);
        assertEquals(retrieved.orElseThrow(), category);

        var update = BaseTestUtils.newCategory(uuid);
        getDao().updateCategory(update);
        assertEquals(cache().getCategory(uuid).orElseThrow(), update);
        retrieved = getClient().get(uuid, Category.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testCurrency() {
        var uuid = UUID.randomUUID();
        var category = BaseTestUtils.newCurrency(uuid);

        getDao().insertCurrency(category);
        assertEquals(cache().getCurrency(uuid).orElseThrow(), category);
        var retrieved = getClient().get(uuid, Currency.class);
        assertEquals(retrieved.orElseThrow(), category);

        var update = BaseTestUtils.newCurrency(uuid);
        getDao().updateCurrency(update);
        assertEquals(cache().getCurrency(uuid).orElseThrow(), update);
        retrieved = getClient().get(uuid, Currency.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testContact() {
        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        getDao().insertIcon(icon);

        var uuid = UUID.randomUUID();
        var contact = BaseTestUtils.newContact(uuid, iconUuid);

        getDao().insertContact(contact);
        assertEquals(cache().getContact(uuid).orElseThrow(), contact);
        var retrieved = getClient().get(uuid, Contact.class);
        assertEquals(retrieved.orElseThrow(), contact);

        var update = BaseTestUtils.newContact(uuid);
        getDao().updateContact(update);
        assertEquals(cache().getContact(uuid).orElseThrow(), update);
        retrieved = getClient().get(uuid, Contact.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testAccount() {
        var iconUuid = UUID.randomUUID();
        var icon = BaseTestUtils.newIcon(iconUuid, ICON_DOLLAR);
        getDao().insertIcon(icon);

        var category = new Category.Builder()
            .catTypeId(CategoryType.BANKS_AND_CASH.getId())
            .guid(UUID.randomUUID())
            .build();
        getDao().insertCategory(category);

        var accountId = UUID.randomUUID();

        var account = new Account.Builder()
            .guid(accountId)
            .typeId(category.getCatTypeId())
            .categoryUuid(category.getUuid())
            .accountNumber("123456")
            .iconUuid(iconUuid)
            .build();
        getDao().insertAccount(account);

        assertEquals(cache().getAccount(accountId).orElseThrow(), account);
        var retrieved = getClient().get(accountId, Account.class);
        assertEquals(retrieved.orElseThrow(), account);

        var update = new Account.Builder(account)
            .accountNumber(UUID.randomUUID().toString())
            .build();

        getDao().updateAccount(update);
        assertEquals(cache().getAccount(accountId).orElseThrow(), update);
        retrieved = getClient().get(accountId, Account.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testTransaction() {
        var category = new Category.Builder()
            .catTypeId(CategoryType.BANKS_AND_CASH.getId())
            .guid(UUID.randomUUID())
            .build();
        getDao().insertCategory(category);

        var account = new Account.Builder()
            .typeId(category.getCatTypeId())
            .categoryUuid(category.getUuid())
            .accountNumber("123456")
            .build();
        getDao().insertAccount(account);

        var id = UUID.randomUUID();

        var now = LocalDate.now();
        var transaction = new Transaction.Builder()
            .guid(id)
            .day(now.getDayOfMonth())
            .month(now.getMonthValue())
            .year(now.getYear())
            .amount(BaseTestUtils.randomBigDecimal())
            .accountDebitedUuid(account.getUuid())
            .accountCreditedUuid(account.getUuid())
            .accountDebitedCategoryUuid(category.getUuid())
            .accountCreditedCategoryUuid(category.getUuid())
            .accountDebitedTypeId(account.getTypeId())
            .accountCreditedTypeId(account.getTypeId())
            .build();


        getDao().insertTransaction(transaction);
        assertEquals(cache().getTransaction(id).orElseThrow(), transaction);
        var retrieved = getClient().get(id, Transaction.class);
        assertEquals(retrieved.orElseThrow(), transaction);

        var update = new Transaction.Builder(transaction)
            .comment(UUID.randomUUID().toString())
            .build();

        getDao().updateTransaction(update);
        assertEquals(cache().getTransaction(id).orElseThrow(), update);
        retrieved = getClient().get(id, Transaction.class);
        assertEquals(retrieved.orElseThrow(), update);
    }
}
