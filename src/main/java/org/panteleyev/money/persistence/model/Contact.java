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

import java.util.Objects;
import java.util.UUID;

public final class Contact implements MoneyRecord, Named, Comparable<Contact> {
    private final int id;
    private final String name;
    private final int typeId;
    private final String phone;
    private final String mobile;
    private final String email;
    private final String web;
    private final String comment;
    private final String street;
    private final String city;
    private final String country;
    private final String zip;
    private final String guid;
    private final long modified;

    private final ContactType type;

    private Contact(int id, String name, int typeId, String phone, String mobile, String email, String web,
                    String comment, String street, String city, String country, String zip,
                    String guid, long modified)
    {
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

    public Contact copy(int newId) {
        return new Builder(newId).build();
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

        Contact that = (Contact) other;
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

    public static final class Builder {
        private int id;
        private String name = "";
        private int typeId = ContactType.PERSONAL.getId();
        private String phone = "";
        private String mobile = "";
        private String email = "";
        private String web = "";
        private String comment = "";
        private String street = "";
        private String city = "";
        private String country = "";
        private String zip = "";
        private String guid = null;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(int id) {
            this.id = id;
        }

        public Builder(Contact c) {
            if (c == null) {
                return;
            }

            id = c.getId();
            name = c.getName();
            typeId = c.getTypeId();
            phone = c.getPhone();
            mobile = c.getMobile();
            email = c.getEmail();
            web = c.getWeb();
            comment = c.getComment();
            street = c.getStreet();
            city = c.getCity();
            country = c.getCountry();
            zip = c.getZip();
            guid = c.getGuid();
            modified = c.getModified();
        }

        public Contact build() {
            if (name.isBlank()) {
                throw new IllegalStateException("Name must not be empty");
            }

            if (guid == null) {
                guid = UUID.randomUUID().toString();
            }

            if (modified == 0) {
                modified = System.currentTimeMillis();
            }

            return new Contact(id, name, typeId, phone, mobile, email, web, comment, street, city, country, zip,
                guid, modified);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Contact name must not be empty");
            }
            this.name = name;
            return this;
        }

        public Builder typeId(int typeId) {
            this.typeId = typeId;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone == null ? "" : phone;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile == null ? "" : mobile;
            return this;
        }

        public Builder email(String email) {
            this.email = email == null ? "" : email;
            return this;
        }

        public Builder web(String web) {
            this.web = web == null ? "" : web;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment == null ? "" : comment;
            return this;
        }

        public Builder street(String street) {
            this.street = street == null ? "" : street;
            return this;
        }

        public Builder city(String city) {
            this.city = city == null ? "" : city;
            return this;
        }

        public Builder country(String country) {
            this.country = country == null ? "" : country;
            return this;
        }

        public Builder zip(String zip) {
            this.zip = zip == null ? "" : zip;
            return this;
        }

        public Builder guid(String guid) {
            this.guid = guid;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
