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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.checkObject;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.loadQuery;
import static org.panteleyev.money.backend.graphql.GraphQLTestUtil.newClient;
import static org.panteleyev.money.model.ContactType.EMPLOYER;
import static org.panteleyev.money.model.ContactType.PERSONAL;

public class ContactTest extends BaseSpringBootTest {

    @LocalServerPort
    private int port;

    private final AtomicReference<MoneyClient> client = new AtomicReference<>(null);

    @BeforeEach
    public void init() {
        client.set(newClient(port));
    }

    @Test
    public void testContact() throws Exception {
        var response = client.get().contactQuery(
                loadQuery("contact/createContact.graphql")
        );
        assertEquals("createContact", response.operation());

        var created = response.payload();
        checkObject(created, Map.ofEntries(
                entry("name", "Test Contact"),
                entry("type", PERSONAL),
                entry("phone", "+1234567890"),
                entry("mobile", "+7123456778"),
                entry("email", "address@email.com"),
                entry("web", "www.web.com"),
                entry("comment", "Test comment"),
                entry("street", "Test Street"),
                entry("city", "Test City"),
                entry("country", "Test Country"),
                entry("zip", "Test ZIP"),
                entry("icon", Optional.empty())
        ));

        var uuid = created.uuid();

        var updateResult = client.get().contactQuery(
                loadQuery("contact/updateContact.graphql"),
                Map.of("uuid", uuid.toString())
        );
        assertEquals("updateContact", updateResult.operation());

        var updated = updateResult.payload();
        checkObject(updated, Map.ofEntries(
                entry("uuid", created.uuid()),
                entry("name", "Test Contact 1"),
                entry("type", EMPLOYER),
                entry("phone", "+1234567890 1"),
                entry("mobile", "+7123456778 1"),
                entry("email", "address@email.com 1"),
                entry("web", "www.web.com 1"),
                entry("comment", "Test comment 1"),
                entry("street", "Test Street 1"),
                entry("city", "Test City 1"),
                entry("country", "Test Country 1"),
                entry("zip", "Test ZIP 1")
        ));
        assertTrue(updated.modified() >= created.modified());
        assertEquals(created.created(), updated.created());

        var getResult = client.get().contactQuery(
                loadQuery("contact/getContact.graphql"),
                Map.of("uuid", uuid.toString())
        );
        assertEquals("contact", getResult.operation());
        assertEquals(updated, getResult.payload());

        var getAllResult = client.get().contactListQuery(
                loadQuery("contact/getContacts.graphql")
        );
        assertEquals("contacts", getAllResult.operation());
        assertTrue(getAllResult.payload().contains(updated));
    }
}
