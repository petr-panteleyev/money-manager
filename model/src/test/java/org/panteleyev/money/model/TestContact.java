/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestContact {

    public static List<Arguments> testBuildDataProvider() {
        var uuid = UUID.randomUUID();
        var name = BaseTestUtils.randomString();
        var type = BaseTestUtils.randomContactType();
        var phone = BaseTestUtils.randomString();
        var mobile = BaseTestUtils.randomString();
        var email = BaseTestUtils.randomString();
        var web = BaseTestUtils.randomString();
        var comment = BaseTestUtils.randomString();
        var street = BaseTestUtils.randomString();
        var city = BaseTestUtils.randomString();
        var country = BaseTestUtils.randomString();
        var zip = BaseTestUtils.randomString();
        var iconUuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        return List.of(
                Arguments.of(
                        new Contact.Builder()
                                .uuid(uuid)
                                .name(name)
                                .type(type)
                                .phone(phone)
                                .mobile(mobile)
                                .email(email)
                                .web(web)
                                .comment(comment)
                                .street(street)
                                .city(city)
                                .country(country)
                                .zip(zip)
                                .iconUuid(iconUuid)
                                .created(created)
                                .modified(modified)
                                .build(),
                        new Contact(
                                uuid, name, type, phone, mobile,
                                email, web, comment, street, city,
                                country, zip, iconUuid, created, modified
                        )
                ),
                Arguments.of(
                        new Contact(
                                uuid, name, type, null, null,
                                null, null, null, null, null,
                                null, null, iconUuid, created, modified
                        ),
                        new Contact(
                                uuid, name, type, "", "",
                                "", "", "", "", "",
                                "", "", iconUuid, created, modified
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testBuildDataProvider")
    public void testBuild(MoneyRecord actual, MoneyRecord expected) {
        assertEquals(expected, actual);
        assertTrue(actual.created() > 0);
        assertTrue(actual.modified() >= actual.created());
    }

    @Test
    public void testEquals() {
        var name = BaseTestUtils.randomString();
        var type = BaseTestUtils.randomContactType();
        var phone = BaseTestUtils.randomString();
        var mobile = BaseTestUtils.randomString();
        var email = BaseTestUtils.randomString();
        var web = BaseTestUtils.randomString();
        var comment = BaseTestUtils.randomString();
        var street = BaseTestUtils.randomString();
        var city = BaseTestUtils.randomString();
        var country = BaseTestUtils.randomString();
        var zip = BaseTestUtils.randomString();
        var iconUuid = UUID.randomUUID();
        var uuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        var c1 = new Contact.Builder()
                .name(name)
                .type(type)
                .phone(phone)
                .mobile(mobile)
                .email(email)
                .web(web)
                .comment(comment)
                .street(street)
                .city(city)
                .country(country)
                .zip(zip)
                .iconUuid(iconUuid)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();

        var c2 = new Contact.Builder()
                .name(name)
                .type(type)
                .phone(phone)
                .mobile(mobile)
                .email(email)
                .web(web)
                .comment(comment)
                .street(street)
                .city(city)
                .country(country)
                .zip(zip)
                .iconUuid(iconUuid)
                .uuid(uuid)
                .created(created)
                .modified(modified)
                .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testCopy() {
        var original = new Contact.Builder()
                .name(BaseTestUtils.randomString())
                .type(BaseTestUtils.randomContactType())
                .phone(BaseTestUtils.randomString())
                .mobile(BaseTestUtils.randomString())
                .email(BaseTestUtils.randomString())
                .web(BaseTestUtils.randomString())
                .comment(BaseTestUtils.randomString())
                .street(BaseTestUtils.randomString())
                .city(BaseTestUtils.randomString())
                .country(BaseTestUtils.randomString())
                .zip(BaseTestUtils.randomString())
                .iconUuid(UUID.randomUUID())
                .uuid(UUID.randomUUID())
                .created(System.currentTimeMillis())
                .modified(System.currentTimeMillis())
                .build();

        var copy = new Contact.Builder(original).build();
        assertEquals(copy, original);
        assertEquals(copy.hashCode(), original.hashCode());

        var manualCopy = new Contact.Builder()
                .name(original.name())
                .type(original.type())
                .phone(original.phone())
                .mobile(original.mobile())
                .email(original.email())
                .web(original.web())
                .comment(original.comment())
                .street(original.street())
                .city(original.city())
                .country(original.country())
                .zip(original.zip())
                .iconUuid(original.iconUuid())
                .uuid(original.uuid())
                .created(original.created())
                .modified(original.modified())
                .build();
        assertEquals(manualCopy, original);
        assertEquals(manualCopy.hashCode(), original.hashCode());
    }
}
