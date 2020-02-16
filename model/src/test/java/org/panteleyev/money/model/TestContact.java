package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.test.BaseTestUtils.randomContactType;
import static org.panteleyev.money.test.BaseTestUtils.randomString;
import static org.testng.Assert.assertEquals;

public class TestContact extends BaseTest {
    @Test
    public void testEquals() {
        var name = randomString();
        var type = randomContactType();
        var phone = randomString();
        var mobile = randomString();
        var email = randomString();
        var web = randomString();
        var comment = randomString();
        var street = randomString();
        var city = randomString();
        var country = randomString();
        var zip = randomString();
        var iconUuid = UUID.randomUUID();
        var uuid = UUID.randomUUID();
        var created = System.currentTimeMillis();
        var modified = System.currentTimeMillis();

        var c1 = new Contact.Builder()
            .name(name)
            .typeId(type.getId())
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
            .guid(uuid)
            .created(created)
            .modified(modified)
            .build();

        var c2 = new Contact.Builder()
            .name(name)
            .typeId(type.getId())
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
            .guid(uuid)
            .created(created)
            .modified(modified)
            .build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testBuilder() {
        var original = new Contact.Builder()
            .name(randomString())
            .typeId(randomContactType().getId())
            .phone(randomString())
            .mobile(randomString())
            .email(randomString())
            .web(randomString())
            .comment(randomString())
            .street(randomString())
            .city(randomString())
            .country(randomString())
            .zip(randomString())
            .iconUuid(UUID.randomUUID())
            .guid(UUID.randomUUID())
            .created(System.currentTimeMillis())
            .modified(System.currentTimeMillis())
            .build();

        var copy = new Contact.Builder(original).build();
        assertEquals(copy, original);
        assertEquals(copy.hashCode(), original.hashCode());

        var manualCopy = new Contact.Builder()
            .name(original.getName())
            .typeId(original.getTypeId())
            .phone(original.getPhone())
            .mobile(original.getMobile())
            .email(original.getEmail())
            .web(original.getWeb())
            .comment(original.getComment())
            .street(original.getStreet())
            .city(original.getCity())
            .country(original.getCountry())
            .zip(original.getZip())
            .iconUuid(original.getIconUuid())
            .guid(original.getUuid())
            .created(original.getCreated())
            .modified(original.getModified())
            .build();
        assertEquals(manualCopy, original);
        assertEquals(manualCopy.hashCode(), original.hashCode());
    }
}
