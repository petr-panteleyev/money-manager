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

import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.PrimaryKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.util.Objects;
import java.util.UUID;

@Table("contact")
public final class Contact implements MoneyRecord, Named, Comparable<Contact> {
    @PrimaryKey
    @Column("uuid")
    private final UUID guid;
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
    @Column("created")
    private final long created;
    @Column("modified")
    private final long modified;

    private final ContactType type;

    @RecordBuilder
    public Contact(@Column("name") String name,
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
                   @Column("uuid") UUID guid,
                   @Column("created") long created,
                   @Column("modified") long modified)
    {
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
        this.created = created;
        this.modified = modified;
        this.type = ContactType.get(this.typeId);
    }

    public final ContactType getType() {
        return type;
    }

    @Override
    public int compareTo(Contact other) {
        return name.compareToIgnoreCase(other.name);
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
    public UUID getGuid() {
        return guid;
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public long getModified() {
        return modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, typeId, phone, mobile, email, web, comment, street, city, country, zip, guid,
            created, modified);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Contact)) {
            return false;
        }

        Contact that = (Contact) other;
        return Objects.equals(name, that.name)
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
            && created == that.created
            && modified == that.modified;
    }

    public static final class Builder {
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
        private UUID guid = null;
        private long created = 0L;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(Contact c) {
            if (c == null) {
                return;
            }

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
            created = c.getCreated();
            modified = c.getModified();
        }

        public Contact build() {
            if (name.isBlank()) {
                throw new IllegalStateException("Name must not be empty");
            }

            if (guid == null) {
                guid = UUID.randomUUID();
            }

            long now = System.currentTimeMillis();
            if (created == 0) {
                created = now;
            }
            if (modified == 0) {
                modified = now;
            }

            return new Contact(name, typeId, phone, mobile, email, web, comment, street, city, country, zip,
                guid, created, modified);
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

        public Builder guid(UUID guid) {
            this.guid = guid;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
