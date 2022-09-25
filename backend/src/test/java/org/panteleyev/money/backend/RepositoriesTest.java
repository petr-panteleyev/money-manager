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
import org.panteleyev.money.backend.repository.DocumentRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.backend.repository.MoneyRepository;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.panteleyev.money.model.MoneyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_EURO;
import static org.panteleyev.money.backend.BaseTestUtils.newAccount;
import static org.panteleyev.money.backend.BaseTestUtils.newCategory;
import static org.panteleyev.money.backend.BaseTestUtils.newContact;
import static org.panteleyev.money.backend.BaseTestUtils.newCurrency;
import static org.panteleyev.money.backend.BaseTestUtils.newDocument;
import static org.panteleyev.money.backend.BaseTestUtils.newIcon;
import static org.panteleyev.money.backend.BaseTestUtils.newTransaction;
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
    private static final UUID DOCUMENT_UUID = UUID.randomUUID();
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
    @Autowired
    private DocumentRepository documentRepository;

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
        var insert = newCategory(CATEGORY_UUID, ICON_UUID, created, created);
        var update = newCategory(CATEGORY_UUID, null, created, System.currentTimeMillis());
        insertAndUpdate(categoryRepository, insert, update);
    }

    @Test
    @Order(3)
    public void testCurrency() {
        var created = System.currentTimeMillis();
        var insert = newCurrency(CURRENCY_UUID, created, created);
        var update = newCurrency(CURRENCY_UUID, created, System.currentTimeMillis());
        insertAndUpdate(currencyRepository, insert, update);
    }

    @Test
    @Order(4)
    public void testContact() {
        var created = System.currentTimeMillis();
        var insert = newContact(CONTACT_UUID, ICON_UUID, created, created);
        var update = newContact(CONTACT_UUID, null, created, System.currentTimeMillis());
        insertAndUpdate(contactRepository, insert, update);
    }

    @Test
    @Order(5)
    public void testAccount() {
        var created = System.currentTimeMillis();
        var insert = newAccount(ACCOUNT_UUID, CATEGORY_UUID, CURRENCY_UUID, ICON_UUID, created, created);
        var update = newAccount(ACCOUNT_UUID, CATEGORY_UUID, CURRENCY_UUID, null,
                created, System.currentTimeMillis());
        insertAndUpdate(accountRepository, insert, update);
    }

    @Test
    @Order(6)
    public void testTransaction() {
        var created = System.currentTimeMillis();
        var insert = newTransaction(TRANSACTION_UUID, ACCOUNT_UUID, CATEGORY_UUID, CONTACT_UUID, created, created);
        var update = newTransaction(TRANSACTION_UUID, ACCOUNT_UUID, CATEGORY_UUID, null,
                created, System.currentTimeMillis());
        insertAndUpdate(transactionRepository, insert, update);
    }

    @Test
    @Order(7)
    public void testDocument() {
        var created = System.currentTimeMillis();
        var insert = newDocument(DOCUMENT_UUID, ACCOUNT_UUID, CONTACT_UUID, created, created);
        var update = newDocument(DOCUMENT_UUID, null, CONTACT_UUID,
                created, System.currentTimeMillis());
        insertAndUpdate(documentRepository, insert, update);
    }

    private static <T extends MoneyRecord> void insertAndUpdate(MoneyRepository<T> repository, T insert, T update) {
        var uuid = insert.uuid();

        var insertResult = repository.insertOrUpdate(insert);
        assertEquals(1, insertResult);
        assertEquals(repository.get(uuid).orElseThrow(), insert);

        var updateResult = repository.insertOrUpdate(update);
        assertEquals(1, updateResult);
        assertEquals(repository.get(uuid).orElseThrow(), update);
    }
}
