/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.panteleyev.money.client.MoneyClient;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Transaction;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class ClientTest {
    private static final UUID ICON_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID TRANSACTION_UUID = UUID.randomUUID();
    private static final UUID DOCUMENT_UUID = UUID.randomUUID();

    @LocalServerPort
    private int port;

    private final AtomicReference<MoneyClient> client = new AtomicReference<>(null);

    // Client methods
    private final Function<Icon, Icon> putIcon = x -> client.get().putIcon(x);
    private final Function<UUID, Optional<Icon>> getIcon = u -> client.get().getIcon(u);
    private final Supplier<List<Icon>> getIcons = () -> client.get().getIcons();

    private final Function<Category, Category> putCategory = x -> client.get().putCategory(x);
    private final Function<UUID, Optional<Category>> getCategory = u -> client.get().getCategory(u);
    private final Supplier<List<Category>> getCategories = () -> client.get().getCategories();

    private final Function<Contact, Contact> putContact = x -> client.get().putContact(x);
    private final Function<UUID, Optional<Contact>> getContact = u -> client.get().getContact(u);
    private final Supplier<List<Contact>> getContacts = () -> client.get().getContacts();

    private final Function<Currency, Currency> putCurrency = x -> client.get().putCurrency(x);
    private final Function<UUID, Optional<Currency>> getCurrency = u -> client.get().getCurrency(u);
    private final Supplier<List<Currency>> getCurrencies = () -> client.get().getCurrencies();

    private final Function<Account, Account> putAccount = x -> client.get().putAccount(x);
    private final Function<UUID, Optional<Account>> getAccount = u -> client.get().getAccount(u);
    private final Supplier<List<Account>> getAccounts = () -> client.get().getAccounts();

    private final Function<Transaction, Transaction> putTransaction = x -> client.get().putTransaction(x);
    private final Function<UUID, Optional<Transaction>> getTransaction = u -> client.get().getTransaction(u);
    private final Supplier<List<Transaction>> getTransactions = () -> client.get().getTransactions();

    private final Function<MoneyDocument, MoneyDocument> putDocument = x -> client.get().putDocument(x);
    private final Function<UUID, Optional<MoneyDocument>> getDocument = u -> client.get().getDocument(u);
    private final Supplier<List<MoneyDocument>> getDocuments = () -> client.get().getDocuments();

    @BeforeEach
    public void init() {
        client.set(new MoneyClient.Builder()
                .withServerUrl("http://localhost:" + port)
                .build());
    }

    @Test
    @Order(1)
    public void testIcons() {
        var created = System.currentTimeMillis();
        var insert = newIcon(ICON_UUID, ICON_DOLLAR, created, created);
        insertAndCheck(putIcon, getIcon, getIcons, insert);

        var original = getIcon.apply(ICON_UUID).orElseThrow();
        var update = newIcon(ICON_UUID, ICON_EURO, original.created(), System.currentTimeMillis());
        updateAndCheck(putIcon, getIcon, update);
    }

    @Test
    @Order(2)
    public void testCategories() {
        var created = System.currentTimeMillis();
        var insert = newCategory(CATEGORY_UUID, ICON_UUID, created, created);
        insertAndCheck(putCategory, getCategory, getCategories, insert);

        var original = getCategory.apply(CATEGORY_UUID).orElseThrow();
        var update = newCategory(CATEGORY_UUID, null, original.created(), System.currentTimeMillis());
        updateAndCheck(putCategory, getCategory, update);
    }

    @Test
    @Order(3)
    public void testContacts() {
        var created = System.currentTimeMillis();
        var insert = newContact(CONTACT_UUID, ICON_UUID, created, created);
        insertAndCheck(putContact, getContact, getContacts, insert);

        var original = getContact.apply(CONTACT_UUID).orElseThrow();
        var update = newContact(CONTACT_UUID, null, original.created(), System.currentTimeMillis());
        updateAndCheck(putContact, getContact, update);
    }

    @Test
    @Order(4)
    public void testCurrencies() {
        var created = System.currentTimeMillis();
        var insert = newCurrency(CURRENCY_UUID, created, created);
        insertAndCheck(putCurrency, getCurrency, getCurrencies, insert);

        var original = getCurrency.apply(CURRENCY_UUID).orElseThrow();
        var update = newCurrency(CURRENCY_UUID, original.created(), System.currentTimeMillis());
        updateAndCheck(putCurrency, getCurrency, update);
    }

    @Test
    @Order(5)
    public void testAccounts() {
        var created = System.currentTimeMillis();
        var insert = newAccount(ACCOUNT_UUID, CATEGORY_UUID, CURRENCY_UUID, ICON_UUID, created, created);
        insertAndCheck(putAccount, getAccount, getAccounts, insert);

        var original = getAccount.apply(ACCOUNT_UUID).orElseThrow();
        var update = newAccount(ACCOUNT_UUID, CATEGORY_UUID, CURRENCY_UUID, null,
                original.created(), System.currentTimeMillis());
        updateAndCheck(putAccount, getAccount, update);
    }

    @Test
    @Order(6)
    public void testTransactions() {
        var created = System.currentTimeMillis();
        var insert = newTransaction(TRANSACTION_UUID, ACCOUNT_UUID, CATEGORY_UUID, CONTACT_UUID, created, created);
        insertAndCheck(putTransaction, getTransaction, getTransactions, insert);

        var original = getTransaction.apply(TRANSACTION_UUID).orElseThrow();
        var update = newTransaction(TRANSACTION_UUID, ACCOUNT_UUID, CATEGORY_UUID, null,
                original.created(), System.currentTimeMillis());
        updateAndCheck(putTransaction, getTransaction, update);
    }

    @Test
    @Order(7)
    public void testDocuments() {
        var created = System.currentTimeMillis();
        var insert = newDocument(DOCUMENT_UUID, ACCOUNT_UUID, CONTACT_UUID, created, created);
        insertAndCheck(putDocument, getDocument, getDocuments, insert);

        var original = getDocument.apply(DOCUMENT_UUID).orElseThrow();
        var update = newDocument(DOCUMENT_UUID, null, CONTACT_UUID,
                original.created(), System.currentTimeMillis());
        updateAndCheck(putDocument, getDocument, update);
    }

    private static <T extends MoneyRecord> void insertAndCheck(
            Function<T, T> putMethod,
            Function<UUID, Optional<T>> getMethod,
            Supplier<List<T>> getAllMethod,
            T insert
    ) {
        var inserted = putMethod.apply(insert);
        assertEquals(insert, inserted);

        var retrieved = getMethod.apply(insert.uuid()).orElse(null);
        assertEquals(insert, retrieved);

        var list = getAllMethod.get();
        assertTrue(list.contains(retrieved));
    }

    private static <T extends MoneyRecord> void updateAndCheck(
            Function<T, T> putMethod,
            Function<UUID, Optional<T>> getMethod,
            T update
    ) {
        var updated = putMethod.apply(update);
        assertEquals(update, updated);
        var retrieved = getMethod.apply(update.uuid()).orElse(null);
        assertEquals(update, retrieved);
    }
}
