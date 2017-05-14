/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.util.Objects;

@Table("contact")
public class Contact implements Record, Named, Comparable<Contact> {

    public static final class Builder {
        private int    id;
        private ContactType type;
        private String name;
        private String comment;
        private String email;
        private String web;
        private String phone;
        private String mobile;
        private String street;
        private String city;
        private String country;
        private String zip;

        public Builder() {
            type = ContactType.PERSONAL;
        }

        public Builder(Contact contact) {
            if (contact != null) {
                this.id = contact.getId();
                this.type = contact.getType();
                this.name = contact.getName();
                this.comment = contact.getComment();
                this.email = contact.getEmail();
                this.web = contact.getWeb();
                this.phone = contact.getPhone();
                this.mobile = contact.getMobile();
                this.street = contact.getStreet();
                this.city = contact.getCity();
                this.country = contact.getCountry();
                this.zip = contact.getZip();
            }
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public int id() {
            return id;
        }

        public Builder type(ContactType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder mobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder web(String web) {
            this.web = web;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder zip(String zip) {
            this.zip = zip;
            return this;
        }

        public Contact build() {
            if (id == 0) {
                throw new IllegalStateException("Contact.id == 0");
            }
            Objects.requireNonNull(type);
            Objects.requireNonNull(name);

            return new Contact(
                    id,
                    name,
                    type,
                    phone,
                    mobile,
                    email,
                    web,
                    comment,
                    street,
                    city,
                    country,
                    zip);
        }
    }

    private final int    id;
    private final String name;
    private final ContactType type;
    private final String comment;
    private final String email;
    private final String web;
    private final String phone;
    private final String mobile;
    private final String street;
    private final String city;
    private final String country;
    private final String zip;

    @RecordBuilder
    public Contact(
            @Field(Field.ID) int id,
            @Field("name") String name,
            @Field("type") ContactType type,
            @Field("phone") String phone,
            @Field("mobile") String mobile,
            @Field("email") String email,
            @Field("web") String web,
            @Field("comment") String comment,
            @Field("street") String street,
            @Field("city") String city,
            @Field("country") String country,
            @Field("zip") String zip
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.phone = phone;
        this.mobile = mobile;
        this.email = email;
        this.web = web;
        this.comment = comment;
        this.street = street;
        this.city = city;
        this.country = country;
        this.zip = zip;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public int getId() {
        return id;
    }

    @Field("name")
    @Override
    public String getName() {
        return name;
    }

    @Field(value = "type", nullable=false)
    public ContactType getType() {
        return type;
    }

    @Field("comment")
    public String getComment() {
        return comment;
    }

    @Field("email")
    public String getEmail() {
        return email;
    }

    @Field("web")
    public String getWeb() {
        return web;
    }

    @Field("phone")
    public String getPhone() {
        return phone;
    }

    @Field("mobile")
    public String getMobile() {
        return mobile;
    }

    @Field("street")
    public String getStreet() {
        return street;
    }

    @Field("city")
    public String getCity() {
        return city;
    }

    @Field("country")
    public String getCountry() {
        return country;
    }

    @Field("zip")
    public String getZip() {
        return zip;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Contact) {
            Contact that = (Contact)obj;
            return this.id == that.id
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.type, that.type)
                    && Objects.equals(this.comment, that.comment)
                    && Objects.equals(this.email, that.email)
                    && Objects.equals(this.web, that.web)
                    && Objects.equals(this.phone, that.phone)
                    && Objects.equals(this.mobile, that.mobile)
                    && Objects.equals(this.street, that.street)
                    && Objects.equals(this.city, that.city)
                    && Objects.equals(this.country, that.country)
                    && Objects.equals(this.zip, that.zip);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, comment, email, web, phone, mobile, street, city, country, zip);
    }

    @Override
    public int compareTo(Contact o) {
        return Objects.compare(this.getName(), o.getName(), String::compareToIgnoreCase);
    }
}
