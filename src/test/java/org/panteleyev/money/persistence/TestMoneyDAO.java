/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Icon;
import org.panteleyev.money.persistence.model.Transaction;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.newCategory;
import static org.panteleyev.money.BaseTestUtils.newContact;
import static org.panteleyev.money.BaseTestUtils.newCurrency;
import static org.panteleyev.money.BaseTestUtils.newIcon;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.persistence.model.CategoryType.BANKS_AND_CASH;
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
        var icon = newIcon(uuid, ICON_DOLLAR);

        getDao().insertIcon(icon);
        assertEquals(getDao().getIcon(uuid).orElseThrow(), icon);
        var retrieved = getDao().get(uuid, Icon.class);
        assertEquals(retrieved.orElseThrow(), icon);

        var update = newIcon(uuid, ICON_EURO);
        getDao().updateIcon(update);
        assertEquals(getDao().getIcon(uuid).orElseThrow(), update);
        retrieved = getDao().get(uuid, Icon.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testCategory() {
        var iconUuid = UUID.randomUUID();
        var icon = newIcon(iconUuid, ICON_DOLLAR);
        getDao().insertIcon(icon);

        var uuid = UUID.randomUUID();
        var category = newCategory(uuid, iconUuid);

        getDao().insertCategory(category);
        assertEquals(getDao().getCategory(uuid).orElseThrow(), category);
        var retrieved = getDao().get(uuid, Category.class);
        assertEquals(retrieved.orElseThrow(), category);

        var update = newCategory(uuid);
        getDao().updateCategory(update);
        assertEquals(getDao().getCategory(uuid).orElseThrow(), update);
        retrieved = getDao().get(uuid, Category.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testCurrency() {
        var uuid = UUID.randomUUID();
        var category = newCurrency(uuid);

        getDao().insertCurrency(category);
        assertEquals(getDao().getCurrency(uuid).orElseThrow(), category);
        var retrieved = getDao().get(uuid, Currency.class);
        assertEquals(retrieved.orElseThrow(), category);

        var update = newCurrency(uuid);
        getDao().updateCurrency(update);
        assertEquals(getDao().getCurrency(uuid).orElseThrow(), update);
        retrieved = getDao().get(uuid, Currency.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testContact() {
        var iconUuid = UUID.randomUUID();
        var icon = newIcon(iconUuid, ICON_DOLLAR);
        getDao().insertIcon(icon);

        var uuid = UUID.randomUUID();
        var contact = newContact(uuid, iconUuid);

        getDao().insertContact(contact);
        assertEquals(getDao().getContact(uuid).orElseThrow(), contact);
        var retrieved = getDao().get(uuid, Contact.class);
        assertEquals(retrieved.orElseThrow(), contact);

        var update = newContact(uuid);
        getDao().updateContact(update);
        assertEquals(getDao().getContact(uuid).orElseThrow(), update);
        retrieved = getDao().get(uuid, Contact.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testAccount() {
        var iconUuid = UUID.randomUUID();
        var icon = newIcon(iconUuid, ICON_DOLLAR);
        getDao().insertIcon(icon);

        var category = new Category.Builder()
            .catTypeId(BANKS_AND_CASH.getId())
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

        assertEquals(getDao().getAccount(accountId).orElseThrow(), account);
        var retrieved = getDao().get(accountId, Account.class);
        assertEquals(retrieved.orElseThrow(), account);

        var update = new Account.Builder(account)
            .accountNumber(UUID.randomUUID().toString())
            .build();

        getDao().updateAccount(update);
        assertEquals(getDao().getAccount(accountId).orElseThrow(), update);
        retrieved = getDao().get(accountId, Account.class);
        assertEquals(retrieved.orElseThrow(), update);
    }

    @Test
    public void testTransaction() {
        var category = new Category.Builder()
            .catTypeId(BANKS_AND_CASH.getId())
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
            .amount(randomBigDecimal())
            .accountDebitedUuid(account.getUuid())
            .accountCreditedUuid(account.getUuid())
            .accountDebitedCategoryUuid(category.getUuid())
            .accountCreditedCategoryUuid(category.getUuid())
            .accountDebitedTypeId(account.getTypeId())
            .accountCreditedTypeId(account.getTypeId())
            .build();


        getDao().insertTransaction(transaction);
        assertEquals(getDao().getTransaction(id).orElseThrow(), transaction);
        var retrieved = getDao().get(id, Transaction.class);
        assertEquals(retrieved.orElseThrow(), transaction);

        var update = new Transaction.Builder(transaction)
            .comment(UUID.randomUUID().toString())
            .build();

        getDao().updateTransaction(update);
        assertEquals(getDao().getTransaction(id).orElseThrow(), update);
        retrieved = getDao().get(id, Transaction.class);
        assertEquals(retrieved.orElseThrow(), update);
    }
}
