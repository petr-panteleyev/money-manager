/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.persistence.model;

import org.panteleyev.money.BaseTest;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.panteleyev.money.BaseTestUtils.randomContactType;
import static org.panteleyev.money.BaseTestUtils.randomString;
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
