/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
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
import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTEXT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.DOCUMENT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.TRANSACTION_ROOT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class ControllerTest {
    private static final UUID ICON_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID TRANSACTION_UUID = UUID.randomUUID();
    private static final UUID DOCUMENT_UUID = UUID.randomUUID();

    @LocalServerPort
    private int port;

    @BeforeEach
    public void init() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Order(1)
    public void testIcons() {
        var created = System.currentTimeMillis();
        var insert = newIcon(ICON_UUID, ICON_DOLLAR, created, created);
        insertAndCheck(insert, Icon.class, Icon[].class, ICON_ROOT);

        var original = get(ICON_UUID, Icon.class, ICON_ROOT);
        var update = newIcon(ICON_UUID, ICON_EURO, original.created(), System.currentTimeMillis());
        updateAndCheck(update, Icon.class, ICON_ROOT);
    }

    @Test
    @Order(2)
    public void testCategories() {
        var created = System.currentTimeMillis();
        var insert = newCategory(CATEGORY_UUID, ICON_UUID, created, created);
        insertAndCheck(insert, Category.class, Category[].class, CATEGORY_ROOT);

        var original = get(CATEGORY_UUID, Category.class, CATEGORY_ROOT);
        var update = newCategory(CATEGORY_UUID, null, original.created(), System.currentTimeMillis());
        updateAndCheck(update, Category.class, CATEGORY_ROOT);
    }

    @Test
    @Order(3)
    public void testContacts() {
        var created = System.currentTimeMillis();
        var insert = newContact(CONTACT_UUID, ICON_UUID, created, created);
        insertAndCheck(insert, Contact.class, Contact[].class, CONTACT_ROOT);

        var original = get(CONTACT_UUID, Contact.class, CONTACT_ROOT);
        var update = newContact(CONTACT_UUID, null, original.created(), System.currentTimeMillis());
        updateAndCheck(update, Contact.class, CONTACT_ROOT);
    }

    @Test
    @Order(4)
    public void testCurrencies() {
        var created = System.currentTimeMillis();
        var insert = newCurrency(CURRENCY_UUID, created, created);
        insertAndCheck(insert, Currency.class, Currency[].class, CURRENCY_ROOT);

        var original = get(CURRENCY_UUID, Currency.class, CURRENCY_ROOT);
        var update = newCurrency(CURRENCY_UUID, original.created(), System.currentTimeMillis());
        updateAndCheck(update, Currency.class, CURRENCY_ROOT);
    }

    @Test
    @Order(5)
    public void testAccounts() {
        var created = System.currentTimeMillis();
        var insert = newAccount(ACCOUNT_UUID, CATEGORY_UUID, CURRENCY_UUID, ICON_UUID, created, created);
        insertAndCheck(insert, Account.class, Account[].class, ACCOUNT_ROOT);

        var original = get(ACCOUNT_UUID, Account.class, ACCOUNT_ROOT);
        var update = newAccount(ACCOUNT_UUID, CATEGORY_UUID, CURRENCY_UUID, null,
                original.created(), System.currentTimeMillis());
        updateAndCheck(update, Account.class, ACCOUNT_ROOT);
    }

    @Test
    @Order(6)
    public void testTransactions() {
        var created = System.currentTimeMillis();
        var insert = newTransaction(TRANSACTION_UUID, ACCOUNT_UUID, CATEGORY_UUID, CONTACT_UUID, created, created);
        insertAndCheck(insert, Transaction.class, Transaction[].class, TRANSACTION_ROOT);

        var original = get(TRANSACTION_UUID, Transaction.class, TRANSACTION_ROOT);
        var update = newTransaction(TRANSACTION_UUID, ACCOUNT_UUID, CATEGORY_UUID, null,
                original.created(), System.currentTimeMillis());
        updateAndCheck(update, Transaction.class, TRANSACTION_ROOT);
    }

    @Test
    @Order(7)
    public void testDocuments() {
        var created = System.currentTimeMillis();
        var insert = newDocument(DOCUMENT_UUID, ACCOUNT_UUID, CONTACT_UUID, created, created);
        insertAndCheck(insert, MoneyDocument.class, MoneyDocument[].class, DOCUMENT_ROOT);

        var original = get(DOCUMENT_UUID, MoneyDocument.class, DOCUMENT_ROOT);
        var update = newDocument(DOCUMENT_UUID, null, CONTACT_UUID,
                original.created(), System.currentTimeMillis());
        updateAndCheck(update, MoneyDocument.class, DOCUMENT_ROOT);
    }

    private <T extends MoneyRecord> void insertAndCheck(T insert, Class<T> clazz, Class<T[]> arrayClass, String api) {
        var inserted = put(insert, clazz, api);
        assertEquals(insert, inserted);

        var retrieved = get(insert.uuid(), clazz, api);
        assertEquals(insert, retrieved);

        var list = get(arrayClass, api);
        assertTrue(list.contains(retrieved));
    }

    private <T extends MoneyRecord> void updateAndCheck(T update, Class<T> clazz, String api) {
        var updated = put(update, clazz, api);
        assertEquals(update, updated);
        var retrieved = get(update.uuid(), clazz, api);
        assertEquals(update, retrieved);
    }

    private <T> List<T> get(Class<T[]> clazz, String api) {
        var array = given()
                .contentType(ContentType.JSON)
                .when()
                .request(Method.GET, CONTEXT_ROOT + api)
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(clazz);
        return Arrays.asList(array);
    }

    private <T> T get(UUID id, Class<T> clazz, String api) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .request(Method.GET, CONTEXT_ROOT + api + "/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(clazz);
    }

    private <T extends MoneyRecord> T put(T value, Class<T> clazz, String api) {
        return given()
                .contentType(ContentType.JSON)
                .body(value)
                .when()
                .request(Method.PUT, CONTEXT_ROOT + api + "/" + value.uuid())
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(clazz);
    }
}
