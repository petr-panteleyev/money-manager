/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.UUID;

public class TestContact extends BaseTest {
    @Test
    public void testEquals() {
        int id = randomId();
        String name = UUID.randomUUID().toString();
        ContactType type = randomContactType();
        String phone = UUID.randomUUID().toString();
        String mobile = UUID.randomUUID().toString();
        String email = UUID.randomUUID().toString();
        String web = UUID.randomUUID().toString();
        String comment = UUID.randomUUID().toString();
        String street = UUID.randomUUID().toString();
        String city = UUID.randomUUID().toString();
        String country = UUID.randomUUID().toString();
        String zip = UUID.randomUUID().toString();
        String uuid = UUID.randomUUID().toString();
        long modified = System.currentTimeMillis();

        Contact c1 = new Contact(id, name, type.getId(), phone, mobile, email, web,
                comment, street, city, country, zip, uuid, modified);

        Contact c2 = new Contact(id, name, type.getId(), phone, mobile, email, web,
                comment, street, city, country, zip, uuid, modified);

        Assert.assertEquals(c1, c2);
        Assert.assertEquals(c1.hashCode(), c2.hashCode());
    }
}
