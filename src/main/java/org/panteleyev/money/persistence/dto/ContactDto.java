/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.nio.charset.StandardCharsets;
import static org.panteleyev.crypto.AES.aes256;

@Table("contact")
public final class ContactDto implements Record, Dto<Contact> {
    @Column(value = "id", primaryKey = true)
    private final int id;
    @Column(value = "bytes", length = BINARY_LENGTH)
    private final byte[] bytes;

    @RecordBuilder
    public ContactDto(@Column("id") int id, @Column("bytes") byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
    }

    ContactDto(Contact contact, String password) {
        id = contact.getId();

        var rawBytes = toJson(contact);
        bytes = password != null && !password.isEmpty() ?
                aes256().encrypt(rawBytes, password) : rawBytes;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public byte[] toJson(Contact contact) {
        var json = new JsonObject();
        json.addProperty("id", contact.getId());
        json.addProperty("name", contact.getName());
        json.addProperty("typeId", contact.getTypeId());
        json.addProperty("phone", contact.getPhone());
        json.addProperty("mobile", contact.getMobile());
        json.addProperty("email", contact.getEmail());
        json.addProperty("web", contact.getWeb());
        json.addProperty("comment", contact.getComment());
        json.addProperty("street", contact.getStreet());
        json.addProperty("city", contact.getCity());
        json.addProperty("country", contact.getCountry());
        json.addProperty("zip", contact.getZip());
        json.addProperty("guid", contact.getGuid());
        json.addProperty("modified", contact.getModified());
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Contact decrypt(String password) {
        var rawBytes = password != null && !password.isEmpty() ?
                aes256().decrypt(bytes, password) : bytes;
        var jsonString = new String(rawBytes, StandardCharsets.UTF_8);
        var obj = (JsonObject) new JsonParser().parse(jsonString);
        return new Contact(obj.get("id").getAsInt(),
                obj.get("name").getAsString(),
                obj.get("typeId").getAsInt(),
                obj.get("phone").getAsString(),
                obj.get("mobile").getAsString(),
                obj.get("email").getAsString(),
                obj.get("web").getAsString(),
                obj.get("comment").getAsString(),
                obj.get("street").getAsString(),
                obj.get("city").getAsString(),
                obj.get("country").getAsString(),
                obj.get("zip").getAsString(),
                obj.get("guid").getAsString(),
                obj.get("modified").getAsLong());
    }
}
