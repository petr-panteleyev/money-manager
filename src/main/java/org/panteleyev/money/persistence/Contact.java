/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence;

import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.util.Objects;
import java.util.UUID;

@Table("contact")
public final class Contact implements MoneyRecord, Named, Comparable<Contact> {
    @Column(value = "id", primaryKey = true)
    private final int id;
    @Column("name")
    private final String name;
    @Column("type_id")
    private final int typeId;
    @Column("phone")
    private final String phone;
    @Column("mobile")
    private final String mobile;
    @Column("email")
    private final String email;
    @Column("web")
    private final String web;
    @Column("comment")
    private final String comment;
    @Column("street")
    private final String street;
    @Column("city")
    private final String city;
    @Column("country")
    private final String country;
    @Column("zip")
    private final String zip;
    @Column("guid")
    private final String guid;
    @Column("modified")
    private final long modified;

    private final ContactType type;

    @RecordBuilder
    public Contact(@Column("id") int id,
                   @Column("name") String name,
                   @Column("type_id") int typeId,
                   @Column("phone") String phone,
                   @Column("mobile") String mobile,
                   @Column("email") String email,
                   @Column("web") String web,
                   @Column("comment") String comment,
                   @Column("street") String street,
                   @Column("city") String city,
                   @Column("country") String country,
                   @Column("zip") String zip,
                   @Column("guid") String guid,
                   @Column("modified") long modified) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
        this.web = web;
        this.comment = comment;
        this.street = street;
        this.city = city;
        this.country = country;
        this.zip = zip;
        this.guid = guid;
        this.modified = modified;
        this.type = ContactType.get(this.typeId);
    }

    public Contact(int id, String name) {
        this(id, name, ContactType.PERSONAL.getId(),
                "", "", "", "", "", "", "", "", "",
                UUID.randomUUID().toString(), System.currentTimeMillis());
    }

    public Contact copy(int newId) {
        return new Contact(newId, name, typeId, phone, mobile, email, web, comment, street, city, country, zip,
                guid, modified);
    }

    public final ContactType getType() {
        return type;
    }

    @Override
    public int compareTo(Contact other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getPhone() {
        return phone;
    }

    public String getMobile() {
        return mobile;
    }

    public String getEmail() {
        return email;
    }

    public String getWeb() {
        return web;
    }

    public String getComment() {
        return comment;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getZip() {
        return zip;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public long getModified() {
        return modified;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, name, typeId, phone, mobile, email, web, comment, street, city, country, zip, guid,
                modified);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Contact)) {
            return false;
        }

        Contact that = (Contact)other;

        return id == that.id
                && Objects.equals(name, that.name)
                && typeId == that.typeId
                && Objects.equals(phone, that.phone)
                && Objects.equals(mobile, that.mobile)
                && Objects.equals(email, that.email)
                && Objects.equals(web, that.web)
                && Objects.equals(comment, that.comment)
                && Objects.equals(street, that.street)
                && Objects.equals(city, that.city)
                && Objects.equals(country, that.country)
                && Objects.equals(zip, that.zip)
                && Objects.equals(guid, that.guid)
                && modified == that.modified;
    }
}
