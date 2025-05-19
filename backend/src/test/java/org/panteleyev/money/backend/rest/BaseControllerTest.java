/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.panteleyev.money.backend.BaseSpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.backend.WebmoneyApplication.CONTEXT_ROOT;

public abstract class BaseControllerTest extends BaseSpringBootTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    public void init() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected <T> void insertAndCheck(T insert, Class<T> clazz, Class<T[]> arrayClass, String api,
            Supplier<UUID> uuidSupplier)
    {
        var inserted = put(uuidSupplier.get(), insert, clazz, api);
        assertEquals(insert, inserted);

        var retrieved = get(uuidSupplier.get(), clazz, api);
        assertEquals(insert, retrieved);

        var list = get(arrayClass, api);
        assertTrue(list.contains(retrieved));
    }

    protected <T> void updateAndCheck(T update, Class<T> clazz, String api, Supplier<UUID> uuidSupplier) {
        var updated = put(uuidSupplier.get(), update, clazz, api);
        assertEquals(update, updated);
        var retrieved = get(uuidSupplier.get(), clazz, api);
        assertEquals(update, retrieved);
    }

    protected <T> List<T> get(Class<T[]> clazz, String api) {
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

    protected <T> T get(UUID id, Class<T> clazz, String api) {
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

    protected <T> T put(UUID uuid, T value, Class<T> clazz, String api) {
        return given()
                .contentType(ContentType.JSON)
                .body(value)
                .when()
                .request(Method.PUT, CONTEXT_ROOT + api + "/" + uuid)
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(clazz);
    }
}
