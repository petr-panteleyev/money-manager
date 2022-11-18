/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql;

import junit.framework.AssertionFailedError;
import org.panteleyev.money.client.MoneyClient;
import org.panteleyev.money.client.dto.AccountDto;
import org.panteleyev.money.client.dto.CategoryDto;
import org.panteleyev.money.client.dto.CurrencyDto;
import org.panteleyev.money.client.dto.MoneyDto;
import org.panteleyev.money.model.CategoryType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GraphQLTestUtil {
    private GraphQLTestUtil() {
    }

    static MoneyClient newClient(int port) {
        return new MoneyClient.Builder()
                .withServerUrl("http://localhost:" + port)
                .build();
    }

    @SuppressWarnings("unchecked")
    static void checkObject(MoneyDto actual, Map<String, Object> expected) throws Exception {
        var actualClass = actual.getClass();
        if (!actualClass.isRecord()) {
            throw new IllegalArgumentException("Class must be a record");
        }

        assertNotNull(actual.uuid());
        assertTrue(actual.created() <= actual.modified());

        for (var entry : expected.entrySet()) {
            var key = entry.getKey();

            // find component
            var accessor = Arrays.stream(actualClass.getRecordComponents())
                    .filter(c -> c.getName().equals(key))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Record component " + key + " not found"))
                    .getAccessor();
            var value = accessor.invoke(actual);

            if (value instanceof MoneyDto moneyDto) {
                checkObject(moneyDto, (Map<String, Object>) entry.getValue());
            } else if (value instanceof Optional<?> optional) {
                if (optional.isEmpty()) {
                    assertEquals(entry.getValue(), Optional.empty(), key);
                } else {
                    var optionalValue = optional.get();
                    if (optionalValue instanceof MoneyDto moneyDtoValue) {
                        checkObject(moneyDtoValue, (Map<String, Object>) entry.getValue());
                    } else {
                        assertEquals(entry.getValue(), optionalValue, key);
                    }
                }
            } else {
                assertEquals(entry.getValue(), value, key);
            }
        }
    }

    static void checkCollection(
            Collection<? extends MoneyDto> actual,
            Collection<Map<String, Object>> expected
    ) throws Exception {
        assertEquals(expected.size(), actual.size());

        for (var map : expected) {
            if (map.get("uuid") instanceof UUID uuid) {
                var item = actual.stream()
                        .filter(o -> o.uuid().equals(uuid))
                        .findAny()
                        .orElseThrow(() -> new AssertionFailedError("Item with uuid=" + uuid + " not found"));
                checkObject(item, map);
            } else {
                throw new IllegalArgumentException("Expected does not contain uuid");
            }
        }
    }

    static String loadQuery(String fileName) {
        try (var inputStream = GraphQLTestUtil.class.getResourceAsStream("/queries/" + fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Query file " + fileName + " cannot be read");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static CurrencyDto createCurrency(MoneyClient client) {
        var response = client.currencyQuery(
                loadQuery("currency/createCurrency.graphql")
        );
        assertEquals("createCurrency", response.operation());
        return response.payload();
    }

    static CategoryDto createCategory(MoneyClient client, String name, CategoryType type) {
        var response = client.categoryQuery(
                loadQuery("category/createCategory.graphql"),
                Map.of(
                        "name", name,
                        "type", type
                )
        );
        assertEquals("createCategory", response.operation());
        return response.payload();
    }

    static CategoryDto createCategory(MoneyClient client, Map<String, Object> variables) {
        var response = client.categoryQuery(
                loadQuery("category/createCategory.graphql"),
                variables
        );
        assertEquals("createCategory", response.operation());
        return response.payload();
    }

    static AccountDto createAccount(MoneyClient client, UUID categoryUuid, UUID currencyUuid) {
        var response = client.accountQuery(
                loadQuery("account/createAccount.graphql"),
                Map.of(
                        "categoryUuid", categoryUuid.toString(),
                        "currencyUuid", currencyUuid.toString()
                )
        );
        assertEquals("createAccount", response.operation());
        return response.payload();
    }
}
