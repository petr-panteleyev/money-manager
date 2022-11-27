/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.client.MoneyClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.backend.Profiles.TEST;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.checkObject;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createCurrency;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.loadQuery;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.newClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST)
@Testcontainers
public class CurrencyTest {

    @LocalServerPort
    private int port;

    private final AtomicReference<MoneyClient> client = new AtomicReference<>(null);

    @BeforeEach
    public void init() {
        client.set(newClient(port));
    }

    @Test
    public void testCurrency() throws Exception {
        var created = createCurrency(client.get());
        checkObject(created, Map.of(
                "symbol", "USD",
                "description", "US Dollar",
                "formatSymbol", "$",
                "formatSymbolPosition", 0,
                "showFormatSymbol", true,
                "def", true,
                "rate", new BigDecimal("10.000000"),
                "direction", 1,
                "useThousandSeparator", true
        ));

        var uuid = created.uuid();

        var updateResult = client.get().currencyQuery(
                loadQuery("currency/updateCurrency.graphql"),
                Map.of("uuid", uuid.toString())
        );
        assertEquals("updateCurrency", updateResult.operation());

        var updated = updateResult.payload();
        checkObject(updated, Map.of(
                "uuid", created.uuid(),
                "symbol", "EUR",
                "description", "Euro",
                "formatSymbol", "",
                "formatSymbolPosition", 1,
                "showFormatSymbol", false,
                "def", true,
                "rate", new BigDecimal("1.000000"),
                "direction", 1,
                "useThousandSeparator", false
        ));
        assertTrue(updated.modified() >= created.modified());
        assertEquals(created.created(), updated.created());

        var getResult = client.get().currencyQuery(
                loadQuery("currency/getCurrency.graphql"),
                Map.of("uuid", uuid.toString())
        );
        assertEquals("currency", getResult.operation());
        assertEquals(updated, getResult.payload());

        var getAllResult = client.get().currencyListQuery(
                loadQuery("currency/getCurrencies.graphql")
        );
        assertEquals("currencies", getAllResult.operation());
        assertTrue(getAllResult.payload().contains(updated));
    }
}
