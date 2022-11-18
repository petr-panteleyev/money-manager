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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.backend.Profiles.TEST;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.checkObject;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createAccount;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createCategory;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createCurrency;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.loadQuery;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.newClient;
import static org.panteleyev.money.model.CardType.MASTERCARD;
import static org.panteleyev.money.model.CardType.NONE;
import static org.panteleyev.money.model.CategoryType.BANKS_AND_CASH;
import static org.panteleyev.money.model.CategoryType.INCOMES;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(TEST)
@Testcontainers
public class AccountTest {

    @LocalServerPort
    private int port;

    private final AtomicReference<MoneyClient> client = new AtomicReference<>(null);

    @BeforeEach
    public void init() {
        client.set(newClient(port));
    }

    @Test
    public void testAccount() throws Exception {
        var category = createCategory(client.get(), Map.of());
        var currency = createCurrency(client.get());

        var created = createAccount(client.get(), category.uuid(), currency.uuid());
        checkObject(created, Map.ofEntries(
                entry("name", "Test Account"),
                entry("comment", "Test account comment"),
                entry("accountNumber", "12345678"),
                entry("openingBalance", new BigDecimal("100.000000")),
                entry("accountLimit", new BigDecimal("200.000000")),
                entry("currencyRate", new BigDecimal("1.000000")),
                entry("category", Map.of(
                        "uuid", category.uuid(),
                        "name", "AAA",
                        "type", INCOMES
                )),
                entry("currency", Map.of(
                        "uuid", currency.uuid()
                )),
                entry("enabled", true),
                entry("interest", new BigDecimal("1.200000")),
                entry("closingDate", LocalDate.of(2022, 2, 3)),
                entry("icon", Optional.empty()),
                entry("cardType", NONE),
                entry("cardNumber", ""),
                entry("total", new BigDecimal("100.000000")),
                entry("totalWaiting", new BigDecimal("0.000000"))
        ));

        var updatedCategory = createCategory(client.get(), Map.of(
                "name", "BBB",
                "type", BANKS_AND_CASH
        ));

        var updateResult = client.get().accountQuery(
                loadQuery("account/updateAccount.graphql"),
                Map.of(
                        "uuid", created.uuid(),
                        "categoryUuid", updatedCategory.uuid(),
                        "currencyUuid", currency.uuid()
                )
        );
        assertEquals("updateAccount", updateResult.operation());

        var updated = updateResult.payload();
        checkObject(updated, Map.ofEntries(
                entry("uuid", created.uuid()),
                entry("name", "Test Account 1"),
                entry("comment", "Test account comment 1"),
                entry("accountNumber", "1234567890"),
                entry("openingBalance", new BigDecimal("110.000000")),
                entry("accountLimit", new BigDecimal("210.000000")),
                entry("currencyRate", new BigDecimal("11.000000")),
                entry("category", Map.of(
                        "uuid", updatedCategory.uuid(),
                        "name", "BBB",
                        "type", BANKS_AND_CASH
                )),
                entry("currency", Map.of(
                        "uuid", currency.uuid()
                )),
                entry("enabled", false),
                entry("interest", new BigDecimal("1.300000")),
                entry("closingDate", Optional.empty()),
                entry("icon", Optional.empty()),
                entry("cardType", MASTERCARD),
                entry("cardNumber", "1234 5678"),
                entry("total", new BigDecimal("110.000000")),
                entry("totalWaiting", new BigDecimal("20.000000"))
        ));
        assertTrue(updated.modified() >= created.modified());
        assertEquals(created.created(), updated.created());

        var getResult = client.get().accountQuery(
                loadQuery("account/getAccount.graphql"),
                Map.of("uuid", updated.uuid())
        );
        assertEquals("account", getResult.operation());
        assertEquals(updated, getResult.payload());

        var getAllResult = client.get().accountListQuery(
                loadQuery("account/getAccounts.graphql")
        );
        assertEquals("accounts", getAllResult.operation());
        assertTrue(getAllResult.payload().contains(updated));
    }
}
