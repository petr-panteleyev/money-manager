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

package org.panteleyev.money.test

import org.panteleyev.money.persistence.Contact
import org.testng.Assert
import org.testng.annotations.Test
import java.util.UUID

class TestContact : BaseTest() {
    @Test
    fun testEquals() {
        val id = BaseTest.RANDOM.nextInt()
        val name = UUID.randomUUID().toString()
        val type = randomContactType()
        val phone = UUID.randomUUID().toString()
        val mobile = UUID.randomUUID().toString()
        val email = UUID.randomUUID().toString()
        val web = UUID.randomUUID().toString()
        val comment = UUID.randomUUID().toString()
        val street = UUID.randomUUID().toString()
        val city = UUID.randomUUID().toString()
        val country = UUID.randomUUID().toString()
        val zip = UUID.randomUUID().toString()
        val uuid = UUID.randomUUID().toString()
        val modified = System.currentTimeMillis()

        val c1 = Contact(id, name, type.id, phone, mobile, email, web,
                comment, street, city, country, zip,
                guid = uuid,
                modified = modified
        )

        val c2 = Contact(id, name, type.id, phone, mobile, email, web,
                comment, street, city, country, zip,
                guid = uuid,
                modified = modified
        )

        Assert.assertEquals(c1, c2)
        Assert.assertEquals(c1.hashCode(), c2.hashCode())
    }
}
