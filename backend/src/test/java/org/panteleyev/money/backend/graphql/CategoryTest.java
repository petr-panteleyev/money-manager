/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.client.MoneyClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.backend.Profiles.TEST;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.checkObject;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createCategory;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.loadQuery;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.newClient;
import static org.panteleyev.money.model.CategoryType.BANKS_AND_CASH;
import static org.panteleyev.money.model.CategoryType.INCOMES;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST)
@Testcontainers
public class CategoryTest {

    @LocalServerPort
    private int port;

    private final AtomicReference<MoneyClient> client = new AtomicReference<>(null);

    @BeforeEach
    public void init() {
        client.set(newClient(port));
    }

    @Test
    public void testCategory() throws Exception {
        var created = createCategory(client.get(), Map.of());
        checkObject(created, Map.of(
                "name", "AAA",
                "type", INCOMES
        ));

        var uuid = created.uuid();

        var updateResult = client.get().categoryQuery(
                loadQuery("category/updateCategory.graphql"),
                Map.of("uuid", uuid.toString())
        );
        assertEquals("updateCategory", updateResult.operation());

        var updated = updateResult.payload();
        checkObject(updated, Map.of(
                "uuid", created.uuid(),
                "name", "New name",
                "comment", "",
                "type", BANKS_AND_CASH
        ));
        assertTrue(updated.modified() >= created.modified());
        assertEquals(created.created(), updated.created());

        var getResult = client.get().categoryQuery(
                loadQuery("category/getCategory.graphql"),
                Map.of("uuid", uuid.toString())
        );
        assertEquals("category", getResult.operation());
        assertEquals(updated, getResult.payload());

        var getAllResult = client.get().categoryListQuery(
                loadQuery("category/getCategories.graphql")
        );
        assertEquals("categories", getAllResult.operation());
        assertTrue(getAllResult.payload().contains(updated));
    }
}
