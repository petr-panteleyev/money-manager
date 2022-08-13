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
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Transaction;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.backend.BaseTestUtils.ACCOUNT_UUID;
import static org.panteleyev.money.backend.BaseTestUtils.CATEGORY_UUID;
import static org.panteleyev.money.backend.BaseTestUtils.CONTACT_UUID;
import static org.panteleyev.money.backend.BaseTestUtils.CURRENCY_UUID;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_DOLLAR;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_EURO;
import static org.panteleyev.money.backend.BaseTestUtils.ICON_UUID;
import static org.panteleyev.money.backend.BaseTestUtils.TRANSACTION_UUID;
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
import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTACT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTEXT_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;
import static org.panteleyev.money.backend.WebmoneyApplication.TRANSACTION_ROOT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    public void init() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Order(1)
    public void testIconInsert() {
        var created = System.currentTimeMillis();
        var insert = newIcon(ICON_UUID, ICON_DOLLAR, created, created);
        insertAndCheck(insert, Icon.class, Icon[].class, ICON_ROOT);
    }

    @Test
    @Order(2)
    public void testIconUpdate() {
        var original = get(ICON_UUID, Icon.class, ICON_ROOT);
        var update = newIcon(ICON_UUID, ICON_EURO, original.created(), System.currentTimeMillis());
        updateAndCheck(update, Icon.class, ICON_ROOT);
    }

    @Test
    @Order(3)
    public void testCategoryInsert() {
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
        insertAndCheck(insert, Category.class, Category[].class, CATEGORY_ROOT);
    }

    @Test
    @Order(4)
    public void testCategoryUpdate() {
        var original = get(CATEGORY_UUID, Category.class, CATEGORY_ROOT);
        var update = new Category(
                CATEGORY_UUID,
                randomString(),
                randomString(),
                randomCategoryType(),
                null,
                original.created(),
                System.currentTimeMillis()
        );
        updateAndCheck(update, Category.class, CATEGORY_ROOT);
    }

    @Test
    @Order(5)
    public void testContactInsert() {
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
        insertAndCheck(insert, Contact.class, Contact[].class, CONTACT_ROOT);
    }

    @Test
    @Order(6)
    public void testContactUpdate() {
        var original = get(CONTACT_UUID, Contact.class, CONTACT_ROOT);
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
                original.created(),
                System.currentTimeMillis()
        );
        updateAndCheck(update, Contact.class, CONTACT_ROOT);
    }

    @Test
    @Order(7)
    public void testCurrencyInsert() {
        var created = System.currentTimeMillis();
        var insert = new Currency(
                CURRENCY_UUID,
                randomString(),
                randomString(),
                randomString(),
                1,
                randomBoolean(),
                randomBoolean(),
                randomBigDecimal(),
                1,
                randomBoolean(),
                created,
                created
        );
        insertAndCheck(insert, Currency.class, Currency[].class, CURRENCY_ROOT);
    }

    @Test
    @Order(8)
    public void testCurrencyUpdate() {
        var original = get(CURRENCY_UUID, Currency.class, CURRENCY_ROOT);
        var update = new Currency(
                CURRENCY_UUID,
                randomString(),
                randomString(),
                randomString(),
                1,
                randomBoolean(),
                randomBoolean(),
                randomBigDecimal(),
                1,
                randomBoolean(),
                original.created(),
                System.currentTimeMillis()
        );
        updateAndCheck(update, Currency.class, CURRENCY_ROOT);
    }

    @Test
    @Order(9)
    public void testAccountInsert() {
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
        insertAndCheck(insert, Account.class, Account[].class, ACCOUNT_ROOT);
    }

    @Test
    @Order(10)
    public void testAccountUpdate() {
        var original = get(ACCOUNT_UUID, Account.class, ACCOUNT_ROOT);
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
                CURRENCY_UUID,
                randomBoolean(),
                randomBigDecimal(),
                LocalDate.now(),
                ICON_UUID,
                randomCardType(),
                randomString(),
                randomBigDecimal(),
                randomBigDecimal(),
                original.created(),
                System.currentTimeMillis()
        );
        updateAndCheck(update, Account.class, ACCOUNT_ROOT);
    }

    @Test
    @Order(11)
    public void testTransactionInsert() {
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
        insertAndCheck(insert, Transaction.class, Transaction[].class, TRANSACTION_ROOT);
    }

    @Test
    @Order(12)
    public void testTransactionUpdate() {
        var original = get(TRANSACTION_UUID, Transaction.class, TRANSACTION_ROOT);
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
                original.created(),
                System.currentTimeMillis()
        );
        updateAndCheck(update, Transaction.class, TRANSACTION_ROOT);
    }

    private <T extends MoneyRecord> void insertAndCheck(T insert, Class<T> clazz, Class<T[]> arrayClass, String api) {
        var inserted = put(insert, clazz, api);
        assertEquals(insert, inserted);

        var retrieved = get(insert.uuid(), clazz, api);
        assertEquals(insert, retrieved);

        var list = get(arrayClass, api);
        assertEquals(List.of(retrieved), list);
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
