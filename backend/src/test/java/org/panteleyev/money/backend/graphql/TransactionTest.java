/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.backend.BaseSpringBootTest;
import org.panteleyev.money.client.MoneyClient;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.checkCollection;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.checkObject;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createAccount;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createCategory;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.createCurrency;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.loadQuery;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.newClient;
import static org.panteleyev.money.model.CategoryType.EXPENSES;
import static org.panteleyev.money.model.CategoryType.INCOMES;
import static org.panteleyev.money.model.TransactionType.CARD_PAYMENT;
import static org.panteleyev.money.model.TransactionType.TRANSFER;

public class TransactionTest extends BaseSpringBootTest {

    @LocalServerPort
    private int port;

    private final AtomicReference<MoneyClient> client = new AtomicReference<>(null);

    @BeforeEach
    public void init() {
        client.set(newClient(port));
    }

    @Test
    public void testTransaction() throws Exception {
        var currency = createCurrency(client.get());

        var debitedCategory = createCategory(client.get(), "Debited Category", EXPENSES);
        var creditedCategory = createCategory(client.get(), "Credited Category", INCOMES);

        var debitedAccount = createAccount(client.get(), debitedCategory.uuid(), currency.uuid());
        var creditedAccount = createAccount(client.get(), creditedCategory.uuid(), currency.uuid());

        var createResult = client.get().transactionModificationQuery(
                loadQuery("transaction/createTransaction.graphql"),
                Map.of(
                        "debitedAccountUuid", debitedAccount.uuid(),
                        "creditedAccountUuid", creditedAccount.uuid()
                )
        );
        assertEquals("createTransaction", createResult.operation());

        var created = createResult.payload();
        checkObject(created.transaction(), Map.ofEntries(
                entry("amount", new BigDecimal("1100.120000")),
                entry("creditAmount", new BigDecimal("1100.120000")),
                entry("day", 1),
                entry("month", 10),
                entry("year", 2022),
                entry("type", CARD_PAYMENT),
                entry("comment", "Test comment"),
                entry("checked", false),
                entry("accountDebited", Map.of(
                        "uuid", debitedAccount.uuid()
                )),
                entry("accountCredited", Map.of(
                        "uuid", creditedAccount.uuid()
                )),
                entry("contact", Optional.empty()),
                entry("invoiceNumber", "12345"),
                entry("parent", Optional.empty()),
                entry("detailed", false),
                entry("statementDate", LocalDate.of(2022, 10, 1))
        ));
        assertNull(created.contact());
        checkCollection(created.accounts(), List.of(
                Map.of(
                        "uuid", debitedAccount.uuid(),
                        "total", new BigDecimal("-1100.120000"),
                        "totalWaiting", new BigDecimal("-1100.120000")
                ),
                Map.of(
                        "uuid", creditedAccount.uuid(),
                        "total", new BigDecimal("1100.120000"),
                        "totalWaiting", new BigDecimal("1100.120000")
                )
        ));

        var newCreditedAccount = createAccount(client.get(), creditedCategory.uuid(), currency.uuid());

        var updateResult = client.get().transactionModificationQuery(
                loadQuery("transaction/updateTransaction.graphql"),
                Map.of(
                        "uuid", created.uuid(),
                        "debitedAccountUuid", debitedAccount.uuid(),
                        "creditedAccountUuid", newCreditedAccount.uuid(),
                        "contactName", "Contact name"
                )
        );
        assertEquals("updateTransaction", updateResult.operation());

        var updated = updateResult.payload();
        checkObject(updated.transaction(), Map.ofEntries(
                entry("uuid", created.transaction().uuid()),
                entry("amount", new BigDecimal("1100.200000")),
                entry("creditAmount", new BigDecimal("2200.400000")),
                entry("day", 2),
                entry("month", 11),
                entry("year", 2023),
                entry("type", TRANSFER),
                entry("comment", "Test comment 1"),
                entry("checked", true),
                entry("accountDebited", Map.of(
                        "uuid", debitedAccount.uuid()
                )),
                entry("accountCredited", Map.of(
                        "uuid", newCreditedAccount.uuid()
                )),
                entry("contact", Map.of(
                        "name", "Contact name"
                )),
                entry("invoiceNumber", "123456"),
                entry("parent", Optional.empty()),
                entry("detailed", false),
                entry("statementDate", LocalDate.of(2023, 11, 2))
        ));
        checkObject(updated.contact(), Map.of(
                "name", "Contact name"
        ));
        checkCollection(updated.accounts(), List.of(
                Map.of(
                        "uuid", debitedAccount.uuid(),
                        "total", new BigDecimal("-1100.200000"),
                        "totalWaiting", new BigDecimal("0.000000")
                ),
                Map.of(
                        "uuid", creditedAccount.uuid(),
                        "total", new BigDecimal("0.000000"),
                        "totalWaiting", new BigDecimal("0.000000")
                ),
                Map.of(
                        "uuid", newCreditedAccount.uuid(),
                        "total", new BigDecimal("2200.400000"),
                        "totalWaiting", new BigDecimal("0.000000")
                )
        ));
    }
}
